/*
 * Copyright 2018 The Apache Software Foundation.
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

package org.apache.storm.kafka.trident;

import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.FailedException;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class DemoTopo {

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        KafkaSpoutConfig<String, String> config = KafkaSpoutConfig.builder("localhost:9092", "demoTopic")
            .setProp(ConsumerConfig.GROUP_ID_CONFIG, "grp")
            .setProp(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer")
            .setOffsetCommitPeriodMs(100)
            .setRetry(new KafkaSpoutRetryExponentialBackoff(
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(100),
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(100),
                10,
                KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(3000)
            ))
            .setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST)
            .setMaxUncommittedOffsets(10000)
            .build();
        
        KafkaSpout<String, String> spout = new KafkaSpout<>(config);
        
        builder.setSpout("spout", spout, 10).setNumTasks(10);
        builder.setBolt("boltA", new BoltA(), 5)
            .shuffleGrouping("spout")
            .setNumTasks(5);
        builder.setBolt("boltB", new BoltB(), 200)
            .shuffleGrouping("boltA")
            .setNumTasks(200);
        
        Config topoConf = new Config();
        topoConf.setNumWorkers(4);
        topoConf.setMessageTimeoutSecs(300);
        
        StormSubmitter.submitTopology("saurabh-demo", topoConf, builder.createTopology());
    }
    
    private static class BoltA extends BaseBasicBolt {

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("index", "rand"));
        }

        @Override
        public void execute(Tuple input, BasicOutputCollector collector) {
            System.out.println("Received " + input.getLongByField("offset") + " in bolt A");
            boolean rand = ThreadLocalRandom.current().nextBoolean();
            for (int i = 1; i < 101; i++) {
                collector.emit(new Values(i, rand));
            }
        }
        
    }
    
    private static class BoltB extends BaseBasicBolt {

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }

        @Override
        public void execute(Tuple input, BasicOutputCollector collector) {
            int index = input.getIntegerByField("index");
            boolean rand = input.getBooleanByField("rand");
            
            System.out.println("Received " + index +"th tuple in BoltB");
            if (index > 97 && rand) {
                System.out.println("Throwing FailedException for " + index +"th tuple");
                throw new FailedException();
            }
        }
        
    }

}
