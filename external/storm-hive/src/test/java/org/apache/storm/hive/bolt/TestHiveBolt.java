
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.hive.bolt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.storm.Config;
import org.apache.storm.hive.bolt.mapper.DelimitedRecordHiveMapper;
import org.apache.storm.hive.bolt.mapper.JsonRecordHiveMapper;
import org.apache.storm.hive.common.HiveOptions;
import org.apache.storm.hive.common.HiveWriter;
import org.apache.storm.hive.common.HiveWriterPool;
import org.apache.storm.hive.common.PartitionValues;
import org.apache.storm.task.GeneralTopologyContext;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.MockTupleHelpers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestHiveBolt {

    private final static String dbName = "testdb";
    private final static String tblName = "test_table";
    private final static String dbName1 = "testdb1";
    private final static String tblName1 = "test_table1";
    private final static String PART1_NAME = "city";
    private final static String PART2_NAME = "state";
    private final static String[] partNames = {PART1_NAME, PART2_NAME};
    private static final String COL1 = "id";
    private static final String COL2 = "msg";
    private static final Logger LOG = LoggerFactory.getLogger(HiveBolt.class);
    private final String partitionVals = "sunnyvale,ca";
    private final String[] colNames = {COL1, COL2};
    private final String[] colNames1 = {COL2, COL1};
    private final String metaStoreURI = null;
    private final String[] colTypes = {serdeConstants.INT_TYPE_NAME, serdeConstants.STRING_TYPE_NAME};
    private final Config config = new Config();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private OutputCollector collector;

    private static class InMemoryTesting implements AutoCloseable {

        private final Map<PartitionValues, List<byte[]>> partitionValuesToWrittenRecords = new HashMap<>();
        private final HiveWriterPool writerPoolMock = Mockito.mock(HiveWriterPool.class);
        private final HiveBolt bolt;

        public InMemoryTesting(HiveOptions options) throws Exception {
            Mockito.doAnswer(invocation -> {
                PartitionValues partitionVals = invocation.getArgument(0);
                HiveWriter writerMock = Mockito.mock(HiveWriter.class);
                Mockito.doAnswer(writeInvocation -> {
                    List<byte[]> writtenRecords = partitionValuesToWrittenRecords.get(partitionVals);
                    if (writtenRecords == null) {
                        writtenRecords = new ArrayList<>();
                        partitionValuesToWrittenRecords.put(partitionVals, writtenRecords);
                    }
                    writtenRecords.add(writeInvocation.getArgument(0));
                    return null;
                }).when(writerMock).write(any());
                return writerMock;
            }).when(writerPoolMock).getOrCreateWriter(any());
            bolt = new HiveBolt(options, (opts, callExecutor) -> writerPoolMock);
        }

        @Override
        public void close() throws Exception {
            bolt.cleanup();
        }

        public Map<PartitionValues, List<byte[]>> getPartitionValuesToWrittenRecords() {
            return partitionValuesToWrittenRecords;
        }

        public HiveBolt getBolt() {
            return bolt;
        }

        public HiveWriterPool getWriterPoolMock() {
            return writerPoolMock;
        }
    }

    @Test
    public void testWithByteArrayIdandMessage()
        throws Exception {
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(colNames))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(2);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, collector);

            Integer id = 100;
            String msg = "test-123";
            String city = "sunnyvale";
            String state = "ca";

            Set<Tuple> tupleSet = new HashSet<>();
            for (int i = 0; i < 4; i++) {
                Tuple tuple = generateTestTuple(id, msg, city, state);
                bolt.execute(tuple);
                tupleSet.add(tuple);
            }

            PartitionValues partVals = new PartitionValues(Lists.newArrayList(city, state));

            for (Tuple t : tupleSet) {
                verify(collector).ack(t);
            }

            Assert.assertEquals(4, testing.getPartitionValuesToWrittenRecords().get(partVals).size());
        }
    }

    @Test
    public void testWithoutPartitions()
        throws Exception {
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(colNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName1, tblName1, mapper)
            .withTxnsPerBatch(2).withBatchSize(2).withAutoCreatePartitions(false);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, collector);

            Integer id = 100;
            String msg = "test-123";
            String city = "sunnyvale";
            String state = "ca";

            Set<Tuple> tupleSet = new HashSet<Tuple>();
            for (int i = 0; i < 4; i++) {
                Tuple tuple = generateTestTuple(id, msg, city, state);
                bolt.execute(tuple);
                tupleSet.add(tuple);
            }

            PartitionValues partVals = new PartitionValues(Collections.emptyList());

            for (Tuple t : tupleSet) {
                verify(collector).ack(t);
            }

            List<byte[]> recordWritten = testing.getPartitionValuesToWrittenRecords().get(partVals);
            Assert.assertNotNull(recordWritten);
            Assert.assertEquals(4, recordWritten.size());

        }
    }

    @Test
    public void testWithTimeformat()
        throws Exception {
        String timeFormat = "yyyy/MM/dd";
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(colNames))
            .withTimeAsPartitionField(timeFormat);
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName1, tblName1, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(1)
            .withMaxOpenConnections(1);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, collector);

            Integer id = 100;
            String msg = "test-123";
            Date d = new Date();
            SimpleDateFormat parseDate = new SimpleDateFormat(timeFormat);
            String today = parseDate.format(d.getTime());

            List<Tuple> tuples = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Tuple tuple = generateTestTuple(id, msg, null, null);
                tuples.add(tuple);
                bolt.execute(tuple);
            }

            for (Tuple t : tuples) {
                verify(collector).ack(t);
            }

            PartitionValues partVals = new PartitionValues(Lists.newArrayList(today));

            List<byte[]> recordsWritten = testing.getPartitionValuesToWrittenRecords().get(partVals);
            Assert.assertNotNull(recordsWritten);
            Assert.assertEquals(2, recordsWritten.size());

            byte[] mapped = generateDelimitedRecord(Lists.newArrayList(id, msg), mapper.getFieldDelimiter());

            for (byte[] record : recordsWritten) {
                Assert.assertArrayEquals(mapped, record);
            }

        }
    }

    @Test
    public void testData()
        throws Exception {
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(colNames))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(1);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, new OutputCollector(collector));

            Integer id = 1;
            String msg = "SJC";
            String city = "Sunnyvale";
            String state = "CA";

            Tuple tuple1 = generateTestTuple(id, msg, city, state);

            bolt.execute(tuple1);
            verify(collector).ack(tuple1);

            PartitionValues partVals = new PartitionValues(Lists.newArrayList(city, state));

            List<byte[]> recordsWritten = testing.getPartitionValuesToWrittenRecords().get(partVals);
            Assert.assertNotNull(recordsWritten);
            Assert.assertEquals(1, recordsWritten.size());

            byte[] mapped = generateDelimitedRecord(Lists.newArrayList(id, msg), mapper.getFieldDelimiter());
            Assert.assertArrayEquals(mapped, recordsWritten.get(0));

        }
    }

    @Test
    public void testJsonWriter()
        throws Exception {
        // json record doesn't need columns to be in the same order
        // as table in hive.
        JsonRecordHiveMapper mapper = new JsonRecordHiveMapper()
            .withColumnFields(new Fields(colNames1))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(1);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, collector);

            Integer id = 1;
            String msg = "SJC";
            String city = "Sunnyvale";
            String state = "CA";

            Tuple tuple1 = generateTestTuple(id, msg, city, state);

            bolt.execute(tuple1);
            verify(collector).ack(tuple1);

            PartitionValues partVals = new PartitionValues(Lists.newArrayList(city, state));

            List<byte[]> recordsWritten = testing.getPartitionValuesToWrittenRecords().get(partVals);
            Assert.assertNotNull(recordsWritten);
            Assert.assertEquals(1, recordsWritten.size());

            byte[] written = recordsWritten.get(0);

            Map<String, ?> writtenMap = objectMapper.readValue(new String(written), new TypeReference<Map<String, ?>>() {
            });

            Map<String, Object> expected = new HashMap<>();
            expected.put(COL1, id);
            expected.put(COL2, msg);

            Assert.assertEquals(expected, writtenMap);

        }
    }

    @Test
    public void testNoAcksUntilFlushed() throws Exception {
        JsonRecordHiveMapper mapper = new JsonRecordHiveMapper()
            .withColumnFields(new Fields(colNames1))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(2);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, new OutputCollector(collector));

            Tuple tuple1 = generateTestTuple(1, "SJC", "Sunnyvale", "CA");
            Tuple tuple2 = generateTestTuple(2, "SFO", "San Jose", "CA");

            bolt.execute(tuple1);
            verifyZeroInteractions(collector);

            bolt.execute(tuple2);
            verify(collector).ack(tuple1);
            verify(collector).ack(tuple2);
        }
    }

    @Test
    public void testNoAcksIfFlushFails() throws Exception {
        JsonRecordHiveMapper mapper = new JsonRecordHiveMapper()
            .withColumnFields(new Fields(colNames1))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(2);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {

            doThrow(new InterruptedException())
                .when(testing.getWriterPoolMock()).flushAllWriters();
            HiveBolt failingBolt = testing.getBolt();

            failingBolt.prepare(config, null, new OutputCollector(collector));

            Tuple tuple1 = generateTestTuple(1, "SJC", "Sunnyvale", "CA");
            Tuple tuple2 = generateTestTuple(2, "SFO", "San Jose", "CA");

            failingBolt.execute(tuple1);
            failingBolt.execute(tuple2);

            verify(collector, never()).ack(tuple1);
            verify(collector, never()).ack(tuple2);

        }
    }

    @Test
    public void testTickTuple() throws Exception {
        JsonRecordHiveMapper mapper = new JsonRecordHiveMapper()
            .withColumnFields(new Fields(colNames1))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(2);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, new OutputCollector(collector));

            Tuple tuple1 = generateTestTuple(1, "SJC", "Sunnyvale", "CA");
            Tuple tuple2 = generateTestTuple(2, "SFO", "San Jose", "CA");

            bolt.execute(tuple1);

            //The tick should cause tuple1 to be ack'd
            Tuple mockTick = MockTupleHelpers.mockTickTuple();
            bolt.execute(mockTick);
            verify(collector).ack(tuple1);

            //The second tuple should NOT be ack'd because the batch should be cleared and this will be
            //the first transaction in the new batch
            bolt.execute(tuple2);
            verify(collector, never()).ack(tuple2);

        }
    }

    @Test
    public void testNoTickEmptyBatches() throws Exception {
        JsonRecordHiveMapper mapper = new JsonRecordHiveMapper()
            .withColumnFields(new Fields(colNames1))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(2)
            .withBatchSize(2);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, new OutputCollector(collector));

            //The tick should NOT cause any acks since the batch was empty except for acking itself
            Tuple mockTick = MockTupleHelpers.mockTickTuple();
            bolt.execute(mockTick);
            verifyZeroInteractions(collector);

        }
    }

    @Test
    public void testMultiPartitionTuples()
        throws Exception {
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(colNames))
            .withPartitionFields(new Fields(partNames));
        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(10)
            .withBatchSize(10);

        try (InMemoryTesting testing = new InMemoryTesting(hiveOptions)) {
            HiveBolt bolt = testing.getBolt();
            bolt.prepare(config, null, new OutputCollector(collector));

            Integer id = 1;
            String msg = "test";
            String city = "San Jose";
            String state = "CA";

            List<Tuple> tuples = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Tuple tuple = generateTestTuple(id, msg, city, state);
                tuples.add(tuple);
                bolt.execute(tuple);
            }

            for (Tuple t : tuples) {
                verify(collector).ack(t);
            }

            PartitionValues partVals = new PartitionValues(Lists.newArrayList(city, state));

            List<byte[]> recordsWritten = testing.getPartitionValuesToWrittenRecords().get(partVals);
            Assert.assertNotNull(recordsWritten);
            Assert.assertEquals(100, recordsWritten.size());

            byte[] mapped = generateDelimitedRecord(Lists.newArrayList(id, msg), mapper.getFieldDelimiter());

            for (byte[] record : recordsWritten) {
                Assert.assertArrayEquals(mapped, record);
            }

        }
    }

    private Tuple generateTestTuple(Object id, Object msg, Object city, Object state) {
        TopologyBuilder builder = new TopologyBuilder();
        GeneralTopologyContext topologyContext = new GeneralTopologyContext(builder.createTopology(),
            new Config(), new HashMap(), new HashMap(), new HashMap(), "") {
            @Override
            public Fields getComponentOutputFields(String componentId, String streamId) {
                return new Fields("id", "msg", "city", "state");
            }
        };
        return new TupleImpl(topologyContext, new Values(id, msg, city, state), "", 1, "");
    }

    private byte[] generateDelimitedRecord(List<?> values, char fieldDelimiter) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(value);
            builder.append(fieldDelimiter);
        }
        return builder.toString().getBytes();
    }

}
