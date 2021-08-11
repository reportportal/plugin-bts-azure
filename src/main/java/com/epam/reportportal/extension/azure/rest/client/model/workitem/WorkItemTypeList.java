package com.epam.reportportal.extension.azure.rest.client.model.workitem;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WorkItemTypeList {

    @SerializedName("count")
    private Integer count = null;

    @SerializedName("value")
    private List<WorkItemType> value = null;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<WorkItemType> getValue() {
        return value;
    }

    public void setValue(List<WorkItemType> value) {
        this.value = value;
    }
}
