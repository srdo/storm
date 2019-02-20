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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.apache.hive.streaming.StreamingException;
import org.apache.storm.hive.bolt.HiveBolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveWriterPool {
    private static final Logger LOG = LoggerFactory.getLogger(HiveBolt.class);
    
    private final Map<PartitionValues, HiveWriter> allWriters = new ConcurrentHashMap<>();
    private final HiveOptions options;
    private final ExecutorService callTimeoutPool;

    public HiveWriterPool(HiveOptions options, ExecutorService callTimeoutPool) {
        this.options = options;
        this.callTimeoutPool = callTimeoutPool;
    }
    
    public HiveWriter getOrCreateWriter(PartitionValues partitionValues)
        throws HiveWriter.ConnectFailure, InterruptedException {
        try {
            HiveWriter writer = allWriters.get(partitionValues);
            if (writer == null) {
                writer = HiveUtils.makeHiveWriter(partitionValues, callTimeoutPool, options);
                if (allWriters.size() > (options.getMaxOpenConnections() - 1)) {
                    LOG.info("cached HiveEndPoint size {} exceeded maxOpenConnections {} ", allWriters.size(),
                             options.getMaxOpenConnections());
                    int retired = retireIdleWriters();
                    if (retired == 0) {
                        retireEldestWriter();
                    }
                }
                allWriters.put(partitionValues, writer);
                HiveUtils.logAllHiveEndPoints(allWriters);
            }
            return writer;
        } catch (HiveWriter.ConnectFailure e) {
            LOG.error("Failed to create HiveWriter", e);
            throw e;
        }
    }
    
    /**
     * Locate writer that has not been used for longest time and retire it.
     */
    public void retireEldestWriter() {
        LOG.info("Attempting close eldest writers");
        long oldestTimeStamp = System.currentTimeMillis();
        PartitionValues eldest = null;
        for (Map.Entry<PartitionValues, HiveWriter> entry : allWriters.entrySet()) {
            if (entry.getValue().getLastUsed() < oldestTimeStamp) {
                eldest = entry.getKey();
                oldestTimeStamp = entry.getValue().getLastUsed();
            }
        }
        LOG.info("Closing least used Writer: {}", allWriters.get(eldest));
        retire(eldest);
    }

    /**
     * Locate all writers past idle timeout and retire them.
     * @return number of writers retired
     */
    public int retireIdleWriters() {
        LOG.info("Attempting close idle writers");
        int count = 0;
        long now = System.currentTimeMillis();

        //1) Find retirement candidates
        for (Map.Entry<PartitionValues, HiveWriter> entry : allWriters.entrySet()) {
            if (now - entry.getValue().getLastUsed() > options.getIdleTimeout()) {
                ++count;
                LOG.info("Closing idle Writer: {}", entry.getValue());
                retire(entry.getKey());
            }
        }
        return count;
    }
    
    public void flushAndCloseAllWriters() {
        for (Map.Entry<PartitionValues, HiveWriter> entry : allWriters.entrySet()) {
            try {
                HiveWriter w = entry.getValue();
                w.flushAndClose();
            } catch (Exception ex) {
                LOG.warn("Error while closing writer " + entry.getValue()
                    + ". Exception follows.", ex);
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    public void flushAllWriters()
        throws HiveWriter.CommitFailure, HiveWriter.TxnFailure, InterruptedException {
        for (HiveWriter writer : allWriters.values()) {
            writer.flush(true);
        }
    }

    public void abortAndCloseWriters() {
        try {
            abortAllWriters();
            closeAllWriters();
        } catch (Exception ie) {
            LOG.warn("unable to close hive connections. ", ie);
        }
    }

    /**
     * Abort current Txn on all writers.
     */
    public void abortAllWriters() throws InterruptedException, StreamingException {
        for (HiveWriter writer : allWriters.values()) {
            try {
                writer.abort();
            } catch (Exception e) {
                LOG.error("Failed to abort hive transaction batch, writer " + writer + " due to exception ", e);
            }
        }
    }

    /**
     * Closes all writers and remove them from cache.
     */
    public void closeAllWriters() {
        //1) Retire writers
        for (HiveWriter writer : allWriters.values()) {
            try {
                writer.close();
            } catch (Exception e) {
                LOG.warn("unable to close writers. ", e);
            }
        }
        //2) Clear cache
        allWriters.clear();
    }

    private void retire(PartitionValues partitionValues) {
        HiveWriter writer = allWriters.remove(partitionValues);
        try {
            if (writer != null) {
                writer.flushAndClose();
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted when attempting to close writer: {}", writer, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.warn("Interrupted when attempting to close writer: {}", writer, e);
        }
    }
}
