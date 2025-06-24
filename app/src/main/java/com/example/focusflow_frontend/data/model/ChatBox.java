package com.example.focusflow_frontend.data.model;

public class ChatBox {
    private String content;
    private boolean isUser;

    public ChatBox(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }
}

