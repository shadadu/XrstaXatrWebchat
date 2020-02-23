package com.xrstaxatrwebchat.wchat;


public class Conversation {

    private final String ackId;
    private final String content;
    
    public Conversation(String ackId, String content) {
        this.ackId = ackId;
        this.content = content;
    }

    public String getAckId() {
        return ackId;
    }

    public String getContent() {
        return content;
    }

}
