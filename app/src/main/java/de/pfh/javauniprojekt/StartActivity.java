package de.pfh.javauniprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private Button register;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        FirebaseApp.initializeApp(this);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
        }
        else
        {
            register = findViewById(R.id.register);
            login = findViewById(R.id.login);

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(StartActivity.this, LoginActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(StartActivity.this, RegisterActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }
}