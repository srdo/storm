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

import static org.apache.storm.kafka.spout.KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.storm.kafka.KafkaUnitRule;
import org.apache.storm.kafka.spout.config.builder.SingleTopicKafkaSpoutConfiguration;
import org.apache.storm.kafka.spout.internal.KafkaConsumerFactory;
import org.apache.storm.kafka.spout.internal.KafkaConsumerFactoryDefault;
import org.apache.storm.kafka.spout.subscription.ManualPartitionSubscription;
import org.apache.storm.kafka.spout.subscription.ManualPartitioner;
import org.apache.storm.kafka.spout.subscription.NamedTopicFilter;
import org.apache.storm.kafka.spout.subscription.RoundRobinManualPartitioner;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KafkaSpoutMultiPartitionTest {

    @Rule
    public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule();

    @Captor
    private ArgumentCaptor<Map<TopicPartition, OffsetAndMetadata>> commitCapture;

    private final TopologyContext topologyContext = mock(TopologyContext.class);
    private final Map<String, Object> conf = new HashMap<>();
    private final SpoutOutputCollector collectorMock = mock(SpoutOutputCollector.class);
    private final ManualPartitioner partitionerMock = mock(ManualPartitioner.class);
    private final long commitOffsetPeriodMs = 2_000;
    private KafkaConsumer<String, String> consumerSpy;
    private KafkaSpout<String, String> spout;
    private final int maxPollRecords = 10;

    @Before
    public void setUp() {
        KafkaSpoutConfig<String, String> spoutConfig =
            SingleTopicKafkaSpoutConfiguration.createKafkaSpoutConfigBuilder(
                new ManualPartitionSubscription(partitionerMock,
                    new NamedTopicFilter(SingleTopicKafkaSpoutConfiguration.TOPIC)),
                kafkaUnitRule.getKafkaUnit().getKafkaPort())
                .setFirstPollOffsetStrategy(EARLIEST)
                .setOffsetCommitPeriodMs(commitOffsetPeriodMs)
                .setProp(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords)
                .build();
        KafkaConsumerFactory<String, String> consumerFactory = new KafkaConsumerFactoryDefault<>();
        this.consumerSpy = spy(consumerFactory.createConsumer(spoutConfig));
        KafkaConsumerFactory<String, String> consumerFactoryMock = mock(KafkaConsumerFactory.class);
        when(consumerFactoryMock.createConsumer(any()))
            .thenReturn(consumerSpy);
        this.spout = new KafkaSpout<>(spoutConfig, consumerFactoryMock);
    }

    private KafkaSpoutMessageId emitOne() {
        ArgumentCaptor<KafkaSpoutMessageId> messageId = ArgumentCaptor.forClass(KafkaSpoutMessageId.class);
        spout.nextTuple();
        verify(collectorMock).emit(anyString(), anyList(), messageId.capture());
        clearInvocations(collectorMock);
        return messageId.getValue();
    }

    @Test
    public void testReassignPartitionsCleansUpPendingTupleCache() throws Exception {
        /*
         * When partitions are reassigned, the spout must clean up the pending tuple caches for partitions it is no longer assigned.
         * This is necessary to prevent the spout from emitting tuples on revoked partitions.
         */

        String topic = SingleTopicKafkaSpoutConfiguration.TOPIC;
        TopicPartition assignedPartition = new TopicPartition(topic, 1);
        TopicPartition partitionToRevoke = new TopicPartition(topic, 2);
        when(partitionerMock.partition(any(), any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        SingleTopicKafkaUnitSetupHelper.populateTopicData(kafkaUnitRule.getKafkaUnit(), assignedPartition, 10);
        SingleTopicKafkaUnitSetupHelper.populateTopicData(kafkaUnitRule.getKafkaUnit(), partitionToRevoke, 10);
        SingleTopicKafkaUnitSetupHelper.initializeSpout(spout, conf, topologyContext, collectorMock);

        //Poll in order to fill the cache
        spout.nextTuple();
        ArgumentCaptor<KafkaSpoutMessageId> emittedMessageId = ArgumentCaptor.forClass(KafkaSpoutMessageId.class);
        verify(collectorMock).emit(anyString(), anyList(), emittedMessageId.capture());
        spout.ack(emittedMessageId.getValue());

        //Rebalance by making the partitioner return only the first partition
        when(partitionerMock.partition(any(), any()))
            .thenAnswer(invocation -> {
                invocation.getArgument(0);
                return null;
            });
        consumerRebalanceListener.onPartitionsRevoked(assignedPartitions);
        consumerRebalanceListener.onPartitionsAssigned(Collections.singleton(assignedPartition));
        when(consumerMock.assignment()).thenReturn(Collections.singleton(assignedPartition));

        //Verify that exactly the tuples belonging to the assigned partition are emitted
        for (int i = 0; i < 10 * 2; i++) {
            spout.nextTuple();
        }
        verify(collectorMock, times(10)).emit(anyString(), anyList(), emittedMessageId.capture());
        for (KafkaSpoutMessageId msgId : emittedMessageId.getAllValues()) {
            assertThat(msgId.partition(), is(assignedPartition.partition()));
        }
    }

}
