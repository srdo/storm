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
package org.apache.storm.localizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDownloadedResource {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDownloadedResource.class);
    private static class NoCancelFuture<T> implements Future<T> {
        private final Future<T> _wrapped;
        
        public NoCancelFuture(Future<T> wrapped) {
            _wrapped = wrapped;
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            //cancel not currently supported
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return _wrapped.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return _wrapped.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return _wrapped.get(timeout, unit);
        }
    }
    private final Future<Void> _pending;
    private final Set<Integer> _ports;
    private boolean _isDone;
    
    
    public LocalDownloadedResource(Future<Void> pending) {
        _pending = new NoCancelFuture<>(pending);
        _ports = new HashSet<>();
        _isDone = false;
    }

    public synchronized Future<Void> reserve(int port) {
        if (!_ports.add(port)) {
            LOG.warn("PORT {} already reserved {} for this topology", port, _ports);
        }
        return _pending;
    }
    
    /**
     * Release a port from the reference count, and update isDone if all is done.
     * @param port the port to release
     * @return true if the port was being counted else false
     */
    public synchronized boolean release(int port) {
        boolean ret = _ports.remove(port);
        if (ret && _ports.isEmpty()) {
            _isDone = true;
        }
        return ret;
    }
    
    public synchronized boolean isDone() {
        return _isDone;
    }

    /**
     * @return the ports this is still waiting to release
     */
    public Set<Integer> getPorts() {
        return Collections.unmodifiableSet(_ports);
    }
}