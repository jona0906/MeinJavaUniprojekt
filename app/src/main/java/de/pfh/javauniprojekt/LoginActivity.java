package de.pfh.javauniprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Diese Klasse ist für den Login des Nutzers zuständig.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private ImageButton login;
    private Button passwordLost;
    private FirebaseAuth auth;

    /**
     * Die Methode wird aufgerufen, wenn die Aktivität erstellt wird. Sie macht grundlegende Dinge und legt die On Click Listener fest.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        passwordLost = findViewById(R.id.passwordLost);
        auth = FirebaseAuth.getInstance();

        passwordLost.setVisibility(View.VISIBLE);

        passwordLost.setOnClickListener(new View.OnClickListener(){
            /**
             * Wenn das Passwort vergessen wurde hat der Nutzer mithilfe von diesem Knopf die Möglichkeit sich eine Mail senden zu lassen,
             * um das Passwort zurückzusetzen.
             */
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                passwordLost.setVisibility(View.INVISIBLE);
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_email))
                {
                    Toast.makeText(LoginActivity.this, "Bitte gib zuerst deine E-Mail Adresse ein.", Toast.LENGTH_SHORT).show();
                    passwordLost.setVisibility(View.VISIBLE);
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
                                                String passwordZuruecksetzenErneut = "Passwort zurücksetzen erneut versuchen";
                                                passwordLost.setText(passwordZuruecksetzenErneut);
                                                passwordLost.setVisibility(View.VISIBLE);
                                            }
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Diese E-Mail-Adresse ist nicht registriert.", Toast.LENGTH_SHORT).show();
                                String passwordZuruecksetzenErneut = "Passwort zurücksetzen erneut versuchen";
                                passwordLost.setText(passwordZuruecksetzenErneut);
                                passwordLost.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Fehler beim Überprüfen der E-Mail-Registrierung.", Toast.LENGTH_SHORT).show();
                            String passwordZuruecksetzenErneut = "Passwort zurücksetzen erneut versuchen";
                            passwordLost.setText(passwordZuruecksetzenErneut);
                            passwordLost.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            /**
             * Diese Methode überprüft, ob Passwort und Mail vorhanden sind und gibt dann an eine Methode weiter, um den Nutzer
             * anzumelden.
             */
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password))
                {
                    Toast.makeText(LoginActivity.this, "Bitte gib dein Passwort und deine E-Mail Adresse ein.", Toast.LENGTH_SHORT).show();
                }
                else {
                    loginUser(txt_email, txt_password);
                }
            }
        });
    }

    /**
     * Diese Methode versucht den Nutzer anzumelden.
     * @param email Mail des Nutzers.
     * @param password Passwort des Nutzers.
     */
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
            Toast.makeText(LoginActivity.this, "Login fehlgeschlagen, überprüfe deine Anmeldeinformationen.", Toast.LENGTH_SHORT).show();
        }
    });
    }
}