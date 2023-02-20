package com.sikderithub.facebookvideodownloader.notification;

public class NotificationData {
    public int action, notiClearAble, notiType;
    public String tittle, description, actionActivity, actionUrl, imgUrl, timestamp;

    public NotificationData() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getNotiClearAble() {
        return notiClearAble;
    }

    public void setNotiClearAble(int notiClearAble) {
        this.notiClearAble = notiClearAble;
    }

    public int getNotiType() {
        return notiType;
    }

    public void setNotiType(int notiType) {
        this.notiType = notiType;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionActivity() {
        return actionActivity;
    }

    public void setActionActivity(String actionActivity) {
        this.actionActivity = actionActivity;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
