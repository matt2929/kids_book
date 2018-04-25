package com.google.cloud.android.speech.customview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.cloud.android.speech.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class CustomAdapter extends ArrayAdapter<DataModel> implements View.OnClickListener {
	private ArrayList<DataModel> dataSet;
	Context mContext;

	public CustomAdapter(ArrayList<DataModel> data, Context context) {
		super(context, R.layout.row_item, data);
		this.dataSet = data;
		this.mContext = context;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DataModel dataModel = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		convertView = inflater.inflate(R.layout.row_item, parent, false);
		TextView wordIncorrect = (TextView) convertView.findViewById((R.id.wordInccorect));
		TextView wordDuration = (TextView) convertView.findViewById(R.id.Duration);
		wordIncorrect.setText(dataModel.name);
		wordDuration.setText(""+((float)dataModel.duration)/1000f+"s");
		return convertView;
	}

	@Override
	public void onClick(View v) {

	}
}
