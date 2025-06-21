package com.example.focusflow_frontend.data.model;

import java.util.List;

public class GroupWithUsersRequest {
    private String groupName;
    private Integer leaderId;
    private List<Integer> userIds;

    public GroupWithUsersRequest(String groupName, Integer leaderId, List<Integer> userIds) {
        this.groupName = groupName;
        this.leaderId = leaderId;
        this.userIds = userIds;
    }

    // getters và setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}

