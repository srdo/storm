/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.kafka.spout;

import static org.apache.storm.kafka.spout.config.builder.SingleTopicKafkaSpoutConfiguration.createKafkaSpoutConfigBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.storm.kafka.spout.config.builder.SingleTopicKafkaSpoutConfiguration;
import org.apache.storm.kafka.spout.subscription.ManualPartitioner;
import org.apache.storm.kafka.spout.subscription.TopicFilter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.utils.Time;
import org.apache.storm.utils.Time.SimulatedTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class KafkaSpoutRetryLimitTest {

    private final long offsetCommitPeriodMs = 2_000;
    private final TopologyContext contextMock = mock(TopologyContext.class);
    private final SpoutOutputCollector collectorMock = mock(SpoutOutputCollector.class);
    private final Map<String, Object> conf = new HashMap<>();
    private final TopicPartition partition = new TopicPartition(SingleTopicKafkaSpoutConfiguration.TOPIC, 1);
    private MockConsumer<String, String> mockConsumer;
    private KafkaSpoutConfig<String, String> spoutConfig;
    
    public static final KafkaSpoutRetryService ZERO_RETRIES_RETRY_SERVICE =
        new KafkaSpoutRetryExponentialBackoff(KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(0), KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(0),
            0, KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(0));
    
    @Before
    public void setUp() {
        spoutConfig = SingleTopicKafkaSpoutConfiguration.createKafkaSpoutConfigBuilderForMockConsumer(partition.topic())
            .setOffsetCommitPeriodMs(offsetCommitPeriodMs)
            .setRetry(ZERO_RETRIES_RETRY_SERVICE)
            .build();
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.NONE);
    }
    
    @Test
    public void testFailingTupleCompletesAckAfterRetryLimitIsMet() {
        //Spout should ack failed messages after they hit the retry limit
        try (SimulatedTime simulatedTime = new SimulatedTime()) {
            KafkaSpout<String, String> spout = SpoutWithMockedConsumerSetupHelper.setupSpout2(spoutConfig, conf, contextMock, collectorMock, mockConsumer,
                new TopicPartitionWithOffsetRange(partition, 0L, null));
            
            SpoutWithMockedConsumerSetupHelper.addRecords(mockConsumer, partition, 0L, 4);
            List<KafkaSpoutMessageId> messageIds = SpoutWithMockedConsumerSetupHelper.callNextTupleAndGetEmittedIds(spout, mockConsumer, 4, collectorMock);
            
            for (KafkaSpoutMessageId messageId : messageIds) {
                spout.fail(messageId);
            }

            // Advance time and then trigger call to kafka consumer commit
            Time.advanceTime(KafkaSpout.TIMER_DELAY_MS + offsetCommitPeriodMs);
            spout.nextTuple();
            
            //verify that offset 4 was committed for the given TopicPartition, since processing should resume at 4.
            assertThat(mockConsumer.committed(partition).offset(), is(4L));
        }
    }
    
}
