package de.pfh.javauniprojekt;

import android.app.Activity;
import android.content.Intent;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * In dieser Klasse werden alle Methoden ausgeführt, welche keinen direkten Einfluss auf die Android Schicht des Programmes haben.
 * Die meisten Methoden in dieser Klasse arbeiten mit der Firebase Datenbank und aktualisieren die dortigen Beiträge.
 */
public class Java {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference usersCollection = db.collection("users");
    private static List<Beitrag> alleKommentare = new ArrayList<>();
    private static List<Beitrag> filteredList = new ArrayList<>();

    /**
     * Die Methode testet, ob der Nutzername bereits vergeben ist.
     * @param username Nutzername, welcher getestet werden soll.
     * @return wahr oder falsch, je nachdem ob der Name vergeben ist.
     */
    public static Task<Boolean> istVerfuegbar(String username) {
        Query query = usersCollection.whereEqualTo("username", username);
        return query.get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        int anzahlErgebnisse = querySnapshot.size();
                        return anzahlErgebnisse == 0;
                    } else {
                        return false;
                    }
                });
    }

    /**
     * Die Methode testet, ob die Mail registriert ist.
     * @param email Mail, welche getestet werden soll.
     * @return wahr oder falsch, je nachdem ob die Mail registriert ist.
     */
    public static Task<Boolean> isEmailRegistered(String email) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods() == null || task.getResult().getSignInMethods().isEmpty()) {
                            taskCompletionSource.setResult(false);
                        } else {
                            taskCompletionSource.setResult(true);
                        }
                    } else {
                        taskCompletionSource.setResult(false);
                    }
                });

        return taskCompletionSource.getTask();
    }

    /**
     * Diese Methode legt einen neunen Benutzer in Firebase an.
     * @param email Mail, mit welcher sich der Nutzer registrieren möchte.
     * @param password Passwort, welches der Nutzer zur Registrierung eingegeben hat.
     * @param username Nutzername, welchen der Nutzer zur Registrierung gewählt hat.
     * @param auth FirebaseAuth-Instanz für die Authentifizierung.
     * @param activity Aktivität, von welcher die Methode aus aufgrufen wurde.
     */
    public static void registerUser(String email, String password, String username, FirebaseAuth auth, Activity activity) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            /**
             * Methode, welche erst startet, wenn der Nutzer angelegt wurde.
             * @param task Aufgabe, welche gerade läuft.
             */
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    showToast(activity, "Registrierung erfolgreich", Toast.LENGTH_LONG);
                    saveUsername(username, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), activity);
                    activity.startActivity(new Intent(activity, StartActivity.class));
                    activity.finish();
                } else {
                    showToast(activity, "Ups, da ist ein Fehler aufgetreten", Toast.LENGTH_SHORT);
                }
            }
        });

    }

    /**
     * Methode zum Speichern des Nutzernamens in Firebase.
     * @param username Nutzername, welcher gespeichert werden soll.
     * @param userId Zugehörige Nutzer-ID, um den Nutzernamen zuordnen zu können.
     * @param activity Laufende Aktivität, welche übergeben wird.
     */
    private static void saveUsername(String username, String userId, Activity activity) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        // Speicheren des Benutzernamen im Dokument
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("hatBeiträge", false);

        userRef.set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Der Benutzername wurde erfolgreich gespeichert
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hier gab es einen Fehler beim Speichern des Benutzernamens
                        Toast.makeText(activity, "Fehler beim Speichern des Benutzernamens", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Methode zum zeigen eines Toasts.
     * @param activity Aktivität, wo der Toast gezeigt werden soll.
     * @param message Nachricht, welche gezeigt werden soll.
     * @param duration Dauer, wie lange die Nachricht angezigt werden soll.
     */
    private static void showToast(Activity activity, String message, int duration) {
        Toast.makeText(activity, message, duration).show();
    }

    /**
     * Sortiert die Liste mit Beiträgen nach dem Datum, indem sie das Datum aus den Beiträgen ausliest.
     * @param alleBeiträge Eingegebene Liste an Beiträgen, welche sortiert werden soll.
     */
    public static void sortByDate(List<Beitrag> alleBeiträge) {
        alleBeiträge.sort(new Comparator<Beitrag>() {
            @Override
            public int compare(Beitrag beitrag1, Beitrag beitrag2) {
                return beitrag2.getDate().compareTo(beitrag1.getDate());
            }
        });
    }

    /**
     * Fügt einen Kommentar zu einem Beitrag hinzu.
     * @param activity Aktivität, wo die Methode aufgerufen wurde.
     * @param newPost Inhalt des Beitrages, welcher erstellt werden soll.
     * @param username Nutzername der Person, die den Beitrag erstellt.
     * @param uid Nutzer-ID der Person, die den Beitrag erstellt.
     * @param postDate Datum des Beitrages, unter welchem der Kommentar stehen soll
     * @param postUserID Nutzer-ID des Beitrages, unter welchem der Kommentar stehen soll
     */
    public static void addComment(Activity activity, String newPost, String username, String uid, Date postDate, String postUserID){
        Java.findPostByDateAndUserID(postUserID, postDate, new Java.OnPostFoundListener() {
            /**
             * Die Methode startet, wenn die vorherige Methode abgeschlossen ist.
             * @param documentPath Dokumentenpfad, des Beitrages unter dem der Kommentar stehen soll.
             */
            @Override
            public Task<List<Beitrag>> onPostFound(String documentPath) {
                String userId = FirebaseAuth.getInstance().getUid();
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = firestore.document(documentPath);
                assert userId != null;
                DocumentReference userDocRef = usersCollection.document(userId);
                CollectionReference collectionRef = docRef.collection("comments");

                Map<String, Object> postData = new HashMap<>();
                postData.put("date", Calendar.getInstance().getTime());
                postData.put("username", username);
                postData.put("userID", uid);
                postData.put("likes", 0);
                postData.put("content", newPost);
                postData.put("gelöscht", false);

                collectionRef.add(postData)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            /**
                             * Löst aus, wenn der Kommentar erfolgreich hochgeladen wurde und zeigt dieses als Toast an.
                             */
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(activity, "Kommentar erfolgreich hinzugefügt", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return null;
            }

            @Override
            public void onPostNotFound() {
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    /**
     * Methode zum Hinzufügen eines neuen Beitrags
     * @param activity Aktivität, aus der die Methode aufgerufen wird.
     * @param newPost Inhalt des neuen Posts.
     * @param username Nutzername der Person, welche den Post veröffentlicht hat.
     * @param uid Nutzer-ID der Person, welche den Post veröffentlicht hat.
     * @param statement Wahr oder falsch, je nachdem ob es sich bei dem Post um ein Statement (einem besonders kurzen Post) handelt
     */
    public static void addPost(Activity activity, String newPost, String username, String uid, boolean statement) {
        String userId = FirebaseAuth.getInstance().getUid();
        Map<String, Object> postData = new HashMap<>();
        postData.put("date", Calendar.getInstance().getTime());
        postData.put("username", username);
        postData.put("userID", uid);
        postData.put("likes", 0);
        postData.put("gelöscht", false);
        if (statement) {
            postData.put("content", newPost.replace("\n", " "));
        } else {
            postData.put("content", newPost);
        }
        postData.put("statement", statement);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference usersCollection = firestore.collection("users");
        DocumentReference userDocRef = usersCollection.document(userId);
        CollectionReference beitraegeCollection = userDocRef.collection("Beiträge");

        beitraegeCollection.add(postData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    /**
                     * Wird ausgelöst, wenn der Beitrag erfolgreich zur Sammlung hinzugefügt wurde. Die Methode zeigt eine Erfolgsmeldung an.
                     */
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Erfolgreich hinzugefügt
                        Toast.makeText(activity, "Beitrag erfolgreich hinzugefügt.", Toast.LENGTH_SHORT).show();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("hatBeiträge", true);
                        userDocRef.update(updates);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    /**
                     * Wird ausgeführt, falls es beim Hinzufügen einen Fehler gab. Die Methode zeigt eine Fehlermeldung an.
                     * @param e Fehlermeldung
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Fehler beim Hinzufügen
                        Toast.makeText(activity, "Fehler beim Hinzufügen des Beitrags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Methode, welche aufgerufen wird, um einen bestimmten Post zu löschen. Die Methode ändert die Eigenschaft "gelöscht" auf true,
     * wodurch der Beitrag den Nutzern nicht mehr angezeigt wird. Dennoch bleibt der Beitrag in Firebase gepeichert, um Konflikte zu
     * vermeiden und bei Unklarheiten ihn später noch betrachten zu können.
     * @param userID Nutzer-ID, des Beitrages, welcher gelöscht werden soll.
     * @param date Veröffentlichungsdatum, des Beitrages, welcher gelöscht werden soll.
     * @param activity Aktivität, von welcher die Methode gestartet wurde.
     */
    public static void deletePost(String userID, Date date, Activity activity){
        findPostByDateAndUserID(userID, date, new OnPostFoundListener() {
            /**
             * Wenn der gesuchte Post gefunden wurde wird hier "gelöscht" auf wahr gesetzt.
             * @param documentPath Dokumentenpfad des Posts, welcher gelöscht werden soll.
             */
            @Override
            public Task<List<Beitrag>> onPostFound(String documentPath) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = firestore.document(documentPath);
                Map<String, Object> updates = new HashMap<>();
                updates.put("gelöscht", true);
                docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(activity, "Der Beitrag wurde erfolgreich gelöscht", Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

            /**
             * Wenn der Post nicht gefunden wurde liegt es daran, dass es sich um einen Kommentar handelt. Ist dies der Fall,
             * so kommt noch die Fehlermeldung, dass Kommentare nicht gelöscht werden können.
             * In Zukunft kann man hier eine Methode aufrufen, welche auf Kommentare finden kann.
             */
            @Override
            public void onPostNotFound() {
                Toast.makeText(activity, "Kommentare können leider noch nicht gelöscht werden...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    /**
     * Die Methode testet, ob der Nutzer diesen Beitrag bereits liked oder noch nicht und gibt das entsprechende Ergebnis zurück.
     * Getestet wird dies über eine Liste, welche jeder Beitrag enthält und welche alle User-IDs speichert, die den Beitrag geliked haben.
     * @param userID Nutzer-ID der Person, welche den Beitrag veröffentlicht hat.
     * @param date Datum, an welchem der gesuchte Beitrag veröffentlicht wurde.
     * @return Der Nutzer hat den Beitrag schon geliked oder der Nutzer hat ihn noch nicht geliked.
     */
    public static Task<Boolean> isLikeVorhanden(String userID, Date date){

        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        Java.findPostByDateAndUserID(userID, date, new Java.OnPostFoundListener() {
            @Override
            public Task<List<Beitrag>> onPostFound(String documentPath) {

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = firestore.document(documentPath);
                String myUserID = FirebaseAuth.getInstance().getUid();
                CollectionReference collectionRef = docRef.collection("likedUser");
                Query query = collectionRef.whereEqualTo("userID", myUserID);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                taskCompletionSource.setResult(true);
                            }
                            else {
                                taskCompletionSource.setResult(false);
                            }
                        }
                        else {
                            taskCompletionSource.setResult(false);
                        }
                    }
                });
                return null;
            }

                @Override
                public void onPostNotFound() {
                }

                @Override
                public void onError(Exception e) {
                }
            });
        return taskCompletionSource.getTask();
    }

    /**
     * Diese Methode fügt einen Like zu einem Beitrag hinzu und setzt den Nutzer auf die Liste, um zu speichern, dass er den Beitrag
     * geliked hat. Somit kann des User den Beitrag nicht doppelt liken.
     * @param position Position des Beitrages in der Liste.
     * @param aUserID Nutzer-ID, der Person, welche den Beitrag veröffentlicht hat.
     * @param aDate Datum der Veröffentlichung.
     * @return
     */
    public static Task<Void> addLike(int position, String aUserID, Date aDate) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        String userID;
        Date date;
        String myUserID = FirebaseAuth.getInstance().getUid();
        if (!filteredList.isEmpty()) {
            userID = filteredList.get(position).getUserID();
            date = filteredList.get(position).getDate();
        } else {
            userID = aUserID;
            date = aDate;
        }
        Java.findPostByDateAndUserID(userID, date, new Java.OnPostFoundListener() {
            @Override
            public Task<List<Beitrag>> onPostFound(String documentPath) {

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = firestore.document(documentPath);

                CollectionReference collectionRef = docRef.collection("likedUser");
                Query query = collectionRef.whereEqualTo("userID", myUserID);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                Query query = collectionRef.whereEqualTo("userID", myUserID);

                                query.get().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        QuerySnapshot querySnapshot2 = task2.getResult();
                                        if (querySnapshot2 != null) {
                                            for (QueryDocumentSnapshot document : querySnapshot2) {
                                                DocumentReference documentReference = document.getReference();
                                                documentReference.delete();


                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("likes", FieldValue.increment(-1)); // "Likes" um 1 erhöhen

                                                // Führe die Aktualisierung aus
                                                docRef.set(updates, SetOptions.merge()) // Merge-Option, um vorhandene Daten beizubehalten
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                //Erfolgreich :)
                                                                taskCompletionSource.setResult(null);
                                                            }
                                                        })
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (!task.isSuccessful()) {
                                                                    //Fehler (Fehlermeldung hier irgendwo einbauen?)(Toast?)
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });
                            } else {
                                Map<String, Object> addUserID = new HashMap<>();
                                addUserID.put("userID", myUserID);

                                collectionRef.add(addUserID)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentReference documentReference = task.getResult();

                                                    // Aktualisiere das "Likes"-Feld um 1
                                                    Map<String, Object> updates = new HashMap<>();
                                                    updates.put("likes", FieldValue.increment(1)); // "Likes" um 1 erhöhen

                                                    // Führe die Aktualisierung aus
                                                    docRef.set(updates, SetOptions.merge()) // Merge-Option, um vorhandene Daten beizubehalten
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    //Erfolgreich :)
                                                                    taskCompletionSource.setResult(null);
                                                                }
                                                            })
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
                return null;
            }

            @Override
            public void onPostNotFound() {
            }

            @Override
            public void onError(Exception e) {
            }
        });
        return taskCompletionSource.getTask();
    }

    /**
     * Diese Methode ist dafür verantwortlich, dass Beiträge aus dem RecyclerView auch in Firebase gefunden werden. Mithilfe der User-ID
     * und des Veröffentlichungsdatums sucht sie den passenden Beitrag und gibt den Pfad, wo dieser in Firebase liegt, als String zurück.
     * Somit können die Beiträge auch in Firebase gefunden werden, obwohl sie selbst nicht ihren Pfad gespeichert haben.
     * Nachteil dieser Methode ist derzeitig noch, dass wenn derselbse User zur selben Zeit 2 Posts veröffentlicht, diese Methode
     * die beiden Posts nicht auseinanderhalten kann.
     * @param userID Nutzer-ID des Posts, welcher gefunden werden soll
     * @param date Veröffentlichungsdatum des Posts, welcher gefunden werden soll.
     */
    public static void findPostByDateAndUserID(String userID, Date date, final OnPostFoundListener listener) {
        CollectionReference usersCollection = db.collection("users");
        DocumentReference userDocRef = usersCollection.document(userID);
        CollectionReference postsCollection = userDocRef.collection("Beiträge");

        Query query = postsCollection.whereEqualTo("date", date);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Es wurde mindestens ein Beitrag gefunden
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        String documentPath = document.getReference().getPath();
                        listener.onPostFound(documentPath);
                    } else {
                        listener.onPostNotFound();
                    }
                } else {
                    listener.onError(task.getException());
                }
            }
        });
    }

    /**
     * Methode, welche aufgerufen wird, um einem Nutzer zu folgen
     * @param userID Nutzer-ID des Nutzers, welchem gefolgt werden soll.
     * @param username Nutzername des Nutzers, welchem gefolgt werden soll.
     * @param activity Aktivität, von wo die Methode aufgerufen wird.
     */
    public static void followUser(String userID, String username, Activity activity) {
        String myUserID = FirebaseAuth.getInstance().getUid();
        CollectionReference usersCollection = db.collection("users");
        DocumentReference userDocRef = usersCollection.document(myUserID);
        CollectionReference collectionRef = userDocRef.collection("followedUser");

        Map<String, Object> follow = new HashMap<>();
        follow.put("userID", userID);

        Query query = collectionRef.whereEqualTo("userID", userID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot.isEmpty()) {
                        if (!userID.equals(myUserID)) {
                            collectionRef.add(follow)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                showToast(activity, "Du folgst jetzt " + username + ".", Toast.LENGTH_SHORT);
                                            }
                                        }
                                    });
                        } else {
                            showToast(activity, "Du kannst dir nicht selbst folgen.", Toast.LENGTH_SHORT);
                        }
                    } else {
                        // Benutzer folgt bereits, also entferne ich ihn aus der Liste
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    showToast(activity, "Du folgst " + username + " nicht mehr.", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * Testet, ob der eingeloggte Nutzer dem übergebenen Nutzer folgt.
     * @param userID Nutzer-ID des Nutzers, welcher überprüft werden soll.
     * @return Wahr oder falsch, je nachdem ob man dem Benutzer bereits folgt.
     */
    public static Task<Boolean> folgeIch(String userID) {
        String myUserID = FirebaseAuth.getInstance().getUid();
        CollectionReference usersCollection = db.collection("users");
        DocumentReference userDocRef = usersCollection.document(myUserID);
        CollectionReference collectionRef = userDocRef.collection("followedUser");

        Query query = collectionRef.whereEqualTo("userID", userID);

        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    boolean isFollowing = !querySnapshot.isEmpty() && !userID.equals(myUserID);
                    taskCompletionSource.setResult(isFollowing);
                } else {
                    taskCompletionSource.setException(Objects.requireNonNull(task.getException()));
                }
            }
        });
        return taskCompletionSource.getTask();
    }

    public interface OnPostFoundListener {
        Task<List<Beitrag>> onPostFound(String documentPath);

        void onPostNotFound();

        void onError(Exception e);
    }

    /**
     * Diese Methode lädt alle Kommentare für einen bestimmten Beitrag.
     * @param userID Nutzer-ID der Perosn, welche den Beitrag veröffentlicht hat, für den die Kommentare geladen werden sollen.
     * @param date Veröffentlichungsdatum, des Beitrages, für den die Kommentare geladen werden sollen.
     * @param recyclerView Recycler View, in welchen die Daten geschrieben werden sollen.
     * @param myAdapter Adapter des Recycler Views
     * @param activity Aktivität, von wo aus die Methode aufgerufen wurde
     * @return Liste an Kommentaren
     */
    public static Task<List<Beitrag>> ladeAlleKommentare(String userID, Date date, RecyclerView recyclerView, MyCommentAdapter myAdapter, Activity activity) {
        alleKommentare.clear();
        final String[] documentPath = new String[1];
        Java.findPostByDateAndUserID(userID, date, new Java.OnPostFoundListener() {
            @Override
            public Task<List<Beitrag>> onPostFound(String aDocumentPath) {
                documentPath[0] = aDocumentPath;

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentRef = db.document(documentPath[0]);
                CollectionReference commentsCollectionRef = documentRef.collection("comments");

                return commentsCollectionRef
                        .get()
                        .continueWith(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Beitrag beitrag = document.toObject(Beitrag.class);
                            if (beitrag != null && !beitrag.getGelöscht()) {
                                alleKommentare.add(beitrag);
                            }
                        }
                        Java.sortByDate(alleKommentare);
                        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                        myAdapter.setItemList(alleKommentare);
                        recyclerView.setAdapter(myAdapter);
                        return alleKommentare;
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
            }

            @Override
            public void onPostNotFound() {
            }

            @Override
            public void onError(Exception e) {
            }
        });
        return Tasks.forResult(new ArrayList<Beitrag>());
    }

    /**
     * Die Methode gibt die Liste "alleKommentare" zurück.
     * @return Liste "alleKommentare"
     */
    public static List<Beitrag> alleKommentare(){
        return alleKommentare;
    }

    /**
     * Die Methode lädt alle in Firebase hochgeladenen Beiträge
     * @return Alle Beiträge, welche hochgeladen wurden.
     */
    public static Task<List<Beitrag>> ladeAlleBeiträge() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userCollectionRef = db.collection("users");
        List<Beitrag> alleBeitraegeListe = new ArrayList<>();
        filteredList.clear();

        return userCollectionRef.whereEqualTo("hatBeiträge", true)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        List<String> userListe = new ArrayList<>();

                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        for (DocumentSnapshot document : documents) {
                            userListe.add(document.getId());
                        }

                        List<Task<List<Beitrag>>> beitraegeTasks = new ArrayList<>();

                        for (String user : userListe) {
                            Task<List<Beitrag>> taskB = ladeBeitraegeFuerBenutzer(user);
                            beitraegeTasks.add(taskB);
                        }
                        return Tasks.whenAllSuccess(beitraegeTasks);
                    } else {
                        return Tasks.forResult(new ArrayList<List<Beitrag>>());
                    }
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<List<Beitrag>> beitraegeErgebnisse = task.getResult();
                        for (List<Beitrag> beitraegeListe : beitraegeErgebnisse) {
                            alleBeitraegeListe.addAll(beitraegeListe);
                        }
                    }
                    return alleBeitraegeListe;
                });
    }

    /**
     * Diese Methode lädt alle Beiträge eines Benutzers. Sie wird z.B. aufgerufen von der Methode "ladeAlleBeiträge", um Beiträge der
     * einzelnen Benutzer zu laden.
     * @param userId Nutzer-ID des Nutzers, von dem die Beiträge geladen werden sollen.
     * @return Liste der Beiträge des Nutzers.
     */
    public static Task<List<Beitrag>> ladeBeitraegeFuerBenutzer(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userCollectionRef = db.collection("users");

        return userCollectionRef.document(userId).collection("Beiträge")
                .whereEqualTo("gelöscht", false)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Beitrag> beitraegeListe = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Beitrag beitrag = document.toObject(Beitrag.class);
                            beitraegeListe.add(beitrag);
                        }
                        return beitraegeListe;
                    } else {
                        return null;
                    }
                });
    }

    /**
     * Die Methode wird aufgerufen, um die Liste "beitraegeListe" zu filtern. So werden nur die Beiträge in die neue Liste hinzugefügt,
     * welche den übergebenen Text enthalten.
     * @param text Text, nach welchem gesucht werden soll. Es werden sowohl die Inhalte als auch die Nutzernamen durchsucht.
     * @param beitraegeListe Liste mit allen Beiträgen, welche gefiltert werden soll.
     * @param activity Aktivität, von wo die Methode aufgerufen wird.
     * @param myAdapter Adapter, wo die Liste gesetzt werden soll.
     */
    public static void filterList(String text, List<Beitrag> beitraegeListe, Activity activity, MyAdapter myAdapter) {
        filteredList.clear();
        if (beitraegeListe != null) {
            for (Beitrag beitrag : beitraegeListe) {
                if (beitrag.getContent().toLowerCase().contains(text.toLowerCase()) || beitrag.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(beitrag);
                }
            }
        }
        if (filteredList.isEmpty()) {
            //Toast.makeText(activity, "Kein Beitrag gefunden", Toast.LENGTH_SHORT).show();
        } else {
            myAdapter.setFilteredList(filteredList);
        }
    }

    /**
     * Diese Methode wandelt das Datum eines Beitrages so ab, dass sofern der Beitrag erst vor einer gewissen Zeit hochgeladen wurde,
     * ein String zurückgegeben wird, in welchem steht "Vor x Minuten hochgeladen.
     * @param date
     * @return String Wert mit passender Formatierung.
     */
    public static String dateCheck(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        GregorianCalendar now = new GregorianCalendar();
        long timeDifferenceMillis = date.getTime() - now.getTime().getTime();
        long minutesDifference = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long hoursDifference = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis);
        if (minutesDifference > -60) {
            return ("Vor " + minutesDifference * -1 + " Minuten hochgeladen");
        } else if (hoursDifference >= -12) {
            return ("Vor " + hoursDifference * -1 + " Stunden hochgeladen");
        } else {
            return ("Hochgeladen am: " + sdf.format(date));
        }
    }

    /**
     * Die Methode wird aufgerufen, um alle Beiträge oder um alle Beiträge aller gefolgten Personen zu laden.
     * @param recyclerView Recycler View, in welchen die Beiträge geschrieben werden sollen.
     * @param myAdapter Adapter des Recycler Views.
     * @param activity Aktivität, von wo die Methode aufgerufen wird.
     * @param beiträge Gibt an, ob alle Beiträge geladen werden sollen (0) oder nur die von Personen denen man folgt (1)
     * @return Liste aller Beiträge
     */
    public static Task<List<Beitrag>> load(RecyclerView recyclerView, MyAdapter myAdapter, Activity activity, int beiträge) {
        Task<List<Beitrag>> task;
        final TaskCompletionSource<List<Beitrag>> taskCompletionSource;
        if (beiträge == 0) {
            task = Java.ladeAlleBeiträge();

            taskCompletionSource = new TaskCompletionSource<>();
        } else {
            task = Java.ladeGefolgteBeiträge();

            taskCompletionSource = new TaskCompletionSource<>();
        }
        task.addOnCompleteListener(new OnCompleteListener<List<Beitrag>>() {
            @Override
            public void onComplete(Task<List<Beitrag>> completedTask) {
                if (completedTask.isSuccessful()) {
                    List<Beitrag> beitraegeListe = completedTask.getResult();
                    Java.sortByDate(beitraegeListe);

                    recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                    myAdapter.setFilteredList(beitraegeListe);
                    recyclerView.setAdapter(myAdapter);

                    taskCompletionSource.setResult(beitraegeListe);
                } else {
                    taskCompletionSource.setException(Objects.requireNonNull(completedTask.getException()));
                }
            }
        });

        return taskCompletionSource.getTask();
    }

    /**
     * Lädt alle Beiträge von Personen denen man folgt.
     * @return Liste von Beiträgen, von Personen denen man folgt.
     */
    public static Task<List<Beitrag>> ladeGefolgteBeiträge() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userCollectionRef = db.collection("users");
        List<Beitrag> alleBeitraegeListe = new ArrayList<>();
        filteredList.clear();
        String userID = FirebaseAuth.getInstance().getUid();

        return userCollectionRef.document(userID).collection("followedUser")
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        List<String> userListe = new ArrayList<>();
                        List<Task<List<Beitrag>>> beitraegeTasks = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            String user = document.getString("userID");
                            userListe.add(user);
                            Task<List<Beitrag>> taskB = ladeBeitraegeFuerBenutzer(user);
                            beitraegeTasks.add(taskB);
                        }
                        return Tasks.whenAllSuccess(beitraegeTasks);
                    } else {
                        return Tasks.forResult(new ArrayList<List<Beitrag>>());
                    }
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<List<Beitrag>> beitraegeErgebnisse = task.getResult();
                        for (List<Beitrag> beitraegeListe : beitraegeErgebnisse) {
                            alleBeitraegeListe.addAll(beitraegeListe);
                        }
                    }
                    return alleBeitraegeListe;
                });
    }
}