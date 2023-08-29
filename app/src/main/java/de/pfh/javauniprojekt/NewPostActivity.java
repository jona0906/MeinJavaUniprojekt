package de.pfh.javauniprojekt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

public class NewPostActivity extends AppCompatActivity {

    private Button post_button;
    private EditText newPost;
    private String username;
    private String uid;
    private Switch statementStorySwitch;
    private TextView textViewStory;
    private boolean statement = true;
    private TextView textViewStatement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            uid = extras.getString("uid");

        }
        Objects.requireNonNull(getSupportActionBar()).setTitle("Neuer Beitrag");

        post_button = findViewById(R.id.post_button);
        newPost = findViewById(R.id.newPost);
        statementStorySwitch = findViewById(R.id.statementStorySwitch);
        textViewStatement = findViewById(R.id.textViewStatement);
        textViewStory = findViewById(R.id.textViewStory);


        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Java.addPost(NewPostActivity.this, newPost.getText().toString(), username, uid, statement);
                Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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

    private void switchOff() {
        textViewStory.setTypeface(null, Typeface.NORMAL);
        textViewStatement.setTypeface(null, Typeface.BOLD);
        int neueMaximaleLaenge = 180;
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(neueMaximaleLaenge);
        newPost.setFilters(filterArray);
        newPost.setText(newPost.getText().toString().substring(0, Math.min(newPost.getText().toString().length(), 180)));
        statement = true;
    }

    private void switchOn() {
        textViewStory.setTypeface(null, Typeface.BOLD);
        textViewStatement.setTypeface(null, Typeface.NORMAL);
        int neueMaximaleLaenge = 5000;
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(neueMaximaleLaenge);
        newPost.setFilters(filterArray);
        statement = false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}