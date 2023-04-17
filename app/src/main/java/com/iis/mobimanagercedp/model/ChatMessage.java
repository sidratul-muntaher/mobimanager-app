package com.iis.mobimanagercedp.model;

public class ChatMessage {

    String chatId;
    String personName;
    String message;
    String dateTime;
    boolean isAdmin;

    public ChatMessage() {
    }

    public ChatMessage(String chatId, String personName, String message, String dateTime, boolean isAdmin) {
        this.chatId = chatId;
        this.personName = personName;
        this.message = message;
        this.dateTime = dateTime;
        this.isAdmin = isAdmin;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
