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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.hive.streaming.RecordWriter;
import org.apache.hive.streaming.SerializationError;
import org.apache.hive.streaming.StreamingConnection;
import org.apache.hive.streaming.StreamingException;
import org.apache.storm.hive.bolt.mapper.HiveMapper;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HiveWriter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory
        .getLogger(HiveWriter.class);

    private final StreamingConnection connection;
    private final ExecutorService callTimeoutPool;
    private final long callTimeout;
    private final Object txnBatchLock = new Object();
    protected boolean closed; // flag indicating HiveWriter was closed
    private long lastUsed; // time of last flush on this writer
    private int totalRecords = 0;

    public HiveWriter(PartitionValues partitionVals, HiveOptions options,
        ExecutorService callTimeoutPool) throws InterruptedException, ConnectFailure {
        this(partitionVals, options, callTimeoutPool, HiveUtils::makeConnection);
    }
    
    public HiveWriter(PartitionValues partitionVals, HiveOptions options,
        ExecutorService callTimeoutPool, StreamingConnectionFactory connectionFactory)
        throws InterruptedException, ConnectFailure {
        this.callTimeout = options.getCallTimeOut();
        this.callTimeoutPool = callTimeoutPool;
        this.connection = newConnection(partitionVals, options, connectionFactory);
        try {
            nextTxn();
            this.closed = false;
            this.lastUsed = System.currentTimeMillis();
        } catch (InterruptedException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectFailure(this.connection, e);
        }
    }

    public final RecordWriter getRecordWriter(final HiveMapper mapper) {
        return mapper.createRecordWriter();
    }

    @Override
    public String toString() {
        return "{ "
               + "Connection = " + connection.toString() + " }";
    }

    /**
     * Write data.
     */
    public synchronized void write(final byte[] record)
        throws WriteFailure, SerializationFailure, InterruptedException {
        if (closed) {
            throw new IllegalStateException("This hive streaming writer was closed "
                    + "and thus no longer able to write : "
                    + connection);
        }
        // write the tuple
        try {
            LOG.debug("Writing event to {}", connection);
            callWithTimeout(new CallRunner<Void>() {
                @Override
                public Void call() throws StreamingException, InterruptedException {
                    connection.write(record);
                    totalRecords++;
                    return null;
                }
            });
        } catch (SerializationError se) {
            throw new SerializationFailure(connection, se);
        } catch (StreamingException | TimeoutException e) {
            throw new WriteFailure(connection, e);
        }
    }

    /**
     * Commits the current Txn if totalRecordsPerTransaction > 0 .
     * If 'rollToNext' is true, will begin a new transaction after committing the current transaction.
     */
    public void flush(boolean rollToNext)
        throws CommitFailure, TxnFailure, InterruptedException {
        // if there are no records do not call flush
        if (totalRecords <= 0) {
            return;
        }
        try {
            synchronized (txnBatchLock) {
                commitTxn();
                if (rollToNext) {
                    nextTxn();
                }
                totalRecords = 0;
                lastUsed = System.currentTimeMillis();
            }
        } catch (StreamingException e) {
            throw new TxnFailure(connection, e);
        }
    }

    /**
     * returns totalRecords written so far in a transaction.
     */
    public int getTotalRecords() {
        return totalRecords;
    }

    /**
     * Flush and Close current transactionBatch.
     */
    public void flushAndClose() throws TxnFailure, CommitFailure, InterruptedException {
        flush(false);
        close();
    }

    @Override
    public void close() throws InterruptedException {
        closeConnection();
        closed = true;
    }

    private void closeConnection() throws InterruptedException {
        LOG.info("Closing connection: {}", connection);
        try {
            callWithTimeout(new CallRunner<Void>() {
                @Override
                public Void call() throws Exception {
                    connection.close(); // could block
                    return null;
                }
            });
        } catch (Exception e) {
            LOG.warn("Error closing connection: " + connection, e);
            // Suppressing exceptions as we don't care for errors on connection close
        }
    }

    private void commitTxn() throws CommitFailure, InterruptedException {
        LOG.debug("Committing to {}", connection);
        try {
            callWithTimeout(new CallRunner<Void>() {
                @Override
                public Void call() throws Exception {
                    connection.commitTransaction(); // could block
                    return null;
                }
            });
        } catch (StreamingException e) {
            throw new CommitFailure(connection, e);
        } catch (TimeoutException e) {
            throw new CommitFailure(connection, e);
        }
    }

    private StreamingConnection newConnection(final PartitionValues partitionVals, final HiveOptions options,
        final StreamingConnectionFactory connectionFactory)
        throws InterruptedException, ConnectFailure {
        try {
            LOG.debug(String.format("Creating connection. MetastoreURI: %s, database: %s table: %s, partitionVals: %s",
                options.getMetaStoreURI(), options.getDatabaseName(), options.getTableName(), partitionVals));
            return callWithTimeout(new CallRunner<StreamingConnection>() {
                @Override
                public StreamingConnection call() throws Exception {
                    StreamingConnection connection = connectionFactory.create(partitionVals, options);
                    if (options.getAutoCreatePartitions()) {
                        connection.createPartitionIfNotExists(partitionVals.getPartitionValues());
                    }
                    return connection;
                }
            });
        } catch (StreamingException | TimeoutException e) {
            throw new ConnectFailure(
                String.format("Failed to create connection. MetastoreURI: %s, database: %s table: %s, partitionVals: %s",
                options.getMetaStoreURI(), options.getDatabaseName(), options.getTableName(), partitionVals), e);
        }
    }

    /**
     * Aborts the current Txn and switches to next Txn.
     * @throws StreamingException if could not switch to next Txn
     */
    public void abort() throws StreamingException, InterruptedException {
        synchronized (txnBatchLock) {
            abortTxn();
            nextTxn();
        }
    }

    /**
     * Aborts current Txn.
     */
    private void abortTxn() throws InterruptedException {
        LOG.info("Aborting transaction on connection {}", connection);
        try {
            callWithTimeout(new CallRunner<Void>() {
                @Override
                public Void call() throws StreamingException, InterruptedException {
                    connection.abortTransaction(); // could block
                    return null;
                }
            });
        } catch (InterruptedException e) {
            throw e;
        } catch (TimeoutException e) {
            LOG.warn("Timeout while aborting transaction on connection " + connection, e);
        } catch (Exception e) {
            LOG.warn("Error aborting transaction on connection " + connection, e);
            // Suppressing exceptions as we don't care for errors on abort
        }
    }

    /**
     * Begins the next transaction.
     */
    private void nextTxn() throws StreamingException, InterruptedException {
        LOG.debug("Switching to next Txn for {}", connection);
        connection.beginTransaction();
    }

    /**
     * Execute the callable on a separate thread and wait for the completion
     * for the specified amount of time in milliseconds. In case of timeout
     * cancel the callable and throw an IOException
     */
    private <T> T callWithTimeout(final CallRunner<T> callRunner)
        throws TimeoutException, StreamingException, InterruptedException {
        Future<T> future = callTimeoutPool.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return callRunner.call();
            }
        });
        try {
            if (callTimeout > 0) {
                return future.get(callTimeout, TimeUnit.MILLISECONDS);
            } else {
                return future.get();
            }
        } catch (TimeoutException timeoutException) {
            future.cancel(true);
            throw timeoutException;
        } catch (ExecutionException e1) {
            Throwable cause = e1.getCause();
            if (cause instanceof StreamingException) {
                throw (StreamingException) cause;
            } else if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof TimeoutException) {
                throw new StreamingException("Operation Timed Out.", (TimeoutException) cause);
            } else {
                throw new RuntimeException(e1);
            }
        }
    }

    public long getLastUsed() {
        return lastUsed;
    }

    private byte[] generateRecord(Tuple tuple) {
        StringBuilder buf = new StringBuilder();
        for (Object o : tuple.getValues()) {
            buf.append(o);
            buf.append(",");
        }
        return buf.toString().getBytes();
    }

    /**
     * Simple interface whose <tt>call</tt> method is called by
     * {#callWithTimeout} in a new thread inside a
     * {@linkplain java.security.PrivilegedExceptionAction#run()} call.
     */
    private interface CallRunner<T> {
        T call() throws Exception;
    }

    public static class Failure extends Exception {
        public Failure(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class WriteFailure extends Failure {
        public WriteFailure(StreamingConnection connection, Throwable cause) {
            super("Failed writing to : " + connection, cause);
        }
    }

    public static class CommitFailure extends Failure {
        public CommitFailure(StreamingConnection connection, Throwable cause) {
            super("Commit failed on EndPoint: " + connection, cause);
        }
    }

    public static class ConnectFailure extends Failure {
        public ConnectFailure(StreamingConnection connection, Throwable cause) {
            super("Failed connecting to EndPoint " + connection, cause);
        }
        
        public ConnectFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TxnFailure extends Failure {
        public TxnFailure(StreamingConnection connection, Throwable cause) {
            super("Failed switching to next Txn on connection " + connection, cause);
        }
    }
    
    public static class SerializationFailure extends Failure {

        public SerializationFailure(StreamingConnection connection, Throwable cause) {
            super(connection.toString(), cause);
        }
        
    }
}
