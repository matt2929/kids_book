package com.google.cloud.android.speech.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.cloud.android.speech.R;
import com.google.cloud.android.speech.customview.CustomAdapter;
import com.google.cloud.android.speech.customview.DataModel;

import java.util.Arrays;
import java.util.Comparator;

public class AnalysisSpeechReport extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_speech_report);
		TextView title = (TextView) findViewById(R.id.textView4);
		ListView listView = (ListView) findViewById(R.id.listOfDifficultWords);
		ImageButton imageLeft = (ImageButton) findViewById(R.id.imageButton2);
		TextView pageTitle = (TextView) findViewById(R.id.textView5);
		ImageButton right = (ImageButton) findViewById(R.id.imageButton);

		listView.setAdapter(new CustomAdapter(PageTurner.words_to_duration,getApplicationContext()));
		title.setText("Problem Words");
	}
}
