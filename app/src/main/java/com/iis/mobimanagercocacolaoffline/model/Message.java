package com.iis.mobimanagercocacolaoffline.model;

import java.io.Serializable;

public class Message implements Serializable {
    String messageId;
    String messageTitle;
    String messageDetails;
    String imageUrl;
    String dateTime;
    String priority;

    public Message() {
    }

    public Message(String messageId, String messageTitle, String messageDetails, String imageUrl, String dateTime, String priority) {
        this.messageId = messageId;
        this.messageTitle = messageTitle;
        this.messageDetails = messageDetails;
        this.imageUrl = imageUrl;
        this.dateTime = dateTime;
        this.priority = priority;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageDetails() {
        return messageDetails;
    }

    public void setMessageDetails(String messageDetails) {
        this.messageDetails = messageDetails;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
