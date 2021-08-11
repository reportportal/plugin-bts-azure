/*
 * Core
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 6.1-preview
 * Contact: nugetvss@microsoft.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.epam.reportportal.extension.azure.rest.client.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The class represents a property bag as a collection of key-value pairs. Values of all primitive types (any type with a &#x60;TypeCode !&#x3D; TypeCode.Object&#x60;) except for &#x60;DBNull&#x60; are accepted. Values of type Byte[], Int32, Double, DateType and String preserve their type, other primitives are retuned as a String. Byte[] expected as base64 encoded string.
 */
@ApiModel(description = "The class represents a property bag as a collection of key-value pairs. Values of all primitive types (any type with a `TypeCode != TypeCode.Object`) except for `DBNull` are accepted. Values of type Byte[], Int32, Double, DateType and String preserve their type, other primitives are retuned as a String. Byte[] expected as base64 encoded string.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-07-22T22:33:25.592Z")
public class PropertiesCollection {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("item")
  private Object item = null;

  @SerializedName("keys")
  private List<String> keys = null;

  @SerializedName("values")
  private List<String> values = null;

  public PropertiesCollection count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * The count of properties in the collection.
   * @return count
  **/
  @ApiModelProperty(value = "The count of properties in the collection.")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public PropertiesCollection item(Object item) {
    this.item = item;
    return this;
  }

   /**
   * Get item
   * @return item
  **/
  @ApiModelProperty(value = "")
  public Object getItem() {
    return item;
  }

  public void setItem(Object item) {
    this.item = item;
  }

  public PropertiesCollection keys(List<String> keys) {
    this.keys = keys;
    return this;
  }

  public PropertiesCollection addKeysItem(String keysItem) {
    if (this.keys == null) {
      this.keys = new ArrayList<String>();
    }
    this.keys.add(keysItem);
    return this;
  }

   /**
   * The set of keys in the collection.
   * @return keys
  **/
  @ApiModelProperty(value = "The set of keys in the collection.")
  public List<String> getKeys() {
    return keys;
  }

  public void setKeys(List<String> keys) {
    this.keys = keys;
  }

  public PropertiesCollection values(List<String> values) {
    this.values = values;
    return this;
  }

  public PropertiesCollection addValuesItem(String valuesItem) {
    if (this.values == null) {
      this.values = new ArrayList<String>();
    }
    this.values.add(valuesItem);
    return this;
  }

   /**
   * The set of values in the collection.
   * @return values
  **/
  @ApiModelProperty(value = "The set of values in the collection.")
  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PropertiesCollection propertiesCollection = (PropertiesCollection) o;
    return Objects.equals(this.count, propertiesCollection.count) &&
        Objects.equals(this.item, propertiesCollection.item) &&
        Objects.equals(this.keys, propertiesCollection.keys) &&
        Objects.equals(this.values, propertiesCollection.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, item, keys, values);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PropertiesCollection {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    item: ").append(toIndentedString(item)).append("\n");
    sb.append("    keys: ").append(toIndentedString(keys)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}