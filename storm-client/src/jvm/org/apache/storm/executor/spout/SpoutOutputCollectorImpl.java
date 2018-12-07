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

package org.apache.storm.executor.spout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.storm.daemon.Acker;
import org.apache.storm.daemon.Task;
import org.apache.storm.executor.TupleInfo;
import org.apache.storm.spout.ISpout;
import org.apache.storm.spout.ISpoutOutputCollector;
import org.apache.storm.tuple.AddressedTuple;
import org.apache.storm.tuple.MessageId;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.ArrayBackedImmutableIntegerMap;
import org.apache.storm.utils.MutableLong;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods are not thread safe. Each thread expected to have a separate instance, or else synchronize externally
 */
public class SpoutOutputCollectorImpl implements ISpoutOutputCollector {
    private static final Logger LOG = LoggerFactory.getLogger(SpoutOutputCollectorImpl.class);
    private final SpoutExecutor executor;
    private final Task taskData;
    private final int taskId;
    private final MutableLong emittedCount;
    private final boolean hasAckers;
    private final Random random;
    private final Boolean isEventLoggers;
    private final Boolean isDebug;
    private final Map<Long, TupleInfo> pending;
    private final ArrayBackedImmutableIntegerMap<Set<Long>> ackerTaskToTupleRootId;
    private final long spoutExecutorThdId;
    private final TupleInfo globalTupleInfo = new TupleInfo();
        // thread safety: assumes Collector.emit*() calls are externally synchronized (if needed).

    @SuppressWarnings("unused")
    public SpoutOutputCollectorImpl(ISpout spout, SpoutExecutor executor, Task taskData,
                                    MutableLong emittedCount, boolean hasAckers, Random random,
                                    Boolean isEventLoggers, Boolean isDebug, Map<Long, TupleInfo> pending,
                                    ArrayBackedImmutableIntegerMap<Set<Long>> ackerTaskToTupleRootId) {
        this.executor = executor;
        this.taskData = taskData;
        this.taskId = taskData.getTaskId();
        this.emittedCount = emittedCount;
        this.hasAckers = hasAckers;
        this.random = random;
        this.isEventLoggers = isEventLoggers;
        this.isDebug = isDebug;
        this.pending = pending;
        this.ackerTaskToTupleRootId = ackerTaskToTupleRootId;
        this.spoutExecutorThdId = executor.getThreadId();
    }

    @Override
    public List<Integer> emit(String streamId, List<Object> tuple, Object messageId) {
        try {
            return sendSpoutMsg(streamId, tuple, messageId, null);
        } catch (InterruptedException e) {
            LOG.warn("Spout thread interrupted during emit().");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void emitDirect(int taskId, String streamId, List<Object> tuple, Object messageId) {
        try {
            sendSpoutMsg(streamId, tuple, messageId, taskId);
        } catch (InterruptedException e) {
            LOG.warn("Spout thread interrupted during emitDirect().");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        try {
            executor.getExecutorTransfer().flush();
        } catch (InterruptedException e) {
            LOG.warn("Spout thread interrupted during flush().");
            throw new RuntimeException(e);
        }
    }


    @Override
    public long getPendingCount() {
        return pending.size();
    }

    @Override
    public void reportError(Throwable error) {
        executor.getErrorReportingMetrics().incrReportedErrorCount();
        executor.getReportError().report(error);
    }

    private List<Integer> sendSpoutMsg(String stream, List<Object> values, Object messageId, Integer outTaskId) throws
        InterruptedException {
        emittedCount.increment();

        List<Integer> outTasks;
        if (outTaskId != null) {
            outTasks = taskData.getOutgoingTasks(outTaskId, stream, values);
        } else {
            outTasks = taskData.getOutgoingTasks(stream, values);
        }

        final boolean needAck = (messageId != null) && hasAckers;

        final List<Long> ackSeq = needAck ? new ArrayList<>() : null;

        final long rootId = needAck ? MessageId.generateId(random) : 0;

        for (int i = 0; i < outTasks.size(); i++) { // perf critical path. don't use iterators.
            Integer t = outTasks.get(i);
            MessageId msgId;
            if (needAck) {
                long as = MessageId.generateId(random);
                msgId = MessageId.makeRootId(rootId, as);
                ackSeq.add(as);
            } else {
                msgId = MessageId.makeUnanchored();
            }

            final TupleImpl tuple =
                new TupleImpl(executor.getWorkerTopologyContext(), values, executor.getComponentId(), this.taskId, stream, msgId);
            AddressedTuple adrTuple = new AddressedTuple(t, tuple);
            executor.getExecutorTransfer().tryTransfer(adrTuple, executor.getPendingEmits());
        }
        if (isEventLoggers) {
            taskData.sendToEventLogger(executor, values, executor.getComponentId(), messageId, random, executor.getPendingEmits());
        }

        if (needAck) {
            boolean sample = executor.samplerCheck();
            TupleInfo info = new TupleInfo();
            info.setTaskId(this.taskId);
            info.setStream(stream);
            info.setMessageId(messageId);
            if (isDebug) {
                info.setValues(values);
            }
            if (sample) {
                info.setTimestamp(System.currentTimeMillis());
            }

            pending.put(rootId, info);
            List<Object> ackInitValues = new Values(rootId, Utils.bitXorVals(ackSeq), this.taskId);
            Tuple ackInitTuple = taskData.getTuple(Acker.ACKER_INIT_STREAM_ID, ackInitValues);
            List<Integer> ackerTaskIds = taskData.getOutgoingTasks(Acker.ACKER_INIT_STREAM_ID, ackInitValues);
            taskData.sendUnanchored(ackerTaskIds, ackInitTuple, executor.getExecutorTransfer(), executor.getPendingEmits());
            //Ack init stream uses field grouping, so exactly one acker task will be sent the init
            ackerTaskToTupleRootId.get(ackerTaskIds.get(0)).add(rootId);
        } else if (messageId != null) {
            // Reusing TupleInfo object as we directly call executor.ackSpoutMsg() & are not sending msgs. perf critical
            if (isDebug) {
                if (spoutExecutorThdId != Thread.currentThread().getId()) {
                    throw new RuntimeException("Detected background thread emitting tuples for the spout. " +
                                               "Spout Output Collector should only emit from the main spout executor thread.");
                }
            }
            globalTupleInfo.clear();
            globalTupleInfo.setStream(stream);
            globalTupleInfo.setValues(values);
            globalTupleInfo.setMessageId(messageId);
            globalTupleInfo.setTimestamp(0);
            globalTupleInfo.setId("0:");
            Long timeDelta = 0L;
            executor.ackSpoutMsg(executor, taskData, timeDelta, globalTupleInfo);
        }
        return outTasks;
    }
}
