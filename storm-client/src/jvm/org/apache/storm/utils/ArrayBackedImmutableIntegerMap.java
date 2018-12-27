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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.apache.storm.utils.ArrayBackedImmutableIntegerMap.Entry;

public class ArrayBackedImmutableIntegerMap<T> implements Iterable<Entry<T>> {

    private final ArrayList<T> elements;
    private final int startId;

    public ArrayBackedImmutableIntegerMap(Map<Integer, T> map) {
        this.startId = map.keySet().stream()
            .min(Integer::compareTo)
            .orElse(Integer.MAX_VALUE);
        this.elements = Utils.convertToArray(map, startId);
    }

    public T get(int id) {
        return elements.get(id - startId);
    }
    
    @Override
    public Iterator<Entry<T>> iterator() {
        return new Iterator<Entry<T>>() {
            int curr = 0;
            
            @Override
            public boolean hasNext() {
                return elements.size() > curr;
            }

            @Override
            public Entry<T> next() {
                Entry<T> entry = new Entry<>(elements.get(curr), curr + startId);
                ++curr;
                return entry;
            }
        };
    }
    
    public static class Entry<T> {
        private final T element;
        private final int id;

        public Entry(T element, int id) {
            this.element = element;
            this.id = id;
        }

        public T getElement() {
            return element;
        }

        public int getId() {
            return id;
        }
    }
}
