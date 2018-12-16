package com.example.talong.sharingapplication.Model;

public class Posts {
    public String date,fullname,postimage,profileimage,time,uid,status;

    public Posts() {
    }

    public Posts(String date, String fullname, String postimage, String profileimage, String time, String uid, String status) {
        this.date = date;
        this.fullname = fullname;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.time = time;
        this.uid = uid;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
