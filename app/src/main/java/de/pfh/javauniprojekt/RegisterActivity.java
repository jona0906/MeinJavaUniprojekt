package de.pfh.javauniprojekt;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Möchte ein Nutzer einen neuen Account anlegen, so startet diese Aktivität.
 * Sie zeigt das entsprechende Layout an, testet ob die Informationen vollständig eingegeben wurden und führt letzendlich Methoden aus,
 * um den Nutzer im System anzulegen.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private ImageButton register;
    private EditText username;
    private FirebaseAuth auth;

    /**
     * Beim starten des Programmes werden Variablen mit der Aktivität verknüpft. Zudem werden standartmäßige Einstellungen getätigt und
     * es werden OnClickListener gesetzt, um zu überwachen auf welche Knöpfe der User drückt.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        username = findViewById(R.id.username);

        register.setOnClickListener(new View.OnClickListener() {
            /**
             * Hier wird überprüft, ob die eingegebenen Daten vollständig sind. Ist dies der Fall werden Methoden aufgerufen, um den Nutzer
             * anzulegen. Zudem testen diese Methoden, ob es bereits einen Nutzer mit dem Nutzernamen gibt.
             */
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