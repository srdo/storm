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
public class ExecutorStats implements org.apache.thrift.TBase<ExecutorStats, ExecutorStats._Fields>, java.io.Serializable, Cloneable, Comparable<ExecutorStats> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ExecutorStats");

  private static final org.apache.thrift.protocol.TField EMITTED_FIELD_DESC = new org.apache.thrift.protocol.TField("emitted", org.apache.thrift.protocol.TType.MAP, (short)1);
  private static final org.apache.thrift.protocol.TField TRANSFERRED_FIELD_DESC = new org.apache.thrift.protocol.TField("transferred", org.apache.thrift.protocol.TType.MAP, (short)2);
  private static final org.apache.thrift.protocol.TField SPECIFIC_FIELD_DESC = new org.apache.thrift.protocol.TField("specific", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("rate", org.apache.thrift.protocol.TType.DOUBLE, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ExecutorStatsStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ExecutorStatsTupleSchemeFactory();

  private java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> emitted; // required
  private java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> transferred; // required
  private ExecutorSpecificStats specific; // required
  private double rate; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    EMITTED((short)1, "emitted"),
    TRANSFERRED((short)2, "transferred"),
    SPECIFIC((short)3, "specific"),
    RATE((short)4, "rate");

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
        case 1: // EMITTED
          return EMITTED;
        case 2: // TRANSFERRED
          return TRANSFERRED;
        case 3: // SPECIFIC
          return SPECIFIC;
        case 4: // RATE
          return RATE;
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
  private static final int __RATE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.EMITTED, new org.apache.thrift.meta_data.FieldMetaData("emitted", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)))));
    tmpMap.put(_Fields.TRANSFERRED, new org.apache.thrift.meta_data.FieldMetaData("transferred", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)))));
    tmpMap.put(_Fields.SPECIFIC, new org.apache.thrift.meta_data.FieldMetaData("specific", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ExecutorSpecificStats.class)));
    tmpMap.put(_Fields.RATE, new org.apache.thrift.meta_data.FieldMetaData("rate", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ExecutorStats.class, metaDataMap);
  }

  public ExecutorStats() {
  }

  public ExecutorStats(
    java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> emitted,
    java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> transferred,
    ExecutorSpecificStats specific,
    double rate)
  {
    this();
    this.emitted = emitted;
    this.transferred = transferred;
    this.specific = specific;
    this.rate = rate;
    set_rate_isSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ExecutorStats(ExecutorStats other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.is_set_emitted()) {
      java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> __this__emitted = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>(other.emitted.size());
      for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String,java.lang.Long>> other_element : other.emitted.entrySet()) {

        java.lang.String other_element_key = other_element.getKey();
        java.util.Map<java.lang.String,java.lang.Long> other_element_value = other_element.getValue();

        java.lang.String __this__emitted_copy_key = other_element_key;

        java.util.Map<java.lang.String,java.lang.Long> __this__emitted_copy_value = new java.util.HashMap<java.lang.String,java.lang.Long>(other_element_value);

        __this__emitted.put(__this__emitted_copy_key, __this__emitted_copy_value);
      }
      this.emitted = __this__emitted;
    }
    if (other.is_set_transferred()) {
      java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> __this__transferred = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>(other.transferred.size());
      for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String,java.lang.Long>> other_element : other.transferred.entrySet()) {

        java.lang.String other_element_key = other_element.getKey();
        java.util.Map<java.lang.String,java.lang.Long> other_element_value = other_element.getValue();

        java.lang.String __this__transferred_copy_key = other_element_key;

        java.util.Map<java.lang.String,java.lang.Long> __this__transferred_copy_value = new java.util.HashMap<java.lang.String,java.lang.Long>(other_element_value);

        __this__transferred.put(__this__transferred_copy_key, __this__transferred_copy_value);
      }
      this.transferred = __this__transferred;
    }
    if (other.is_set_specific()) {
      this.specific = new ExecutorSpecificStats(other.specific);
    }
    this.rate = other.rate;
  }

  public ExecutorStats deepCopy() {
    return new ExecutorStats(this);
  }

  @Override
  public void clear() {
    this.emitted = null;
    this.transferred = null;
    this.specific = null;
    set_rate_isSet(false);
    this.rate = 0.0;
  }

  public int get_emitted_size() {
    return (this.emitted == null) ? 0 : this.emitted.size();
  }

  public void put_to_emitted(java.lang.String key, java.util.Map<java.lang.String,java.lang.Long> val) {
    if (this.emitted == null) {
      this.emitted = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>();
    }
    this.emitted.put(key, val);
  }

  public java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> get_emitted() {
    return this.emitted;
  }

  public void set_emitted(java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> emitted) {
    this.emitted = emitted;
  }

  public void unset_emitted() {
    this.emitted = null;
  }

  /** Returns true if field emitted is set (has been assigned a value) and false otherwise */
  public boolean is_set_emitted() {
    return this.emitted != null;
  }

  public void set_emitted_isSet(boolean value) {
    if (!value) {
      this.emitted = null;
    }
  }

  public int get_transferred_size() {
    return (this.transferred == null) ? 0 : this.transferred.size();
  }

  public void put_to_transferred(java.lang.String key, java.util.Map<java.lang.String,java.lang.Long> val) {
    if (this.transferred == null) {
      this.transferred = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>();
    }
    this.transferred.put(key, val);
  }

  public java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> get_transferred() {
    return this.transferred;
  }

  public void set_transferred(java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>> transferred) {
    this.transferred = transferred;
  }

  public void unset_transferred() {
    this.transferred = null;
  }

  /** Returns true if field transferred is set (has been assigned a value) and false otherwise */
  public boolean is_set_transferred() {
    return this.transferred != null;
  }

  public void set_transferred_isSet(boolean value) {
    if (!value) {
      this.transferred = null;
    }
  }

  public ExecutorSpecificStats get_specific() {
    return this.specific;
  }

  public void set_specific(ExecutorSpecificStats specific) {
    this.specific = specific;
  }

  public void unset_specific() {
    this.specific = null;
  }

  /** Returns true if field specific is set (has been assigned a value) and false otherwise */
  public boolean is_set_specific() {
    return this.specific != null;
  }

  public void set_specific_isSet(boolean value) {
    if (!value) {
      this.specific = null;
    }
  }

  public double get_rate() {
    return this.rate;
  }

  public void set_rate(double rate) {
    this.rate = rate;
    set_rate_isSet(true);
  }

  public void unset_rate() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __RATE_ISSET_ID);
  }

  /** Returns true if field rate is set (has been assigned a value) and false otherwise */
  public boolean is_set_rate() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __RATE_ISSET_ID);
  }

  public void set_rate_isSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __RATE_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case EMITTED:
      if (value == null) {
        unset_emitted();
      } else {
        set_emitted((java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>)value);
      }
      break;

    case TRANSFERRED:
      if (value == null) {
        unset_transferred();
      } else {
        set_transferred((java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>)value);
      }
      break;

    case SPECIFIC:
      if (value == null) {
        unset_specific();
      } else {
        set_specific((ExecutorSpecificStats)value);
      }
      break;

    case RATE:
      if (value == null) {
        unset_rate();
      } else {
        set_rate((java.lang.Double)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case EMITTED:
      return get_emitted();

    case TRANSFERRED:
      return get_transferred();

    case SPECIFIC:
      return get_specific();

    case RATE:
      return get_rate();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case EMITTED:
      return is_set_emitted();
    case TRANSFERRED:
      return is_set_transferred();
    case SPECIFIC:
      return is_set_specific();
    case RATE:
      return is_set_rate();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof ExecutorStats)
      return this.equals((ExecutorStats)that);
    return false;
  }

  public boolean equals(ExecutorStats that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_emitted = true && this.is_set_emitted();
    boolean that_present_emitted = true && that.is_set_emitted();
    if (this_present_emitted || that_present_emitted) {
      if (!(this_present_emitted && that_present_emitted))
        return false;
      if (!this.emitted.equals(that.emitted))
        return false;
    }

    boolean this_present_transferred = true && this.is_set_transferred();
    boolean that_present_transferred = true && that.is_set_transferred();
    if (this_present_transferred || that_present_transferred) {
      if (!(this_present_transferred && that_present_transferred))
        return false;
      if (!this.transferred.equals(that.transferred))
        return false;
    }

    boolean this_present_specific = true && this.is_set_specific();
    boolean that_present_specific = true && that.is_set_specific();
    if (this_present_specific || that_present_specific) {
      if (!(this_present_specific && that_present_specific))
        return false;
      if (!this.specific.equals(that.specific))
        return false;
    }

    boolean this_present_rate = true;
    boolean that_present_rate = true;
    if (this_present_rate || that_present_rate) {
      if (!(this_present_rate && that_present_rate))
        return false;
      if (this.rate != that.rate)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((is_set_emitted()) ? 131071 : 524287);
    if (is_set_emitted())
      hashCode = hashCode * 8191 + emitted.hashCode();

    hashCode = hashCode * 8191 + ((is_set_transferred()) ? 131071 : 524287);
    if (is_set_transferred())
      hashCode = hashCode * 8191 + transferred.hashCode();

    hashCode = hashCode * 8191 + ((is_set_specific()) ? 131071 : 524287);
    if (is_set_specific())
      hashCode = hashCode * 8191 + specific.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(rate);

    return hashCode;
  }

  @Override
  public int compareTo(ExecutorStats other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

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
    lastComparison = java.lang.Boolean.valueOf(is_set_specific()).compareTo(other.is_set_specific());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_specific()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.specific, other.specific);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_rate()).compareTo(other.is_set_rate());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_rate()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.rate, other.rate);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("ExecutorStats(");
    boolean first = true;

    sb.append("emitted:");
    if (this.emitted == null) {
      sb.append("null");
    } else {
      sb.append(this.emitted);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("transferred:");
    if (this.transferred == null) {
      sb.append("null");
    } else {
      sb.append(this.transferred);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("specific:");
    if (this.specific == null) {
      sb.append("null");
    } else {
      sb.append(this.specific);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("rate:");
    sb.append(this.rate);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (!is_set_emitted()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'emitted' is unset! Struct:" + toString());
    }

    if (!is_set_transferred()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'transferred' is unset! Struct:" + toString());
    }

    if (!is_set_specific()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'specific' is unset! Struct:" + toString());
    }

    if (!is_set_rate()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'rate' is unset! Struct:" + toString());
    }

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

  private static class ExecutorStatsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public ExecutorStatsStandardScheme getScheme() {
      return new ExecutorStatsStandardScheme();
    }
  }

  private static class ExecutorStatsStandardScheme extends org.apache.thrift.scheme.StandardScheme<ExecutorStats> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ExecutorStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // EMITTED
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map320 = iprot.readMapBegin();
                struct.emitted = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>(2*_map320.size);
                java.lang.String _key321;
                java.util.Map<java.lang.String,java.lang.Long> _val322;
                for (int _i323 = 0; _i323 < _map320.size; ++_i323)
                {
                  _key321 = iprot.readString();
                  {
                    org.apache.thrift.protocol.TMap _map324 = iprot.readMapBegin();
                    _val322 = new java.util.HashMap<java.lang.String,java.lang.Long>(2*_map324.size);
                    java.lang.String _key325;
                    long _val326;
                    for (int _i327 = 0; _i327 < _map324.size; ++_i327)
                    {
                      _key325 = iprot.readString();
                      _val326 = iprot.readI64();
                      _val322.put(_key325, _val326);
                    }
                    iprot.readMapEnd();
                  }
                  struct.emitted.put(_key321, _val322);
                }
                iprot.readMapEnd();
              }
              struct.set_emitted_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // TRANSFERRED
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map328 = iprot.readMapBegin();
                struct.transferred = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>(2*_map328.size);
                java.lang.String _key329;
                java.util.Map<java.lang.String,java.lang.Long> _val330;
                for (int _i331 = 0; _i331 < _map328.size; ++_i331)
                {
                  _key329 = iprot.readString();
                  {
                    org.apache.thrift.protocol.TMap _map332 = iprot.readMapBegin();
                    _val330 = new java.util.HashMap<java.lang.String,java.lang.Long>(2*_map332.size);
                    java.lang.String _key333;
                    long _val334;
                    for (int _i335 = 0; _i335 < _map332.size; ++_i335)
                    {
                      _key333 = iprot.readString();
                      _val334 = iprot.readI64();
                      _val330.put(_key333, _val334);
                    }
                    iprot.readMapEnd();
                  }
                  struct.transferred.put(_key329, _val330);
                }
                iprot.readMapEnd();
              }
              struct.set_transferred_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SPECIFIC
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.specific = new ExecutorSpecificStats();
              struct.specific.read(iprot);
              struct.set_specific_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // RATE
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.rate = iprot.readDouble();
              struct.set_rate_isSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ExecutorStats struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.emitted != null) {
        oprot.writeFieldBegin(EMITTED_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, struct.emitted.size()));
          for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String,java.lang.Long>> _iter336 : struct.emitted.entrySet())
          {
            oprot.writeString(_iter336.getKey());
            {
              oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, _iter336.getValue().size()));
              for (java.util.Map.Entry<java.lang.String, java.lang.Long> _iter337 : _iter336.getValue().entrySet())
              {
                oprot.writeString(_iter337.getKey());
                oprot.writeI64(_iter337.getValue());
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.transferred != null) {
        oprot.writeFieldBegin(TRANSFERRED_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, struct.transferred.size()));
          for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String,java.lang.Long>> _iter338 : struct.transferred.entrySet())
          {
            oprot.writeString(_iter338.getKey());
            {
              oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, _iter338.getValue().size()));
              for (java.util.Map.Entry<java.lang.String, java.lang.Long> _iter339 : _iter338.getValue().entrySet())
              {
                oprot.writeString(_iter339.getKey());
                oprot.writeI64(_iter339.getValue());
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.specific != null) {
        oprot.writeFieldBegin(SPECIFIC_FIELD_DESC);
        struct.specific.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(RATE_FIELD_DESC);
      oprot.writeDouble(struct.rate);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ExecutorStatsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public ExecutorStatsTupleScheme getScheme() {
      return new ExecutorStatsTupleScheme();
    }
  }

  private static class ExecutorStatsTupleScheme extends org.apache.thrift.scheme.TupleScheme<ExecutorStats> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ExecutorStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      {
        oprot.writeI32(struct.emitted.size());
        for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String,java.lang.Long>> _iter340 : struct.emitted.entrySet())
        {
          oprot.writeString(_iter340.getKey());
          {
            oprot.writeI32(_iter340.getValue().size());
            for (java.util.Map.Entry<java.lang.String, java.lang.Long> _iter341 : _iter340.getValue().entrySet())
            {
              oprot.writeString(_iter341.getKey());
              oprot.writeI64(_iter341.getValue());
            }
          }
        }
      }
      {
        oprot.writeI32(struct.transferred.size());
        for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String,java.lang.Long>> _iter342 : struct.transferred.entrySet())
        {
          oprot.writeString(_iter342.getKey());
          {
            oprot.writeI32(_iter342.getValue().size());
            for (java.util.Map.Entry<java.lang.String, java.lang.Long> _iter343 : _iter342.getValue().entrySet())
            {
              oprot.writeString(_iter343.getKey());
              oprot.writeI64(_iter343.getValue());
            }
          }
        }
      }
      struct.specific.write(oprot);
      oprot.writeDouble(struct.rate);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ExecutorStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      {
        org.apache.thrift.protocol.TMap _map344 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
        struct.emitted = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>(2*_map344.size);
        java.lang.String _key345;
        java.util.Map<java.lang.String,java.lang.Long> _val346;
        for (int _i347 = 0; _i347 < _map344.size; ++_i347)
        {
          _key345 = iprot.readString();
          {
            org.apache.thrift.protocol.TMap _map348 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, iprot.readI32());
            _val346 = new java.util.HashMap<java.lang.String,java.lang.Long>(2*_map348.size);
            java.lang.String _key349;
            long _val350;
            for (int _i351 = 0; _i351 < _map348.size; ++_i351)
            {
              _key349 = iprot.readString();
              _val350 = iprot.readI64();
              _val346.put(_key349, _val350);
            }
          }
          struct.emitted.put(_key345, _val346);
        }
      }
      struct.set_emitted_isSet(true);
      {
        org.apache.thrift.protocol.TMap _map352 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
        struct.transferred = new java.util.HashMap<java.lang.String,java.util.Map<java.lang.String,java.lang.Long>>(2*_map352.size);
        java.lang.String _key353;
        java.util.Map<java.lang.String,java.lang.Long> _val354;
        for (int _i355 = 0; _i355 < _map352.size; ++_i355)
        {
          _key353 = iprot.readString();
          {
            org.apache.thrift.protocol.TMap _map356 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, iprot.readI32());
            _val354 = new java.util.HashMap<java.lang.String,java.lang.Long>(2*_map356.size);
            java.lang.String _key357;
            long _val358;
            for (int _i359 = 0; _i359 < _map356.size; ++_i359)
            {
              _key357 = iprot.readString();
              _val358 = iprot.readI64();
              _val354.put(_key357, _val358);
            }
          }
          struct.transferred.put(_key353, _val354);
        }
      }
      struct.set_transferred_isSet(true);
      struct.specific = new ExecutorSpecificStats();
      struct.specific.read(iprot);
      struct.set_specific_isSet(true);
      struct.rate = iprot.readDouble();
      struct.set_rate_isSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

