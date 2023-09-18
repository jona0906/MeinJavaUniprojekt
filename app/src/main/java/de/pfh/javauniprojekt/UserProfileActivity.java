package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private Button logout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle("Meine Daten");

        logout = findViewById(R.id.logout);

        TextView nutzer = findViewById(R.id.nutzer);
        TextView textViewUsername = findViewById(R.id.username);

        String angemeldetAls = "Angemeldet als: " + FirebaseAuth.getInstance().getCurrentUser().getEmail();
        nutzer.setText(angemeldetAls);
        textViewUsername.setText(username);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(UserProfileActivity.this, "Erfolgreich abgemeldet", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserProfileActivity.this, StartActivity.class));
                finish();
            }
        });
    }
}