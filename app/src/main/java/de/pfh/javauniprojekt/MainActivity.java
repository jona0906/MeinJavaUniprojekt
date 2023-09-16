package de.pfh.javauniprojekt;

import static de.pfh.javauniprojekt.Java.ladeBeitraegeFuerBenutzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {

    private ImageButton ic_user;
    private ImageButton ic_newPost;
    private ImageButton ic_reload;
    private List<Beitrag> beitraegeListe;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private MyAdapter myAdapter;
    private String myUsername;
    private ImageButton followedPosts;
    private ImageButton allPosts;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ic_user = findViewById(R.id.ic_user);
        ic_newPost = findViewById(R.id.ic_newPost);
        ic_reload = findViewById(R.id.reload);
        recyclerView = findViewById(R.id.recyclerview);
        searchView = findViewById(R.id.searchView);
        followedPosts = findViewById(R.id.followedPosts);
        allPosts = findViewById(R.id.allPosts);
        searchView.clearFocus();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
            @Override
            public void onComplete(Task<List<Beitrag>> task) {
                if (task.isSuccessful()) {
                    beitraegeListe = task.getResult();
                }
            }
        });

        ic_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.putExtra("username", myUsername);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            }
        });

        ic_reload.setOnClickListener(new View.OnClickListener() {
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

        ic_newPost.setOnClickListener(new View.OnClickListener() {
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

    private void myUsername(String username) {
        myUsername = username;
    }


    @Override
    public void onBackPressed() { //Ich habe diese Methode eingebaut, damit man nicht zurück navigieren kann.
        // Wenn noch genug Zeit, dann Abfrage einführen "Möchten sie die App verlassen"
    }


    public void onItemClick(int position) {
        Beitrag beitrag = new Beitrag(beitraegeListe.get(position).getContent(), beitraegeListe.get(position).getUsername(), beitraegeListe.get(position).getUserID());
        Intent intent = new Intent(MainActivity.this, BeitragActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Datum", beitraegeListe.get(position).getDate());
        intent.putExtra("Beitrag", beitrag);
        intent.putExtra("Kommentar", false);
        intent.putExtra("username", myUsername);
        intent.putExtra("uid", userId);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
