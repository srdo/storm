/*
 * Copyright 2016 The Apache Software Foundation.
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
package org.apache.storm.starter.tools;

import java.util.Collections;
import org.apache.storm.Config;
import org.apache.storm.ILocalCluster;
import org.apache.storm.Testing;
import org.apache.storm.testing.CompleteTopologyParam;
import org.apache.storm.testing.FixedTupleSpout;
import org.apache.storm.testing.MkClusterParam;
import org.apache.storm.testing.MockedSources;
import org.apache.storm.testing.TestJob;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.testng.annotations.Test;

public class STORM2175 {
    
    @Test(invocationCount = 20)
    public void runSmallTopology() {
        Config daemonConf = new Config();
        daemonConf.setNumWorkers(4);
        MkClusterParam clusterParam = new MkClusterParam();
        clusterParam.setSupervisors(4);
        clusterParam.setPortsPerSupervisor(1);
        clusterParam.setDaemonConf(daemonConf);
        
        final CompleteTopologyParam completeParams = new CompleteTopologyParam();
        MockedSources mockedSources = new MockedSources();
        completeParams.setMockedSources(mockedSources);
        completeParams.setTimeoutMs(90_000);
        completeParams.setCleanupState(true);
        
        Testing.withLocalCluster(clusterParam, new TestJob() {
                @Override
                public void run(ILocalCluster cluster) throws Exception {
                    FixedTupleSpout fixedTupleSpout = new FixedTupleSpout(Collections.singletonList(new Values(0)), "field1");

                    TopologyBuilder topologyBuilder = new TopologyBuilder();
                    topologyBuilder.setSpout("source-spout", fixedTupleSpout);

                    topologyBuilder.setBolt("ackBolt", new AckBolt())
                            .shuffleGrouping("source-spout");

                    Testing.completeTopology(cluster, topologyBuilder.createTopology(), completeParams);
                }
            });
    }
    
    private static class AckBolt extends BaseBasicBolt {

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }

        @Override
        public void execute(Tuple input, BasicOutputCollector collector) {
        }

    }
}
