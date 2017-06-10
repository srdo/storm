/*
 * Copyright 2017 The Apache Software Foundation.
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

package org.apache.storm.kafka.spout.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.NoSuchElementException;
import org.apache.kafka.common.TopicPartition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OffsetManagerTest {

    @Rule
    public ExpectedException expect = ExpectedException.none();
    
    @Test
    public void getNthUncommittedOffsetAfterCommittedOffset() {
    
        TopicPartition tp = new TopicPartition("test", 0);
        OffsetManager manager = new OffsetManager(tp, 0);
        
        manager.addToEmitMsgs(1);
        manager.addToEmitMsgs(2);
        manager.addToEmitMsgs(5);
        manager.addToEmitMsgs(30);
        
        assertThat("The third uncommitted offset should be 5", manager.getNthUncommittedOffsetAfterCommittedOffset(3), is(5L));
        assertThat("The fourth uncommitted offset should be 30", manager.getNthUncommittedOffsetAfterCommittedOffset(4), is(30L));
        
        expect.expect(NoSuchElementException.class);
        manager.getNthUncommittedOffsetAfterCommittedOffset(5);
        
    }
    
}
