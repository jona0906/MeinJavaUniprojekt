package de.pfh.javauniprojekt;

import java.util.Date;

public class Kommentar {

    private String content;
    private String username;
    private String userID;
    private Date date;

    public Kommentar(String content, String username, String userID) {
        this.content = content;
        this.username = username;
        this.userID = userID;
    }

    public Date getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }
}
