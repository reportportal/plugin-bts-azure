/*
 * WorkItemTracking
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 6.1-preview
 * Contact: nugetvss@microsoft.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.epam.reportportal.extension.azure.rest.client.model.workitem;

import com.google.gson.annotations.SerializedName;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Describes a work item field operation.
 */
@Schema(description = "Describes a work item field operation.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-08-03T21:33:08.123Z")
public class WorkItemFieldOperation {
  @SerializedName("name")
  private String name = null;

  @SerializedName("referenceName")
  private String referenceName = null;

  public WorkItemFieldOperation name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Friendly name of the operation.
   * @return name
  **/
  @Schema(description = "Friendly name of the operation.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WorkItemFieldOperation referenceName(String referenceName) {
    this.referenceName = referenceName;
    return this;
  }

   /**
   * Reference name of the operation.
   * @return referenceName
  **/
  @Schema(description = "Reference name of the operation.")
  public String getReferenceName() {
    return referenceName;
  }

  public void setReferenceName(String referenceName) {
    this.referenceName = referenceName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkItemFieldOperation workItemFieldOperation = (WorkItemFieldOperation) o;
    return Objects.equals(this.name, workItemFieldOperation.name) &&
        Objects.equals(this.referenceName, workItemFieldOperation.referenceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, referenceName);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkItemFieldOperation {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    referenceName: ").append(toIndentedString(referenceName)).append("\n");
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
