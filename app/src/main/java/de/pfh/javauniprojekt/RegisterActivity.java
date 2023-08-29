package de.pfh.javauniprojekt;

//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button register;
    private EditText username;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Registrieren");

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        username = findViewById(R.id.username);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String txt_username = username.getText().toString().replace(" ", "");

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_username)) {
                    Toast.makeText(RegisterActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password zu kurz", Toast.LENGTH_SHORT).show();
                } else {
                    Java.istVerfuegbar(txt_username)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    boolean istVerfuegbar = task.getResult();
                                    if (istVerfuegbar) {
                                        // Der Benutzername ist verfügbar
                                        if (txt_username.length() < 4) {
                                            Toast.makeText(RegisterActivity.this, "Benutzername ist zu kurz", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Java.registerUser(txt_email, txt_password, txt_username, auth, RegisterActivity.this);
                                        }
                                    } else {
                                        // Der Benutzername ist bereits vergeben
                                        Toast.makeText(RegisterActivity.this, "Der Benutzername ist bereits vergeben", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}