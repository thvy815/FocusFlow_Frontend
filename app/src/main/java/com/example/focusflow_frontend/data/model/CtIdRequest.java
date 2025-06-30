package com.example.focusflow_frontend.data.model;

import java.util.List;

public class CtIdRequest {
    private List<Integer> userIds;
    private int groupId;

    public CtIdRequest(List<Integer> userIds, int groupId) {
        this.userIds = userIds;
        this.groupId = groupId;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public int getGroupId() {
        return groupId;
    }
}
