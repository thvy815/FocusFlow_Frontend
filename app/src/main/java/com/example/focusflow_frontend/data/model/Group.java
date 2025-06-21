package com.example.focusflow_frontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Group implements Serializable {
    private int id;

    @SerializedName("groupName")  // ánh xạ đúng với JSON
    private String groupName;

    @SerializedName("leaderId")
    private int leaderId;

    public Group(int id, String groupName, int leaderId) {
        this.id = id;
        this.groupName = groupName;
        this.leaderId = leaderId;
    }

    public int getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }
}
