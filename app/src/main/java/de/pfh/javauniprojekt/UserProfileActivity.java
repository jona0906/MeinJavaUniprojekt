package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        getSupportActionBar().setTitle("Meine Daten");

        logout = findViewById(R.id.logout);

        TextView nutzer = findViewById(R.id.nutzer);
        TextView textViewUsername = findViewById(R.id.username);

        nutzer.setText("Angemeldet als: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        textViewUsername.setText(username);
        /*FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String usernameShow = "Benutzername: "+ username;
                        textViewUsername.setText(usernameShow);
                    }
                }); */
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