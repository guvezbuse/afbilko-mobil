
package com.example.afbilko;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private boolean isEmergency;

    public ChatMessage(String message, boolean isUser, boolean isEmergency) {
        this.message = message;
        this.isUser = isUser;
        this.isEmergency = isEmergency;
    }

    public String getMessage() { return message; }
    public boolean isUser() { return isUser; }
    public boolean isEmergency() { return isEmergency; }
}