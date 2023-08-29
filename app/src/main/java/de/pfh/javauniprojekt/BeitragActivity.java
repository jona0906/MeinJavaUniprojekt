package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class BeitragActivity extends AppCompatActivity {
    private Beitrag beitrag;
    private TextView username;
    private TextView date;
    private TextView content;
    private ImageButton followButton;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beitrag);
        beitrag = getIntent().getParcelableExtra("Beitrag");
        Bundle bundle = getIntent().getExtras();
        Date dateObjekt = (Date) bundle.getSerializable("Datum");

        content = findViewById(R.id.content);
        username = findViewById(R.id.username);
        date = findViewById(R.id.date);
        followButton = findViewById(R.id.followButton);

        userID = beitrag.getUserID();
        content.setText(beitrag.getContent());
        username.setText(beitrag.getUsername());
        date.setText(Java.dateCheck(dateObjekt));

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Java.followUser(userID, username.getText().toString(),BeitragActivity.this);
            }
        });
    }
}