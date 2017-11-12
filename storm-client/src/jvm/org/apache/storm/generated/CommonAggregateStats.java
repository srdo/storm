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
/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.storm.generated;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)")
public class CommonAggregateStats implements org.apache.thrift.TBase<CommonAggregateStats, CommonAggregateStats._Fields>, java.io.Serializable, Cloneable, Comparable<CommonAggregateStats> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CommonAggregateStats");

  private static final org.apache.thrift.protocol.TField NUM_EXECUTORS_FIELD_DESC = new org.apache.thrift.protocol.TField("num_executors", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField NUM_TASKS_FIELD_DESC = new org.apache.thrift.protocol.TField("num_tasks", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField EMITTED_FIELD_DESC = new org.apache.thrift.protocol.TField("emitted", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField TRANSFERRED_FIELD_DESC = new org.apache.thrift.protocol.TField("transferred", org.apache.thrift.protocol.TType.I64, (short)4);
  private static final org.apache.thrift.protocol.TField ACKED_FIELD_DESC = new org.apache.thrift.protocol.TField("acked", org.apache.thrift.protocol.TType.I64, (short)5);
  private static final org.apache.thrift.protocol.TField FAILED_FIELD_DESC = new org.apache.thrift.protocol.TField("failed", org.apache.thrift.protocol.TType.I64, (short)6);
  private static final org.apache.thrift.protocol.TField RESOURCES_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("resources_map", org.apache.thrift.protocol.TType.MAP, (short)7);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new CommonAggregateStatsStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new CommonAggregateStatsTupleSchemeFactory();

  private int num_executors; // optional
  private int num_tasks; // optional
  private long emitted; // optional
  private long transferred; // optional
  private long acked; // optional
  private long failed; // optional
  private java.util.Map<java.lang.String,java.lang.Double> resources_map; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NUM_EXECUTORS((short)1, "num_executors"),
    NUM_TASKS((short)2, "num_tasks"),
    EMITTED((short)3, "emitted"),
    TRANSFERRED((short)4, "transferred"),
    ACKED((short)5, "acked"),
    FAILED((short)6, "failed"),
    RESOURCES_MAP((short)7, "resources_map");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // NUM_EXECUTORS
          return NUM_EXECUTORS;
        case 2: // NUM_TASKS
          return NUM_TASKS;
        case 3: // EMITTED
          return EMITTED;
        case 4: // TRANSFERRED
          return TRANSFERRED;
        case 5: // ACKED
          return ACKED;
        case 6: // FAILED
          return FAILED;
        case 7: // RESOURCES_MAP
          return RESOURCES_MAP;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __NUM_EXECUTORS_ISSET_ID = 0;
  private static final int __NUM_TASKS_ISSET_ID = 1;
  private static final int __EMITTED_ISSET_ID = 2;
  private static final int __TRANSFERRED_ISSET_ID = 3;
  private static final int __ACKED_ISSET_ID = 4;
  private static final int __FAILED_ISSET_ID = 5;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.NUM_EXECUTORS,_Fields.NUM_TASKS,_Fields.EMITTED,_Fields.TRANSFERRED,_Fields.ACKED,_Fields.FAILED,_Fields.RESOURCES_MAP};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NUM_EXECUTORS, new org.apache.thrift.meta_data.FieldMetaData("num_executors", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.NUM_TASKS, new org.apache.thrift.meta_data.FieldMetaData("num_tasks", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.EMITTED, new org.apache.thrift.meta_data.FieldMetaData("emitted", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.TRANSFERRED, new org.apache.thrift.meta_data.FieldMetaData("transferred", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.ACKED, new org.apache.thrift.meta_data.FieldMetaData("acked", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.FAILED, new org.apache.thrift.meta_data.FieldMetaData("failed", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.RESOURCES_MAP, new org.apache.thrift.meta_data.FieldMetaData("resources_map", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CommonAggregateStats.class, metaDataMap);
  }

  public CommonAggregateStats() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CommonAggregateStats(CommonAggregateStats other) {
    __isset_bitfield = other.__isset_bitfield;
    this.num_executors = other.num_executors;
    this.num_tasks = other.num_tasks;
    this.emitted = other.emitted;
    this.transferred = other.transferred;
    this.acked = other.acked;
    this.failed = other.failed;
    if (other.is_set_resources_map()) {
      java.util.Map<java.lang.String,java.lang.Double> __this__resources_map = new java.util.HashMap<java.lang.String,java.lang.Double>(other.resources_map);
      this.resources_map = __this__resources_map;
    }
  }

  public CommonAggregateStats deepCopy() {
    return new CommonAggregateStats(this);
  }

  @Override
  public void clear() {
    set_num_executors_isSet(false);
    this.num_executors = 0;
    set_num_tasks_isSet(false);
    this.num_tasks = 0;
    set_emitted_isSet(false);
    this.emitted = 0;
    set_transferred_isSet(false);
    this.transferred = 0;
    set_acked_isSet(false);
    this.acked = 0;
    set_failed_isSet(false);
    this.failed = 0;
    this.resources_map = null;
  }

  public int get_num_executors() {
    return this.num_executors;
  }

  public void set_num_executors(int num_executors) {
    this.num_executors = num_executors;
    set_num_executors_isSet(true);
  }

  public void unset_num_executors() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __NUM_EXECUTORS_ISSET_ID);
  }

  /** Returns true if field num_executors is set (has been assigned a value) and false otherwise */
  public boolean is_set_num_executors() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __NUM_EXECUTORS_ISSET_ID);
  }

  public void set_num_executors_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __NUM_EXECUTORS_ISSET_ID, value);
  }

  public int get_num_tasks() {
    return this.num_tasks;
  }

  public void set_num_tasks(int num_tasks) {
    this.num_tasks = num_tasks;
    set_num_tasks_isSet(true);
  }

  public void unset_num_tasks() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __NUM_TASKS_ISSET_ID);
  }

  /** Returns true if field num_tasks is set (has been assigned a value) and false otherwise */
  public boolean is_set_num_tasks() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __NUM_TASKS_ISSET_ID);
  }

  public void set_num_tasks_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __NUM_TASKS_ISSET_ID, value);
  }

  public long get_emitted() {
    return this.emitted;
  }

  public void set_emitted(long emitted) {
    this.emitted = emitted;
    set_emitted_isSet(true);
  }

  public void unset_emitted() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __EMITTED_ISSET_ID);
  }

  /** Returns true if field emitted is set (has been assigned a value) and false otherwise */
  public boolean is_set_emitted() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __EMITTED_ISSET_ID);
  }

  public void set_emitted_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __EMITTED_ISSET_ID, value);
  }

  public long get_transferred() {
    return this.transferred;
  }

  public void set_transferred(long transferred) {
    this.transferred = transferred;
    set_transferred_isSet(true);
  }

  public void unset_transferred() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TRANSFERRED_ISSET_ID);
  }

  /** Returns true if field transferred is set (has been assigned a value) and false otherwise */
  public boolean is_set_transferred() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TRANSFERRED_ISSET_ID);
  }

  public void set_transferred_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TRANSFERRED_ISSET_ID, value);
  }

  public long get_acked() {
    return this.acked;
  }

  public void set_acked(long acked) {
    this.acked = acked;
    set_acked_isSet(true);
  }

  public void unset_acked() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __ACKED_ISSET_ID);
  }

  /** Returns true if field acked is set (has been assigned a value) and false otherwise */
  public boolean is_set_acked() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __ACKED_ISSET_ID);
  }

  public void set_acked_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __ACKED_ISSET_ID, value);
  }

  public long get_failed() {
    return this.failed;
  }

  public void set_failed(long failed) {
    this.failed = failed;
    set_failed_isSet(true);
  }

  public void unset_failed() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __FAILED_ISSET_ID);
  }

  /** Returns true if field failed is set (has been assigned a value) and false otherwise */
  public boolean is_set_failed() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __FAILED_ISSET_ID);
  }

  public void set_failed_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __FAILED_ISSET_ID, value);
  }

  public int get_resources_map_size() {
    return (this.resources_map == null) ? 0 : this.resources_map.size();
  }

  public void put_to_resources_map(java.lang.String key, double val) {
    if (this.resources_map == null) {
      this.resources_map = new java.util.HashMap<java.lang.String,java.lang.Double>();
    }
    this.resources_map.put(key, val);
  }

  public java.util.Map<java.lang.String,java.lang.Double> get_resources_map() {
    return this.resources_map;
  }

  public void set_resources_map(java.util.Map<java.lang.String,java.lang.Double> resources_map) {
    this.resources_map = resources_map;
  }

  public void unset_resources_map() {
    this.resources_map = null;
  }

  /** Returns true if field resources_map is set (has been assigned a value) and false otherwise */
  public boolean is_set_resources_map() {
    return this.resources_map != null;
  }

  public void set_resources_map_isSet(boolean value) {
    if (!value) {
      this.resources_map = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case NUM_EXECUTORS:
      if (value == null) {
        unset_num_executors();
      } else {
        set_num_executors((java.lang.Integer)value);
      }
      break;

    case NUM_TASKS:
      if (value == null) {
        unset_num_tasks();
      } else {
        set_num_tasks((java.lang.Integer)value);
      }
      break;

    case EMITTED:
      if (value == null) {
        unset_emitted();
      } else {
        set_emitted((java.lang.Long)value);
      }
      break;

    case TRANSFERRED:
      if (value == null) {
        unset_transferred();
      } else {
        set_transferred((java.lang.Long)value);
      }
      break;

    case ACKED:
      if (value == null) {
        unset_acked();
      } else {
        set_acked((java.lang.Long)value);
      }
      break;

    case FAILED:
      if (value == null) {
        unset_failed();
      } else {
        set_failed((java.lang.Long)value);
      }
      break;

    case RESOURCES_MAP:
      if (value == null) {
        unset_resources_map();
      } else {
        set_resources_map((java.util.Map<java.lang.String,java.lang.Double>)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case NUM_EXECUTORS:
      return get_num_executors();

    case NUM_TASKS:
      return get_num_tasks();

    case EMITTED:
      return get_emitted();

    case TRANSFERRED:
      return get_transferred();

    case ACKED:
      return get_acked();

    case FAILED:
      return get_failed();

    case RESOURCES_MAP:
      return get_resources_map();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case NUM_EXECUTORS:
      return is_set_num_executors();
    case NUM_TASKS:
      return is_set_num_tasks();
    case EMITTED:
      return is_set_emitted();
    case TRANSFERRED:
      return is_set_transferred();
    case ACKED:
      return is_set_acked();
    case FAILED:
      return is_set_failed();
    case RESOURCES_MAP:
      return is_set_resources_map();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof CommonAggregateStats)
      return this.equals((CommonAggregateStats)that);
    return false;
  }

  public boolean equals(CommonAggregateStats that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_num_executors = true && this.is_set_num_executors();
    boolean that_present_num_executors = true && that.is_set_num_executors();
    if (this_present_num_executors || that_present_num_executors) {
      if (!(this_present_num_executors && that_present_num_executors))
        return false;
      if (this.num_executors != that.num_executors)
        return false;
    }

    boolean this_present_num_tasks = true && this.is_set_num_tasks();
    boolean that_present_num_tasks = true && that.is_set_num_tasks();
    if (this_present_num_tasks || that_present_num_tasks) {
      if (!(this_present_num_tasks && that_present_num_tasks))
        return false;
      if (this.num_tasks != that.num_tasks)
        return false;
    }

    boolean this_present_emitted = true && this.is_set_emitted();
    boolean that_present_emitted = true && that.is_set_emitted();
    if (this_present_emitted || that_present_emitted) {
      if (!(this_present_emitted && that_present_emitted))
        return false;
      if (this.emitted != that.emitted)
        return false;
    }

    boolean this_present_transferred = true && this.is_set_transferred();
    boolean that_present_transferred = true && that.is_set_transferred();
    if (this_present_transferred || that_present_transferred) {
      if (!(this_present_transferred && that_present_transferred))
        return false;
      if (this.transferred != that.transferred)
        return false;
    }

    boolean this_present_acked = true && this.is_set_acked();
    boolean that_present_acked = true && that.is_set_acked();
    if (this_present_acked || that_present_acked) {
      if (!(this_present_acked && that_present_acked))
        return false;
      if (this.acked != that.acked)
        return false;
    }

    boolean this_present_failed = true && this.is_set_failed();
    boolean that_present_failed = true && that.is_set_failed();
    if (this_present_failed || that_present_failed) {
      if (!(this_present_failed && that_present_failed))
        return false;
      if (this.failed != that.failed)
        return false;
    }

    boolean this_present_resources_map = true && this.is_set_resources_map();
    boolean that_present_resources_map = true && that.is_set_resources_map();
    if (this_present_resources_map || that_present_resources_map) {
      if (!(this_present_resources_map && that_present_resources_map))
        return false;
      if (!this.resources_map.equals(that.resources_map))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((is_set_num_executors()) ? 131071 : 524287);
    if (is_set_num_executors())
      hashCode = hashCode * 8191 + num_executors;

    hashCode = hashCode * 8191 + ((is_set_num_tasks()) ? 131071 : 524287);
    if (is_set_num_tasks())
      hashCode = hashCode * 8191 + num_tasks;

    hashCode = hashCode * 8191 + ((is_set_emitted()) ? 131071 : 524287);
    if (is_set_emitted())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(emitted);

    hashCode = hashCode * 8191 + ((is_set_transferred()) ? 131071 : 524287);
    if (is_set_transferred())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(transferred);

    hashCode = hashCode * 8191 + ((is_set_acked()) ? 131071 : 524287);
    if (is_set_acked())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(acked);

    hashCode = hashCode * 8191 + ((is_set_failed()) ? 131071 : 524287);
    if (is_set_failed())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(failed);

    hashCode = hashCode * 8191 + ((is_set_resources_map()) ? 131071 : 524287);
    if (is_set_resources_map())
      hashCode = hashCode * 8191 + resources_map.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(CommonAggregateStats other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(is_set_num_executors()).compareTo(other.is_set_num_executors());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_num_executors()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.num_executors, other.num_executors);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_num_tasks()).compareTo(other.is_set_num_tasks());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_num_tasks()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.num_tasks, other.num_tasks);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_emitted()).compareTo(other.is_set_emitted());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_emitted()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.emitted, other.emitted);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_transferred()).compareTo(other.is_set_transferred());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_transferred()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.transferred, other.transferred);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_acked()).compareTo(other.is_set_acked());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_acked()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.acked, other.acked);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_failed()).compareTo(other.is_set_failed());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_failed()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.failed, other.failed);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_resources_map()).compareTo(other.is_set_resources_map());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_resources_map()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.resources_map, other.resources_map);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("CommonAggregateStats(");
    boolean first = true;

    if (is_set_num_executors()) {
      sb.append("num_executors:");
      sb.append(this.num_executors);
      first = false;
    }
    if (is_set_num_tasks()) {
      if (!first) sb.append(", ");
      sb.append("num_tasks:");
      sb.append(this.num_tasks);
      first = false;
    }
    if (is_set_emitted()) {
      if (!first) sb.append(", ");
      sb.append("emitted:");
      sb.append(this.emitted);
      first = false;
    }
    if (is_set_transferred()) {
      if (!first) sb.append(", ");
      sb.append("transferred:");
      sb.append(this.transferred);
      first = false;
    }
    if (is_set_acked()) {
      if (!first) sb.append(", ");
      sb.append("acked:");
      sb.append(this.acked);
      first = false;
    }
    if (is_set_failed()) {
      if (!first) sb.append(", ");
      sb.append("failed:");
      sb.append(this.failed);
      first = false;
    }
    if (is_set_resources_map()) {
      if (!first) sb.append(", ");
      sb.append("resources_map:");
      if (this.resources_map == null) {
        sb.append("null");
      } else {
        sb.append(this.resources_map);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class CommonAggregateStatsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public CommonAggregateStatsStandardScheme getScheme() {
      return new CommonAggregateStatsStandardScheme();
    }
  }

  private static class CommonAggregateStatsStandardScheme extends org.apache.thrift.scheme.StandardScheme<CommonAggregateStats> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CommonAggregateStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // NUM_EXECUTORS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.num_executors = iprot.readI32();
              struct.set_num_executors_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // NUM_TASKS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.num_tasks = iprot.readI32();
              struct.set_num_tasks_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // EMITTED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.emitted = iprot.readI64();
              struct.set_emitted_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TRANSFERRED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.transferred = iprot.readI64();
              struct.set_transferred_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // ACKED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.acked = iprot.readI64();
              struct.set_acked_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // FAILED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.failed = iprot.readI64();
              struct.set_failed_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // RESOURCES_MAP
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map396 = iprot.readMapBegin();
                struct.resources_map = new java.util.HashMap<java.lang.String,java.lang.Double>(2*_map396.size);
                java.lang.String _key397;
                double _val398;
                for (int _i399 = 0; _i399 < _map396.size; ++_i399)
                {
                  _key397 = iprot.readString();
                  _val398 = iprot.readDouble();
                  struct.resources_map.put(_key397, _val398);
                }
                iprot.readMapEnd();
              }
              struct.set_resources_map_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, CommonAggregateStats struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.is_set_num_executors()) {
        oprot.writeFieldBegin(NUM_EXECUTORS_FIELD_DESC);
        oprot.writeI32(struct.num_executors);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_num_tasks()) {
        oprot.writeFieldBegin(NUM_TASKS_FIELD_DESC);
        oprot.writeI32(struct.num_tasks);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_emitted()) {
        oprot.writeFieldBegin(EMITTED_FIELD_DESC);
        oprot.writeI64(struct.emitted);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_transferred()) {
        oprot.writeFieldBegin(TRANSFERRED_FIELD_DESC);
        oprot.writeI64(struct.transferred);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_acked()) {
        oprot.writeFieldBegin(ACKED_FIELD_DESC);
        oprot.writeI64(struct.acked);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_failed()) {
        oprot.writeFieldBegin(FAILED_FIELD_DESC);
        oprot.writeI64(struct.failed);
        oprot.writeFieldEnd();
      }
      if (struct.resources_map != null) {
        if (struct.is_set_resources_map()) {
          oprot.writeFieldBegin(RESOURCES_MAP_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.DOUBLE, struct.resources_map.size()));
            for (java.util.Map.Entry<java.lang.String, java.lang.Double> _iter400 : struct.resources_map.entrySet())
            {
              oprot.writeString(_iter400.getKey());
              oprot.writeDouble(_iter400.getValue());
            }
            oprot.writeMapEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CommonAggregateStatsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public CommonAggregateStatsTupleScheme getScheme() {
      return new CommonAggregateStatsTupleScheme();
    }
  }

  private static class CommonAggregateStatsTupleScheme extends org.apache.thrift.scheme.TupleScheme<CommonAggregateStats> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CommonAggregateStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.is_set_num_executors()) {
        optionals.set(0);
      }
      if (struct.is_set_num_tasks()) {
        optionals.set(1);
      }
      if (struct.is_set_emitted()) {
        optionals.set(2);
      }
      if (struct.is_set_transferred()) {
        optionals.set(3);
      }
      if (struct.is_set_acked()) {
        optionals.set(4);
      }
      if (struct.is_set_failed()) {
        optionals.set(5);
      }
      if (struct.is_set_resources_map()) {
        optionals.set(6);
      }
      oprot.writeBitSet(optionals, 7);
      if (struct.is_set_num_executors()) {
        oprot.writeI32(struct.num_executors);
      }
      if (struct.is_set_num_tasks()) {
        oprot.writeI32(struct.num_tasks);
      }
      if (struct.is_set_emitted()) {
        oprot.writeI64(struct.emitted);
      }
      if (struct.is_set_transferred()) {
        oprot.writeI64(struct.transferred);
      }
      if (struct.is_set_acked()) {
        oprot.writeI64(struct.acked);
      }
      if (struct.is_set_failed()) {
        oprot.writeI64(struct.failed);
      }
      if (struct.is_set_resources_map()) {
        {
          oprot.writeI32(struct.resources_map.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.Double> _iter401 : struct.resources_map.entrySet())
          {
            oprot.writeString(_iter401.getKey());
            oprot.writeDouble(_iter401.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CommonAggregateStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(7);
      if (incoming.get(0)) {
        struct.num_executors = iprot.readI32();
        struct.set_num_executors_isSet(true);
      }
      if (incoming.get(1)) {
        struct.num_tasks = iprot.readI32();
        struct.set_num_tasks_isSet(true);
      }
      if (incoming.get(2)) {
        struct.emitted = iprot.readI64();
        struct.set_emitted_isSet(true);
      }
      if (incoming.get(3)) {
        struct.transferred = iprot.readI64();
        struct.set_transferred_isSet(true);
      }
      if (incoming.get(4)) {
        struct.acked = iprot.readI64();
        struct.set_acked_isSet(true);
      }
      if (incoming.get(5)) {
        struct.failed = iprot.readI64();
        struct.set_failed_isSet(true);
      }
      if (incoming.get(6)) {
        {
          org.apache.thrift.protocol.TMap _map402 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.resources_map = new java.util.HashMap<java.lang.String,java.lang.Double>(2*_map402.size);
          java.lang.String _key403;
          double _val404;
          for (int _i405 = 0; _i405 < _map402.size; ++_i405)
          {
            _key403 = iprot.readString();
            _val404 = iprot.readDouble();
            struct.resources_map.put(_key403, _val404);
          }
        }
        struct.set_resources_map_isSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

