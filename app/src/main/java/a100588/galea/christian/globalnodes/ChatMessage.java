package a100588.galea.christian.globalnodes;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

/**
 * Created by Chris on 14/05/2017.
 */

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private String userKey;

    public ChatMessage(String messageText, String messageUser, String userKey) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.userKey = userKey;
        // Initialize to current time
        messageTime = new Date().getTime();

//        userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}