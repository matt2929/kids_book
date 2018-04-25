package com.google.cloud.android.speech.customview;
public class DataModel {

	String name;
	Long duration;

	public DataModel(String name, Long duration) {
		this.name=name;
		this.duration=duration;
	}

	public String getName() {
		return name;
	}

	public Long getType() {
		return duration;
	}



}