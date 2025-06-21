package com.example.focusflow_frontend.data.model;

import com.google.gson.annotations.SerializedName;

public class CtGroupUser {
    @SerializedName("id")
    private Integer id;

    @SerializedName("groupId")
    private Integer groupId;

    @SerializedName("userId")
    private Integer userId;

    public int getIdCt() {
        return id;
    }

    public void setIdCt(int idCt) {
        this.id = idCt;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
