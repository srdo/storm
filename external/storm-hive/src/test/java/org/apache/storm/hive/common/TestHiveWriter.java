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

package org.apache.storm.hive.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.streaming.StreamingConnection;
import org.apache.storm.Config;
import org.apache.storm.hive.bolt.mapper.DelimitedRecordHiveMapper;
import org.apache.storm.hive.bolt.mapper.HiveMapper;
import org.apache.storm.hive.common.HiveWriter.SerializationFailure;
import org.apache.storm.task.GeneralTopologyContext;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public class TestHiveWriter {
    public static final String PART1_NAME = "city";
    public static final String PART2_NAME = "state";
    public static final String[] partNames = { PART1_NAME, PART2_NAME };
    final static String dbName = "testdb";
    final static String tblName = "test_table2";
    final PartitionValues partitionVals = new PartitionValues(Arrays.asList(new String[] { "sunnyvale", "ca" }));
    final String[] colNames = { "id", "msg" };
    private final String metaStoreURI = null;
    int timeout = 10000; // msec
    UserGroupInformation ugi = null;
    private ExecutorService callTimeoutPool;
    private DelimitedRecordHiveMapper mapper;
    private HiveOptions options;

    @BeforeEach
    public void setUp() throws Exception {
        int callTimeoutPoolSize = 1;
        callTimeoutPool = Executors.newFixedThreadPool(callTimeoutPoolSize,
                                                       new ThreadFactoryBuilder().setNameFormat("hiveWriterTest").build());
        mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(colNames))
            .withPartitionFields(new Fields(partNames));
        options = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
            .withTxnsPerBatch(10);
    }
    
    @AfterEach
    public void tearDown() {
        callTimeoutPool.shutdownNow();
    }

    @Test
    public void testInstantiate() throws Exception {
        HiveWriter writer = new HiveWriter(partitionVals, options, callTimeoutPool, (ignored, ignored2) -> mock(StreamingConnection.class));
        writer.close();
    }

    @Test
    public void testWriteBasic() throws Exception {
        StreamingConnection connectionMock = mock(StreamingConnection.class);
        try (HiveWriter writer = new HiveWriter(partitionVals, options, callTimeoutPool, (ignored, ignored2) -> connectionMock)) {
            writeTuples(writer, mapper, 3);
            writer.flush(false);
        }
        InOrder inOrder = inOrder(connectionMock);
        inOrder.verify(connectionMock, times(1)).beginTransaction();
        inOrder.verify(connectionMock, times(3)).write(any(byte[].class));
        inOrder.verify(connectionMock).commitTransaction();
    }

    @Test
    public void testWriteMultiFlush() throws Exception {
        StreamingConnection connectionMock = mock(StreamingConnection.class);
        try (HiveWriter writer = new HiveWriter(partitionVals, options, callTimeoutPool, (ignored, ignored2) -> connectionMock)) {
            Tuple tuple = generateTestTuple("1", "abc");
            writer.write(mapper.mapRecord(tuple));
            tuple = generateTestTuple("2", "def");
            writer.write(mapper.mapRecord(tuple));
            assertEquals(2, writer.getTotalRecords());
            InOrder inOrder = inOrder(connectionMock);
            inOrder.verify(connectionMock).beginTransaction();
            inOrder.verify(connectionMock, times(2)).write(any(byte[].class));
            writer.flush(true);
            assertEquals(0, writer.getTotalRecords());
            inOrder = inOrder(connectionMock);
            inOrder.verify(connectionMock).commitTransaction();
            inOrder.verify(connectionMock).beginTransaction();
            clearInvocations(connectionMock);

            tuple = generateTestTuple("3", "ghi");
            writer.write(mapper.mapRecord(tuple));
            writer.flush(true);
            inOrder = inOrder(connectionMock);
            inOrder.verify(connectionMock).write(any(byte[].class));
            inOrder.verify(connectionMock).commitTransaction();
            inOrder.verify(connectionMock).beginTransaction();
            clearInvocations(connectionMock);

            tuple = generateTestTuple("4", "klm");
            writer.write(mapper.mapRecord(tuple));
            writer.flush(true);
            writer.close();
            inOrder = inOrder(connectionMock);
            inOrder.verify(connectionMock).write(any(byte[].class));
            inOrder.verify(connectionMock).commitTransaction();
            inOrder.verify(connectionMock).beginTransaction();
        }
    }

    private Tuple generateTestTuple(Object id, Object msg) {
        TopologyBuilder builder = new TopologyBuilder();
        GeneralTopologyContext topologyContext = new GeneralTopologyContext(builder.createTopology(),
                                                                            new Config(), new HashMap(), new HashMap(), new HashMap(), "") {
            @Override
            public Fields getComponentOutputFields(String componentId, String streamId) {
                return new Fields("id", "msg");
            }
        };
        return new TupleImpl(topologyContext, new Values(id, msg), "", 1, "");
    }

    private void writeTuples(HiveWriter writer, HiveMapper mapper, int count)
        throws HiveWriter.WriteFailure, InterruptedException, SerializationFailure {
        Integer id = 100;
        String msg = "test-123";
        for (int i = 1; i <= count; i++) {
            Tuple tuple = generateTestTuple(id, msg);
            writer.write(mapper.mapRecord(tuple));
        }
    }

}