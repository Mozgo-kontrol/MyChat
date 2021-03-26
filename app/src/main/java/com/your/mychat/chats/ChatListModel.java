package com.your.mychat.chats;

public class ChatListModel {

    private String _userId;
    private String _userName;
    private String _photoName;
    private String _unreadCount;
    private String _lastMessage;
    private String _lastMessageTime;

    public ChatListModel(String _userId, String _userName, String _photoName, String _unreadCount, String _lastMessage, String _lastMessageTime) {
        this._userId = _userId;
        this._userName = _userName;
        this._photoName = _photoName;
        this._unreadCount = _unreadCount;
        this._lastMessage = _lastMessage;
        this._lastMessageTime = _lastMessageTime;
    }

    public String get_userId() {
        return _userId;
    }

    public void set_userId(String _userId) {
        this._userId = _userId;
    }

    public String get_userName() {
        return _userName;
    }

    public void set_userName(String _userName) {
        this._userName = _userName;
    }

    public String get_photoName() {
        return _photoName;
    }

    public void set_photoName(String _photoName) {
        this._photoName = _photoName;
    }

    public String get_unreadCount() {
        return _unreadCount;
    }

    public void set_unreadCount(String _unreadCount) {
        this._unreadCount = _unreadCount;
    }

    public String get_lastMessage() {
        return _lastMessage;
    }

    public void set_lastMessage(String _lastMessage) {
        this._lastMessage = _lastMessage;
    }

    public String get_lastMessageTime() {
        return _lastMessageTime;
    }

    public void set_lastMessageTime(String _lastMessageTime) {
        this._lastMessageTime = _lastMessageTime;
    }
}
