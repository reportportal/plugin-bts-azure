package com.epam.reportportal.extension.azure.rest.client.model.workitem;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WorkItemTypeFieldWithReferencesList {

    @SerializedName("count")
    private Integer count = null;

    @SerializedName("value")
    private List<WorkItemTypeFieldWithReferences> value = null;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<WorkItemTypeFieldWithReferences> getValue() {
        return value;
    }

    public void setValue(List<WorkItemTypeFieldWithReferences> value) {
        this.value = value;
    }
}
