package com.your.mychat.friendrequests;

public class RequestModel {

    private String _userId;
    private String _userName;
    private String _photoNae;

    public RequestModel(String _userId, String _userName, String _photoNae) {
        this._userId = _userId;
        this._userName = _userName;
        this._photoNae = _photoNae;
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

    public String get_photoNae() {
        return _photoNae;
    }

    public void set_photoNae(String _photoNae) {
        this._photoNae = _photoNae;
    }
}
