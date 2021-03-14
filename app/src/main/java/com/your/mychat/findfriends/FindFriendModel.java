package com.your.mychat.findfriends;

public class FindFriendModel {


    private String _userName;
    private String _photoName;
    private String _userId;
    private boolean _requestSent;

    public FindFriendModel(String _userName, String _photoName, String _userId, boolean _requestSent) {
        this._userName = _userName;
        this._photoName = _photoName;
        this._userId = _userId;
        this._requestSent = _requestSent;
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

    public String get_userId() {
        return _userId;
    }

    public void set_userId(String _userId) {
        this._userId = _userId;
    }

    public boolean is_requestSent() {
        return _requestSent;
    }

    public void set_requestSent(boolean _requestSent) {
        this._requestSent = _requestSent;
    }




}
