
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.starter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.testing.TestWordSpout;
import org.apache.storm.topology.ConfigurableTopology;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a basic example of a Storm topology.
 */
public class ExclamationTopology extends ConfigurableTopology {

    public static class ToJsonBolt extends BaseRichBolt {

        private OutputCollector collector;

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("json"));
        }

        @Override
        public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(Tuple input) {
            String word = input.getStringByField("word");
            JSONObject json = new JSONObject();
            json.put("word", word);
            collector.emit(new Values(json));
        }
    }

    public static class FromJsonBolt extends BaseRichBolt {

        private final Logger logger = LoggerFactory.getLogger(FromJsonBolt.class);

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }

        @Override
        public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        }

        @Override
        public void execute(Tuple input) {
            JSONObject json = (JSONObject) input.getValueByField("json");
            logger.info("Received some json {}", json.toJSONString());
        }

    }

    public static class JSONObjectSerializer extends Serializer<JSONObject> {

        @Override
        public void write(Kryo kryo, Output output, JSONObject t) {
            output.writeString(t.toJSONString());
        }

        @Override
        public JSONObject read(Kryo kryo, Input input, Class<JSONObject> type) {
            try {
                return (JSONObject) JSONValue.parseWithException(input.readString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new ExclamationTopology(), args);
    }

    protected int run(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("word", new TestWordSpout(), 10);
        builder.setBolt("toJson", new ToJsonBolt(), 10).shuffleGrouping("word");
        builder.setBolt("fromJson", new FromJsonBolt(), 10).shuffleGrouping("toJson");

        conf.setDebug(true);
        conf.setFallBackOnJavaSerialization(false);
        
        conf.registerSerialization(JSONObject.class);
        //Uncomment to use to-string serialization
        //conf.registerSerialization(JSONObject.class, JSONObjectSerializer.class);

        String topologyName = "test";

        conf.setNumWorkers(3);

        if (args != null && args.length > 0) {
            topologyName = args[0];
        }

        return submit(topologyName, conf, builder);
    }
}
