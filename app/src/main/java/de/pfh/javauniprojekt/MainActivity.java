package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

/**
 * Diese Klasse zeigt ist der Hauptpart des Programmes. Sie beinhaltet unteranderem den RecyclerView, in dem alle Beiträge angezeigt werden.
 * Von hier aus kann der Benutzer in viele andere Aktivitäten navigieren.
 */
public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {
    private ImageButton ic_user;
    private ImageButton ic_newPost;
    private List<Beitrag> beitraegeListe;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private MyAdapter myAdapter;
    private String myUsername;
    private ImageButton followedPosts;
    private ImageButton allPosts;
    private String userId;

    /**
     * Beim starten werden die Variablen verknüpft und der RecyclerView mit allen Beiträgen wird geladen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ic_user = findViewById(R.id.ic_user);
        ic_newPost = findViewById(R.id.ic_newPost);
        recyclerView = findViewById(R.id.recyclerview);
        searchView = findViewById(R.id.searchView);
        followedPosts = findViewById(R.id.followedPosts);
        allPosts = findViewById(R.id.allPosts);
        searchView.clearFocus();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        myUsername(documentSnapshot.getString("username"));
                    }
                });
        myAdapter = new MyAdapter(getApplicationContext(), MainActivity.this, recyclerView, MainActivity.this);

        Task<List<Beitrag>> loadDataTask = Java.load(recyclerView, myAdapter, MainActivity.this, 0);

        loadDataTask.addOnCompleteListener(new OnCompleteListener<List<Beitrag>>() {
            /**
             * Die Liste "beitraegeListe" wird auf die Daten von Firebase gesetzt.
             */
            @Override
            public void onComplete(Task<List<Beitrag>> task) {
                if (task.isSuccessful()) {
                    beitraegeListe = task.getResult();
                }
            }
        });

        ic_user.setOnClickListener(new View.OnClickListener() {
            /**
             * Die Nutzerdetails werden in einer neune Aktivität angezeigt.
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.putExtra("username", myUsername);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        ic_newPost.setOnClickListener(new View.OnClickListener() {
            /**
             * Neuer Post wird erstellt, weswegen eine entsprechende Aktivität aufgerufen wird.
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                intent.putExtra("username", myUsername);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Java.filterList(newText, beitraegeListe, MainActivity.this, myAdapter);
                return true;
            }
        });

        allPosts.setOnClickListener(new View.OnClickListener() {
            /**
             * Filtert die Posts so, dass alle Posts angezeigt werden.
             */
            @Override
            public void onClick(View view) {
                Task<List<Beitrag>> loadDataTask = Java.load(recyclerView, myAdapter, MainActivity.this, 0);

                loadDataTask.addOnCompleteListener(new OnCompleteListener<List<Beitrag>>() {
                    @Override
                    public void onComplete(Task<List<Beitrag>> task) {
                        if (task.isSuccessful()) {
                            beitraegeListe = task.getResult();
                        }
                    }
                });
            }
        });

        followedPosts.setOnClickListener(new View.OnClickListener() {
            /**
             * Filtert die Posts so, dass nur diejenigen angezeigt werden von den Leuten denen man folgt.
             */
            @Override
            public void onClick(View view) {
                Task<List<Beitrag>> loadDataTask = Java.load(recyclerView, myAdapter, MainActivity.this, 1);
                loadDataTask.addOnCompleteListener(new OnCompleteListener<List<Beitrag>>() {
                    @Override
                    public void onComplete(Task<List<Beitrag>> task) {
                        if (task.isSuccessful()) {
                            beitraegeListe = task.getResult();
                        }
                    }
                });
            }
        });
    }

    /**
     * Setzt den eigenen Nutzernamen fest.
     * @param username Eigener Nutzername.
     */
    private void myUsername(String username) {
        myUsername = username;
    }

    /**
     * Verhindert, dass man durch das drücken auf den zurück Knopf die Anwendung verlässt.
     */
    @Override
    public void onBackPressed() { //Ich habe diese Methode eingebaut, damit man nicht zurück navigieren kann.
    }

    /**
     * Wenn ein Item aus dem RecyclerView mit den Beiträgen angeklickt wird, dann ruft diese Methode eine neue
     * Aktivität auf, wo man mehr Details zu dem Beitrag einsehen kann, sowie kommentierne kann.
     * @param position Position des Beitrages, auf welchen geklickt wird.
     */
    public void onItemClick(int position) {
        List<Beitrag> items = MyAdapter.getItems();
        Beitrag beitrag = new Beitrag(items.get(position).getContent(), items.get(position).getUsername(), items.get(position).getUserID());
        Intent intent = new Intent(MainActivity.this, BeitragActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Datum", items.get(position).getDate());
        intent.putExtra("Beitrag", beitrag);
        intent.putExtra("Kommentar", false);
        intent.putExtra("username", myUsername);
        intent.putExtra("uid", userId);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
