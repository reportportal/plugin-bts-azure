package com.epam.reportportal.extension.azure.rest.client.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class ResponseExceptionModel {

    @SerializedName("$id")
    private Integer id = null;

    @SerializedName("innerException")
    private String innerException = null;

    @SerializedName("message")
    private String message = null;

    @SerializedName("typeName")
    private String typeName = null;

    @SerializedName("typeKey")
    private String typeKey = null;

    @SerializedName("errorCode")
    private Integer errorCode = null;

    @SerializedName("eventId")
    private Integer eventId = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInnerException() {
        return innerException;
    }

    public void setInnerException(String innerException) {
        this.innerException = innerException;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "{" + "id=" + id + ", innerException='" + innerException + '\'' + ", message='"
                + message + '\'' + ", typeName='" + typeName + '\'' + ", typeKey='" + typeKey + '\'' + ", errorCode="
                + errorCode + ", eventId=" + eventId + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResponseExceptionModel that = (ResponseExceptionModel) o;
        return Objects.equals(id, that.id) && Objects.equals(innerException, that.innerException) && Objects
                .equals(message, that.message) && Objects.equals(typeName, that.typeName) && Objects
                .equals(typeKey, that.typeKey) && Objects.equals(errorCode, that.errorCode) && Objects
                .equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, innerException, message, typeName, typeKey, errorCode, eventId);
    }
}
