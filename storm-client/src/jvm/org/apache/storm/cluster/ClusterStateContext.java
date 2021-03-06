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

package org.apache.storm.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.storm.generated.WorkerTokenServiceType;
import org.apache.zookeeper.data.ACL;

/**
 * This class is intended to provide runtime-context to StateStorageFactory implementors, giving information such as what daemon is creating
 * it.
 */
public class ClusterStateContext {

    private final Map<String, Object> conf;
    private DaemonType daemonType;

    public ClusterStateContext() {
        daemonType = DaemonType.UNKNOWN;
        conf = new HashMap<>();
    }

    public ClusterStateContext(DaemonType daemonType, Map<String, Object> conf) {
        this.daemonType = daemonType;
        this.conf = conf;
    }

    public DaemonType getDaemonType() {
        return daemonType;
    }

    public List<ACL> getDefaultZkAcls() {
        return daemonType.getDefaultZkAcls(conf);
    }

    public List<ACL> getZkSecretAcls(WorkerTokenServiceType type) {
        return daemonType.getZkSecretAcls(type, conf);
    }
}
