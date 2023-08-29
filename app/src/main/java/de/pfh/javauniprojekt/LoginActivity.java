package de.pfh.javauniprojekt;

//import android.support.v7.app.AppCompatActivity; //Hat mal nicht funktioniert
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private Button passwordLost;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Anmelden");

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        passwordLost = findViewById(R.id.passwordLost);
        auth = FirebaseAuth.getInstance();

        passwordLost.setVisibility(View.VISIBLE);

        passwordLost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                passwordLost.setVisibility(View.INVISIBLE);
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_email))
                {
                    Toast.makeText(LoginActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Java.isEmailRegistered(txt_email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isRegistered = task.getResult();
                            if (isRegistered) {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(txt_email)
                                        .addOnCompleteListener(passwordResetTask -> {
                                            if (passwordResetTask.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Passwort zurückgesetzt. Überprüfen Sie Ihre E-Mails.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Fehler beim Zurücksetzen des Passworts.", Toast.LENGTH_SHORT).show();
                                                passwordLost.setText("Erneut versuchen");
                                                passwordLost.setVisibility(View.VISIBLE);
                                            }
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Diese E-Mail-Adresse ist nicht registriert.", Toast.LENGTH_SHORT).show();
                                passwordLost.setText("Erneut versuchen");
                                passwordLost.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Fehler beim Überprüfen der E-Mail-Registrierung.", Toast.LENGTH_SHORT).show();
                            passwordLost.setText("Erneut versuchen");
                            passwordLost.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password))
                {
                    Toast.makeText(LoginActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    loginUser(txt_email, txt_password);
                }
            }
        });
    }
    public void loginUser(String email, String password)
    {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(LoginActivity.this, "Erfolgreich angemeldet", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(LoginActivity.this, "Login fehlgeschlagen. Überprüfen Sie Ihre Anmeldeinformationen.", Toast.LENGTH_SHORT).show();
        }
    });
    }
}