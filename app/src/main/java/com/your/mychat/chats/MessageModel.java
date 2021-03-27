package com.your.mychat.chats;

public class MessageModel {

    private String message;
    private String messageType;
    private String messageFrom;
    private long messageTime;
    private String messageId;
   //this Constructor to fetch the Messages direct fro DataBase
    public MessageModel() {
    }

    public MessageModel(String message, String messageType, String messageFrom, long messageTime, String messageId) {
        this.message = message;
        this.messageType = messageType;
        this.messageFrom = messageFrom;
        this.messageTime = messageTime;
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
