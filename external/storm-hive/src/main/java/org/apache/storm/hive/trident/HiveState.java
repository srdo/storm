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

package org.apache.storm.hive.trident;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.storm.hive.common.HiveOptions;
import org.apache.storm.hive.common.HiveUtils;
import org.apache.storm.hive.common.HiveWriter;
import org.apache.storm.hive.common.HiveWriterPool;
import org.apache.storm.hive.common.PartitionValues;
import org.apache.storm.task.IMetricsContext;
import org.apache.storm.topology.FailedException;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.state.State;
import org.apache.storm.trident.tuple.TridentTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveState implements State {
    private static final Logger LOG = LoggerFactory.getLogger(HiveState.class);
    private final HiveOptions options;
    private transient Integer currentBatchSize;
    private transient ExecutorService callTimeoutPool;
    private transient HiveWriterPool hiveWriterPool;

    public HiveState(HiveOptions options) {
        this.options = options;
    }


    @Override
    public void beginCommit(Long txId) {
    }

    @Override
    public void commit(Long txId) {
        try {
            hiveWriterPool.flushAllWriters();
            currentBatchSize = 0;
        } catch (HiveWriter.TxnFailure | InterruptedException | HiveWriter.CommitFailure ex) {
            LOG.warn("Commit failed. Failing the batch.", ex);
            throw new FailedException(ex);
        }
    }

    public void prepare(Map<String, Object> conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
        this.currentBatchSize = 0;
        try {
            try {
                //HiveWriter will automatically use logged in user at the time the connection is created.
                //Log in before creating connections.
                HiveUtils.authenticate(HiveUtils.isTokenAuthEnabled(conf), options.getKerberosKeytab(), options.getKerberosPrincipal());
            } catch (HiveUtils.AuthenticationFailed ex) {
                LOG.error("Hive kerberos authentication failed " + ex.getMessage(), ex);
                throw new IllegalArgumentException(ex);
            }

            String timeoutName = "hive-bolt-%d";
            this.callTimeoutPool = Executors.newFixedThreadPool(1,
                                                                new ThreadFactoryBuilder().setNameFormat(timeoutName).build());
            this.hiveWriterPool = new HiveWriterPool(options, callTimeoutPool);
        } catch (Exception e) {
            LOG.warn("unable to make connection to hive ", e);
        }
    }

    public void updateState(List<TridentTuple> tuples, TridentCollector collector) {
        try {
            writeTuples(tuples);
        } catch (Exception e) {
            hiveWriterPool.abortAndCloseWriters();
            LOG.warn("hive streaming failed.", e);
            throw new FailedException(e);
        }
    }

    private void writeTuples(List<TridentTuple> tuples)
        throws Exception {
        for (TridentTuple tuple : tuples) {
            PartitionValues partitionVals = new PartitionValues(options.getMapper().mapPartitions(tuple));
            HiveWriter writer = hiveWriterPool.getOrCreateWriter(partitionVals);
            writer.write(options.getMapper().mapRecord(tuple));
            currentBatchSize++;
            if (currentBatchSize >= options.getBatchSize()) {
                hiveWriterPool.flushAllWriters();
                currentBatchSize = 0;
            }
        }
    }

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
    }

}
