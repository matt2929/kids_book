package com.google.cloud.android.speech.Activities;

public class WordToDuration {
	String wordIncorrect = "";
	Long duration = -1L;

	public WordToDuration(String wordIncorrect, Long duration){
		this.wordIncorrect = wordIncorrect;
		this.duration = duration;
	}
}
