package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Diese Aktivität wird aufgerufen, sobald ein neuer Beitrag hinzugefügt werden soll. Sie zeigt entsprechende Textfelder an
 * und sorgt dafür, dass die richtigen Methoden aufgerufen werden, um den Beitrag in Firebase zu speichern.
 */
public class NewPostActivity extends AppCompatActivity {

    private Button post_button;
    private EditText newPost;
    private String username;
    private String uid;
    private Switch statementStorySwitch;
    private TextView textViewStory;
    private boolean statement = true;
    private TextView textViewStatement;
    private ImageView newStatement;
    private ImageView newStory;

    /**
     * Methode, welche aufgerufen wird, wenn die Aktivität gestartet wird. Sie weißt die Variablen der Aktivität zu und setzt
     * grundlegende Dinge fest.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            uid = extras.getString("uid");
        }

        post_button = findViewById(R.id.post_button);
        newPost = findViewById(R.id.newPost);
        statementStorySwitch = findViewById(R.id.statementStorySwitch);
        textViewStatement = findViewById(R.id.textViewStatement);
        textViewStory = findViewById(R.id.textViewStory);
        newStatement = findViewById(R.id.imageViewStatement);
        newStory = findViewById(R.id.imageViewStory);
        switchOff();

        post_button.setOnClickListener(new View.OnClickListener() {
            /**
             * Wenn dieser Button geklickt wird, so wird ein neuer Post hochgeladen.
             */
            @Override
            public void onClick(View view) {
                Java.addPost(NewPostActivity.this, newPost.getText().toString(), username, uid, statement);
                Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /**
         * Wird der Schalter, bei dem man zwischen Statement und Story switchen kann umgelegt, so wird diese Methode aufgerufen
         * und aktualisiert ein paar Dinge.
         */
        statementStorySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchOn();
                } else {
                    switchOff();
                }
            }
        });
    }

    /**
     * Der Schalter ist aus. Es handelt sich bei dem neuen Beitrag um ein Statement. (Ein besonders kurzer Text.)
     */
    private void switchOff() {
        textViewStory.setTypeface(null, Typeface.NORMAL);
        textViewStatement.setTypeface(null, Typeface.BOLD);
        int neueMaximaleLaenge = 180;
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(neueMaximaleLaenge);
        newPost.setFilters(filterArray);
        newPost.setText(newPost.getText().toString().substring(0, Math.min(newPost.getText().toString().length(), 180)));
        newStatement.setVisibility(View.VISIBLE);
        newStory.setVisibility(View.INVISIBLE);
        statement = true;
    }

    /**
     * Der Schalter ist an, es handlet sich bei dem neuen Beitrag um eine Story. (Kaum eine Zeichenbegrenzung beim neuen Post.)
     */
    private void switchOn() {
        textViewStory.setTypeface(null, Typeface.BOLD);
        textViewStatement.setTypeface(null, Typeface.NORMAL);
        int neueMaximaleLaenge = 5000;
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(neueMaximaleLaenge);
        newPost.setFilters(filterArray);
        newStatement.setVisibility(View.INVISIBLE);
        newStory.setVisibility(View.VISIBLE);
        statement = false;
    }

    /**
     * Methode, welche aufgerufen wird, wenn der zurück Knopf gedrpckt wird.
     * Die Methode startet wieder die MainActivity.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}