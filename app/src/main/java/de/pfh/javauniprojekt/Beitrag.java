package de.pfh.javauniprojekt;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

public class Beitrag implements Parcelable
{
    private String content;
    private Date date;
    private String userID;
    private String username;
    private String uid;
    private int likes;

    public Beitrag(String content, String username, String userID) {
        this.content = content;
        this.username = username;
        this.userID = userID;
    }
    public Beitrag(){ // wird benötigt.

    }

    protected Beitrag(Parcel in) {
        content = in.readString();
        userID = in.readString();
        username = in.readString();
    }

    public static final Creator<Beitrag> CREATOR = new Creator<Beitrag>() {
        @Override
        public Beitrag createFromParcel(Parcel in) {
            return new Beitrag(in);
        }

        @Override
        public Beitrag[] newArray(int size) {
            return new Beitrag[size];
        }
    };

    public String getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }

    public int getLikes() { return likes;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(content);
        parcel.writeString(userID);
        parcel.writeString(username);
    }
}


