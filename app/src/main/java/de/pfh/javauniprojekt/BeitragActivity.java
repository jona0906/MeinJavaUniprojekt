package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.Date;
import java.util.List;

/**
 * Die Activity "BeitragActivity" ist dafür da, weitere Details der Beiträge anzuzeigen. Dadurch sehen die Nutzer nun Kommentare
 * und können selbst auch Kommentare zu einem Beitrag hinzufügen.
 */
public class BeitragActivity extends AppCompatActivity implements RecyclerViewInterface {
    private Beitrag beitrag;
    private TextView username;
    private TextView date;
    private TextView content;
    private ImageButton followButton;
    private ImageButton unfollowButton;
    private String userID;
    private RecyclerView recyclerView;
    private Button kommentarHinzufügen;
    private TextView kommentar;
    private List<Beitrag> alleKommentare;
    private boolean kommentarVorhanden;
    private String myUsername;
    private String myUID;
    private ImageView imageView;
    private Button deletePost;

    /**
     * Diese Methode startet, wenn die Aktivität gestartet wird. Sie führt die wesentlichen Schritte aus, um die Funktionen der Aktivität
     * aufzubauen. Sie verknüpft z.B. die TextViews und Buttons mit den xml Dateien oder testet, ob Kommentare vorhanden sind, um diese
     * anzuzeigen.
     */
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
        imageView = findViewById(R.id.imageView);
        unfollowButton = findViewById(R.id.unfollowButton);
        deletePost = findViewById(R.id.deletePost);

        userID = beitrag.getUserID();
        content.setText(beitrag.getContent());
        username.setText(beitrag.getUsername());
        date.setText(Java.dateCheck(dateObjekt));


        if(!kommentarVorhanden)
        {
            kommentar.setVisibility(View.VISIBLE);
            kommentarHinzufügen.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            MyCommentAdapter myAdapter = new MyCommentAdapter(getApplicationContext(), BeitragActivity.this, recyclerView, BeitragActivity.this);
            Java.ladeAlleKommentare(beitrag.getUserID(), dateObjekt, recyclerView, myAdapter, BeitragActivity.this);

            kommentarHinzufügen.setOnClickListener(new View.OnClickListener() {
                /**
                 * Die Methode wird aufgerufen, um ein Kommentar zu einem Beitrag hinzuzufügen.
                 */
                @Override
                public void onClick(View view) {
                    if(kommentar.getText().toString().replace(" ", "").length() > 0)
                    {
                        Java.addComment(BeitragActivity.this, kommentar.getText().toString(),myUsername, myUID, dateObjekt, beitrag.getUserID());
                        Java.ladeAlleKommentare(beitrag.getUserID(), dateObjekt, recyclerView, myAdapter, BeitragActivity.this);
                        kommentar.setText("");
                    }
                    else{
                        Toast.makeText(BeitragActivity.this, "Kein Text vorhanden.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            kommentar.setVisibility(View.INVISIBLE);
            kommentarHinzufügen.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
        folgeIch();

        followButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Die Methode wird aufgerufen, um einer Person zu folgen. Sie wird aufgerufen, wenn auf den entsprechenden Knopf
             * gedrückt wird.
             */
            @Override
            public void onClick(View view) {
                Java.followUser(userID, username.getText().toString(),BeitragActivity.this);
                followButton.setVisibility(View.INVISIBLE);
            }
        });

        unfollowButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Methode, welche auslöst, wenn ein Nutzer einem anderen Nutzer entfolgen möchte.
             */
            @Override
            public void onClick(View view) {
                Java.followUser(userID, username.getText().toString(),BeitragActivity.this);
                unfollowButton.setVisibility(View.INVISIBLE);
            }
        });

        deletePost.setOnClickListener(new View.OnClickListener() {
            /**
             * Wenn der Nutzer auf "Beitrag löschen" klickt wird diese Methode aufgerufen. Sie sorgt dafür, dass der Beitrag
             * für alle Nutzer ausgeblendet wird. Außerdem startet sie die Main Activity, da der Beitrag, welcher gerade betrachtet wird
             * nicht mehr existiert.
             */
            @Override
            public void onClick(View view) {
                Java.deletePost(userID, dateObjekt, BeitragActivity.this);
                Intent intent = new Intent(BeitragActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Diese Methode wird aufgerufen, wenn auf ein Item aus dem RecyclerView geklickt wurde.
     * Die Methode startet die Aktivität "BeitragActivity", also die gleiche, welche zum Aufruf der Methode läuft,
     * um den Nutzer weitere Informationen zu einem Kommentar anzuzeigen, auf welchen dieser geklickt hat.
     * @param position Position des Beitrags, auf welchen geklickt wurde.
     */
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

    /**
     * Testet, ob man der Person dessen Beitrag man gerade ansieht folgt oder nicht. Je nachdem was der Fall ist wird ein anderer Knopf
     * eingeblendet. Handelt es sich bei dem Post um den eigenen, so wird ein Knopf angezeigt, um diesen zu löschen.
     */
    private void folgeIch(){
        if(myUID.equals(userID))
        {
            deletePost.setVisibility(View.VISIBLE);
            followButton.setVisibility(View.INVISIBLE);
            unfollowButton.setVisibility(View.INVISIBLE);
        }
        else {
            deletePost.setVisibility(View.INVISIBLE);
            Java.folgeIch(userID).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(Task<Boolean> task) {
                    if (task.isSuccessful()) {
                        boolean isFollowing = task.getResult();
                        if (isFollowing) {
                            followButton.setVisibility(View.INVISIBLE);
                            unfollowButton.setVisibility(View.VISIBLE);
                        } else {
                            followButton.setVisibility(View.VISIBLE);
                            unfollowButton.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Exception e = task.getException();
                    }
                }
            });
        }
    }
}