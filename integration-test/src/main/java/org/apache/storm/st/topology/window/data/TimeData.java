/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.st.topology.window.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Collection;

public class TimeData implements Comparable<TimeData> {
    public static final TimeData CLS = new TimeData(-1);
    private static final String NUMBER_FIELD_NAME = "number";
    private static final String STRING_FIELD_NAME = "dateAsStr";
    private static final String TIMESTAMP_FIELD_NAME = "date";
    static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
    private final int num;
    private final Instant now;

    private TimeData(int num) {
        this(num, Instant.now());
    }

    private TimeData(int num, Instant date) {
        this.num = num;
        this.now = date;
    }

    public static TimeData newData(int num) {
        return new TimeData(num);
    }

    public static TimeData fromTuple(Tuple tuple) {
        return new TimeData(tuple.getIntegerByField(NUMBER_FIELD_NAME), Instant.ofEpochMilli(tuple.getLongByField(TIMESTAMP_FIELD_NAME)));
    }

    public static TimeData fromJson(String jsonStr) {
        return GSON.fromJson(jsonStr, TimeData.class);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public static String toString(Collection<TimeData> elements) {
        return GSON.toJson(elements);
    }

    public Values getValues() {
        return new Values(num, now.toString(), now.toEpochMilli());
    }

    public static String getTimestampFieldName() {
        return TIMESTAMP_FIELD_NAME;
    }

    public Instant getDate() {
        return now;
    }

    public static Fields getFields() {
        return new Fields(NUMBER_FIELD_NAME, STRING_FIELD_NAME, TIMESTAMP_FIELD_NAME);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeData data = (TimeData) o;

        if (num != data.num) return false;
        return now.equals(data.now);

    }

    @Override
    public int hashCode() {
        int result = num;
        result = 31 * result + now.hashCode();
        return result;
    }

    @Override
    public int compareTo(TimeData o) {
        return Long.compare(now.toEpochMilli(), o.now.toEpochMilli());
    }
}
