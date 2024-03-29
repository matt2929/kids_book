package com.google.cloud.android.speech.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.cloud.android.speech.R;

public class FrontPage extends Activity {
    Button start, author, history, confirmName;
    EditText editText;
    CheckBox checkBox;
    public static boolean EYETRACK = true;
    public static String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_front_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.CAMERA, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO },555);

        start = (Button) findViewById(R.id.titlereadtome);
        author = (Button) findViewById(R.id.author);
        history = (Button) findViewById(R.id.ADMIN);
        confirmName = (Button) findViewById(R.id.savename);
        editText = (EditText) findViewById(R.id.editText);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        mainButtons();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterNameView();

            }
        });
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Authors.class);
                startActivity(i);

            }
        });

        confirmName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editText.getText().toString();
                Log.e("NAME", name);


                Intent i;
                    i = new Intent(getApplicationContext(), PageTurner.class);

                startActivity(i);

            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EYETRACK = b;
            }
        });
    }

    public void mainButtons() {
        start.setVisibility(View.VISIBLE);
        author.setVisibility(View.VISIBLE);
        history.setVisibility(View.VISIBLE);
        confirmName.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

    }

    public void enterNameView() {
        start.setVisibility(View.GONE);
        author.setVisibility(View.GONE);
        history.setVisibility(View.GONE);
        confirmName.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
    }
}