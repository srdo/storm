/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.storm.executor.bolt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.storm.shade.com.google.common.collect.ConcurrentHashMultiset;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;

/**
 * When automatic timeout reset is enabled for tuples that are pending in bolt.execute,
 * this class is used to track which anchors might need resetting.
 */
public class ActiveAnchorIdTracker {

    private final ConcurrentHashMultiset<Long> activeAnchorIds;
    private final boolean enableAutoTimeoutResetInExecute;

    public ActiveAnchorIdTracker(boolean enableAutoTimeoutResetInExecute) {
        this.enableAutoTimeoutResetInExecute = enableAutoTimeoutResetInExecute;
        if (enableAutoTimeoutResetInExecute) {
            activeAnchorIds = ConcurrentHashMultiset.create();
        } else {
            activeAnchorIds = null;
        }
    }
    
    private void update(Tuple tuple, Consumer<Long> anchorConsumer) {
        if (!Utils.isSystemId(tuple.getSourceStreamId())) {
            for (Long anchor : tuple.getMessageId().getAnchors()) {
                anchorConsumer.accept(anchor);
            }
        }
    }
    
    /**
     * Adds the anchors of the given tuple to the set tracked by this instance.
     */
    public void track(Tuple tuple) {
        if (enableAutoTimeoutResetInExecute) {
            update(tuple, activeAnchorIds::add);
        }
    }
    
    /**
     * Removes the anchors of the given tuple from the set tracked by this instance.
     * Note that since tuples may share anchors, some of the given tuple's anchor ids may still be present after this call.
     */
    public void complete(Tuple tuple) {
        if (enableAutoTimeoutResetInExecute) {
            update(tuple, activeAnchorIds::remove);
        }
    }
    
    /**
     * Get the anchors for tuples that have been tracked, but not yet completed.
     */
    public Set<Long> getActiveAnchors() {
        if (enableAutoTimeoutResetInExecute) {
            return new HashSet<>(activeAnchorIds.elementSet());
        }
        return Collections.emptySet();
    }
    
}
