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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.storm.Config;
import org.apache.storm.hive.common.HiveOptions;
import org.apache.storm.hive.common.HiveUtils;
import org.apache.storm.hive.common.HiveWriter;
import org.apache.storm.hive.common.HiveWriterPool;
import org.apache.storm.hive.common.HiveWriterPoolFactory;
import org.apache.storm.hive.common.PartitionValues;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.BatchHelper;
import org.apache.storm.utils.TupleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(HiveBolt.class);
    private final HiveOptions options;
    private final HiveWriterPoolFactory hiveWriterPoolFactory;
    private transient OutputCollector collector;
    private transient ExecutorService callTimeoutPool;
    private transient BatchHelper batchHelper;
    private transient HiveWriterPool hiveWriterPool;

    public HiveBolt(HiveOptions options) {
        this(options, HiveWriterPool::new);
    }
    
    @VisibleForTesting
    HiveBolt(HiveOptions options, HiveWriterPoolFactory hiveWriterPoolFactory) {
        this.options = options;
        this.hiveWriterPoolFactory = hiveWriterPoolFactory;
    }

    @Override
    public void prepare(Map<String, Object> conf, TopologyContext topologyContext, OutputCollector collector) {
        try {
            try {
                //HiveWriter will automatically use logged in user at the time the connection is created.
                //Log in before creating connections.
                HiveUtils.authenticate(HiveUtils.isTokenAuthEnabled(conf), options.getKerberosKeytab(), options.getKerberosPrincipal());
            } catch (HiveUtils.AuthenticationFailed ex) {
                LOG.error("Hive kerberos authentication failed " + ex.getMessage(), ex);
                throw new IllegalArgumentException(ex);
            }

            this.collector = collector;
            this.batchHelper = new BatchHelper(options.getBatchSize(), collector);
            String timeoutName = "hive-bolt-%d";
            this.callTimeoutPool = Executors.newFixedThreadPool(1,
                                                                new ThreadFactoryBuilder().setNameFormat(timeoutName).build());
            this.hiveWriterPool = hiveWriterPoolFactory.create(options, callTimeoutPool);
        } catch (Exception e) {
            LOG.warn("unable to make connection to hive ", e);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        try {
            if (batchHelper.shouldHandle(tuple)) {
                PartitionValues partitionVals = new PartitionValues(options.getMapper().mapPartitions(tuple));
                HiveWriter writer = hiveWriterPool.getOrCreateWriter(partitionVals);
                writer.write(options.getMapper().mapRecord(tuple));
                batchHelper.addBatch(tuple);
            }

            if (batchHelper.shouldFlush()) {
                hiveWriterPool.flushAllWriters();
                LOG.info("acknowledging tuples after writers flushed ");
                batchHelper.ack();
            }
            if (TupleUtils.isTick(tuple)) {
                hiveWriterPool.retireIdleWriters();
            }
        } catch (HiveWriter.SerializationFailure sf) {
            LOG.info("Serialization exception occurred, tuple is acknowledged but not written to Hive.", tuple);
            this.collector.reportError(sf);
            collector.ack(tuple);
        } catch (Exception e) {
            batchHelper.fail(e);
            hiveWriterPool.abortAndCloseWriters();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public void cleanup() {
        hiveWriterPool.flushAndCloseAllWriters();
        ExecutorService[] toShutdown = { callTimeoutPool };
        for (ExecutorService execService : toShutdown) {
            execService.shutdown();
            try {
                while (!execService.isTerminated()) {
                    execService.awaitTermination(
                        options.getCallTimeOut(), TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException ex) {
                LOG.warn("shutdown interrupted on " + execService, ex);
            }
        }

        callTimeoutPool = null;
        super.cleanup();
        LOG.info("Hive Bolt stopped");
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> conf = super.getComponentConfiguration();
        if (conf == null) {
            conf = new Config();
        }

        if (options.getTickTupleInterval() > 0) {
            conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, options.getTickTupleInterval());
        }

        return conf;
    }

}
