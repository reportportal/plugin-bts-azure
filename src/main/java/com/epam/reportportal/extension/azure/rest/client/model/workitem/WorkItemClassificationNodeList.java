package com.epam.reportportal.extension.azure.rest.client.model.workitem;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WorkItemClassificationNodeList {

    @SerializedName("count")
    private Integer count = null;

    @SerializedName("value")
    private List<WorkItemClassificationNode> value = null;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<WorkItemClassificationNode> getValue() {
        return value;
    }

    public void setValue(List<WorkItemClassificationNode> value) {
        this.value = value;
    }
}
