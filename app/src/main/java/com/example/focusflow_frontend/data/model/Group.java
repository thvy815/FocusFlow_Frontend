package com.example.focusflow_frontend.data.model;

public class Group {
    private String id;
    private String group_name;
    private String leader_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getLeader_id() {
        return leader_id;
    }

    public void setLeader_id(String leader_id) {
        this.leader_id = leader_id;
    }

    public Group(String id, String group_name, String leader_id) {
        this.id = id;
        this.group_name = group_name;
        this.leader_id = leader_id;
    }
}
