/*
 * Copyright 2018 The Apache Software Foundation.
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

package org.apache.storm.utils;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.apache.storm.utils.ArrayBackedImmutableIntegerMap.Entry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayBackedImmutableIntegerMapTest {

    Map<Integer, Integer> testValues = new HashMap<>();
    ArrayBackedImmutableIntegerMap<Integer> mapToTest;
    
    @BeforeEach
    public void setUp() {
        testValues.put(5, 10);
        testValues.put(6, 15);
        testValues.put(3, 11);
        mapToTest = new ArrayBackedImmutableIntegerMap<>(testValues);
    }
    
    @Test
    public void testGetValues() {  
        assertThat(mapToTest.get(3), is(11));
        assertThat(mapToTest.get(5), is(10));
        assertThat(mapToTest.get(6), is(15));
        assertThat(mapToTest.get(4), nullValue());
    }
    
    @Test
    public void testGetOutOfRange() {        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
           mapToTest.get(50);
        });
    }
    
    @Test
    public void testIteration() {
        Map<Integer, Integer> actualValues = new HashMap<>();
        
        for(Entry<Integer> entry : mapToTest) {
            if (entry.getElement() != null) {
                actualValues.put(entry.getId(), entry.getElement());
            }
        }
        
        assertThat(actualValues, is(testValues));
    }
    
}
