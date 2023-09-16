package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.List;

public class BeitragActivity extends AppCompatActivity implements RecyclerViewInterface {
    private Beitrag beitrag;
    private TextView username;
    private TextView date;
    private TextView content;
    private ImageButton followButton;
    private String userID;
    private RecyclerView recyclerView;
    private Button kommentarHinzufügen;
    private TextView kommentar;
    private List<Beitrag> alleKommentare;
    private Boolean kommentarVorhanden;
    private String myUsername;
    private String myUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beitrag);
        beitrag = getIntent().getParcelableExtra("Beitrag");
        Bundle bundle = getIntent().getExtras();
        Date dateObjekt = (Date) bundle.getSerializable("Datum");
        kommentarVorhanden = bundle.getBoolean("Kommentar");
        myUsername = bundle.getString("username");
        myUID = bundle.getString("uid");

        content = findViewById(R.id.content);
        username = findViewById(R.id.username);
        date = findViewById(R.id.date);
        followButton = findViewById(R.id.followButton);
        recyclerView = findViewById(R.id.recyclerViewComment);
        kommentar = findViewById(R.id.kommentar);
        kommentarHinzufügen = findViewById(R.id.kommentarHinzufügen);

        userID = beitrag.getUserID();
        content.setText(beitrag.getContent());
        username.setText(beitrag.getUsername());
        date.setText(Java.dateCheck(dateObjekt));



        if(!kommentarVorhanden)
        {
            kommentar.setVisibility(View.VISIBLE);
            kommentarHinzufügen.setVisibility(View.VISIBLE);
            MyCommentAdapter myAdapter = new MyCommentAdapter(getApplicationContext(), BeitragActivity.this, recyclerView, BeitragActivity.this);
            Java.ladeAlleKommentare(beitrag.getUserID(), dateObjekt, recyclerView, myAdapter, BeitragActivity.this);

            kommentarHinzufügen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("BeitragActivity", "onClick: " + dateObjekt + "   " + beitrag.getUserID());
                    Java.addComment(BeitragActivity.this, kommentar.getText().toString(),myUsername, myUID, dateObjekt, beitrag.getUserID());
                    Java.ladeAlleKommentare(beitrag.getUserID(), dateObjekt, recyclerView, myAdapter, BeitragActivity.this);
                    kommentar.setText("");
                }
            });
        }
        else {
            kommentar.setVisibility(View.INVISIBLE);
            kommentarHinzufügen.setVisibility(View.INVISIBLE);
        }


        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Java.followUser(userID, username.getText().toString(),BeitragActivity.this);
            }
        });
    }
    public void onItemClick(int position) {
        alleKommentare = Java.alleKommentare();
        Beitrag beitrag = new Beitrag(alleKommentare.get(position).getContent(), alleKommentare.get(position).getUsername(), alleKommentare.get(position).getUserID());
        Intent intent = new Intent(BeitragActivity.this, BeitragActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Datum", alleKommentare.get(position).getDate());
        intent.putExtra("Beitrag", beitrag);
        intent.putExtra("Kommentar", true);
        intent.putExtra("username", myUsername);
        intent.putExtra("uid", myUID);
        intent.putExtras(bundle);
        startActivity(intent);


    }
}