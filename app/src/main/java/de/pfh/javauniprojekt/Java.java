package de.pfh.javauniprojekt;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Java {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static CollectionReference usersCollection = db.collection("users");

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

    public static void registerUser(String email, String password, String username, FirebaseAuth auth, Activity activity) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    showToast(activity, "Registrierung erfolgreich", Toast.LENGTH_LONG);
                    saveUsername(username, FirebaseAuth.getInstance().getCurrentUser().getUid(), activity);
                    activity.startActivity(new Intent(activity, StartActivity.class));
                    activity.finish();
                } else {
                    showToast(activity, "Ups, da ist ein Fehler aufgetreten", Toast.LENGTH_SHORT);
                }
            }
        });

    }

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

    private static void showToast(Activity activity, String message, int duration) {
        Toast.makeText(activity, message, duration).show();
    }

    public static void sortByDate(List<Beitrag> alleBeiträge) {
        Collections.sort(alleBeiträge, new Comparator<Beitrag>() {
            @Override
            public int compare(Beitrag beitrag1, Beitrag beitrag2) {
                return beitrag2.getDate().compareTo(beitrag1.getDate());
            }
        });
    }

    public static void addPost(Activity activity, String newPost, String username, String uid, boolean statement) {
        String userId = FirebaseAuth.getInstance().getUid();
        Map<String, Object> postData = new HashMap<>();
        postData.put("date", Calendar.getInstance().getTime());
        postData.put("username", username);
        postData.put("userID", uid);
        postData.put("likes", 0);
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
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Erfolgreich hinzugefügt
                        Toast.makeText(activity, "Beitrag erfolgreich hinzugefügt.", Toast.LENGTH_SHORT).show();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("hatBeiträge", true);
                        userDocRef.update(updates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "hatBeiträge auf true gesetzt");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Firestore", "Fehler beim Aktualisieren von hatBeiträge", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Fehler beim Hinzufügen
                        Toast.makeText(activity, "Fehler beim Hinzufügen des Beitrags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void addLike(int position, String aUserID, Date aDate) {
        String userID;
        Date date;
        String myUserID = FirebaseAuth.getInstance().getUid();
        if (!filteredList.isEmpty()) {
            Log.d("Java", "Nutze filtered List.");
            userID = filteredList.get(position).getUserID();
            date = filteredList.get(position).getDate();
        } else {
            userID = aUserID;
            date = aDate;
        }
        Log.d("Java", "onPostFound: " + userID + " " + date);
        Java.findPostByDateAndUserID(userID, date, new Java.OnPostFoundListener() {
            @Override
            public void onPostFound(String documentPath) {

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

                                                } else {
                                                    // Hier auch Fehler

                                                }
                                            }
                                        });


                            }
                        } else {
                            // Fehler

                        }
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

    }


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
                    Log.d("Java", "onComplete: " + task.getResult().getDocuments().get(0).getReference().getPath());
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
                        showToast(activity, "Du folgst " + username + " bereits.", Toast.LENGTH_SHORT);
                    }
                }
            }
        });
    }

    public interface OnPostFoundListener {
        void onPostFound(String documentPath);

        void onPostNotFound();

        void onError(Exception e);
    }


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
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("Java", "Fehler beim Laden der Benutzer: " + exception.getMessage());
                        }
                        return Tasks.forResult(new ArrayList<List<Beitrag>>());
                    }
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<List<Beitrag>> beitraegeErgebnisse = task.getResult();
                        for (List<Beitrag> beitraegeListe : beitraegeErgebnisse) {
                            alleBeitraegeListe.addAll(beitraegeListe);
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("Java", "Fehler beim Laden der Beiträge: " + exception.getMessage());
                        }
                    }
                    return alleBeitraegeListe;
                });
    }

    public static Task<List<Beitrag>> ladeBeitraegeFuerBenutzer(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userCollectionRef = db.collection("users");

        return userCollectionRef.document(userId).collection("Beiträge").get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Beitrag> beitraegeListe = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Beitrag beitrag = document.toObject(Beitrag.class);
                            beitraegeListe.add(beitrag);
                        }
                        return beitraegeListe;
                    } else {
                        //Fehler
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("Java", "Fehler beim Laden der Beiträge: " + exception.getMessage());
                        }
                        return null;
                    }
                });
    }

    private static List<Beitrag> filteredList = new ArrayList<>();

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
            Toast.makeText(activity, "Kein Beitrag gefunden", Toast.LENGTH_SHORT).show();
        } else {
            myAdapter.setFilteredList(filteredList);
        }
    }

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
                    taskCompletionSource.setException(completedTask.getException());
                }
            }
        });

        return taskCompletionSource.getTask();
    }

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