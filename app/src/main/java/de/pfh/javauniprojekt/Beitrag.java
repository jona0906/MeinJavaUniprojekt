package de.pfh.javauniprojekt;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Date;

/**
 * Die Klasse Beitrag wird verwendet, um Objekte vom Typ "Beitrag" zu erstellen. Ein solches Objekt ist
 * dabei immer ein von einem User hochgeladener Post. Das Objekt selbst wird dabei nicht hochgeladen, aber wenn
 * die Daten aus Firebase ausgelesen werden, dann werden die einzelnen Elemente in einem Objekt gespeichert.
 * Ein Objekt enthält somit Daten, wie den Inhalt des Posts, das Datum der Veröffentlichung oder den Nutzernamen der Person,
 * welche den Post veröffentlicht hat.
 */
public class Beitrag implements Parcelable
{
    private String content;
    private Date date;
    private String userID;
    private String username;
    private String uid;
    private int likes;
    private boolean gelöscht;

    /**
     * Die Methode "Beitrag" erstellt beim Aufruf einen neuen Beitrag.
     * @param content Inhalt des Beitrages.
     * @param username Nutzername der Person, welche den Beitrag veröffentlicht hat.
     * @param userID Nutzer-ID der Person, welche den Beitrag veröffentlicht hat.
     */
    public Beitrag(String content, String username, String userID) {
        this.content = content;
        this.username = username;
        this.userID = userID;
    }

    /**
     * Auch diese Methode erstellt einen neuen Beitrag. Sie wird benötigt, um die Beiträge aus Firebase auszulesen.
     */
    public Beitrag(){ // wird benötigt.
    }

    /**
     * Konstruktor, um ein Beitrag-Objekt aus einem Parcel zu erstellen.
     * @param in Das Parcel, das die Daten für die Erstellung des 'Beitrag'-Objekts enthält.
     */
    protected Beitrag(Parcel in) {
        content = in.readString();
        userID = in.readString();
        username = in.readString();
    }

/**
 * Ein CREATOR, der verwendet wird, um einen Beitrag aus einem Parcel zu erstellen.
 */
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

    /**
     * Methode zum Auslesen des Inhalts.
     * @return Inhalt des jeweiligen Beitrags.
     */
    public String getContent() {
        return content;
    }

    /**
     * Methode zum Auslesen des Datums.
     * @return Datum, wann der Beitrag veröffentlicht wurde.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Methode zum Auslesen des Nutzernamen.
     * @return Nutzername der Person, welche den Beitrag veröffentlicht hat.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Methode zum Auslesen der Variable gelöscht.
     * @return Die Methode gibt zurück, ob der Beitrag von dem Nutzer gelöscht wurde.
     */
    public boolean getGelöscht() {
        return gelöscht;
    }

    /**
     * Methode zum Auslesen der Nutzer-ID.
     * @return Die Methode gibt die Nutzer-ID des Users zurück, welcher den Beitrag erstellt hat.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Methode zum Auslesen der Likes.
     * @return Die Methode gibt die Anzahl der Likes des Beitrages zurück.
     */
    public int getLikes() { return likes;}

    /**
     * Die Methode setzt die Anzahl der Likes des Objektes neu fest.
     * @param likes neue Anzahl an likes, welche das Objekt jetzt enthält
     */
    public void setLikes(int likes) {
        this.likes = likes;
    }

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


