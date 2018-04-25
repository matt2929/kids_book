/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.android.speech.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.cloud.android.speech.googlecloud.MessageDialogFragment;
import com.google.cloud.android.speech.R;
import com.google.cloud.android.speech.googlecloud.SpeechService;
import com.google.cloud.android.speech.googlecloud.VoiceRecorder;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener {

	private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

	private static final String STATE_RESULTS = "results";

	private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

	private SpeechService mSpeechService;

	private VoiceRecorder mVoiceRecorder;
	private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

		@Override
		public void onVoiceStart() {
			showStatus(true);
			if (mSpeechService != null) {
				mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
			}
		}

		@Override
		public void onVoice(byte[] data, int size) {
			if (mSpeechService != null) {
				mSpeechService.recognize(data, size);
			}
		}

		@Override
		public void onVoiceEnd() {
			showStatus(false);
			if (mSpeechService != null) {
				mSpeechService.finishRecognizing();
			}
		}

	};

	// Resource caches
	private int mColorHearing;
	private int mColorNotHearing;
	TextView textView;

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder binder) {
			mSpeechService = SpeechService.from(binder);
			mSpeechService.addListener(mSpeechServiceListener);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mSpeechService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 textView= (TextView) findViewById(R.id.textView);

		final Resources resources = getResources();
		final Resources.Theme theme = getTheme();
		mColorHearing = ResourcesCompat.getColor(resources, R.color.status_hearing, theme);
		mColorNotHearing = ResourcesCompat.getColor(resources, R.color.status_not_hearing, theme);

		final ArrayList<String> results = savedInstanceState == null ? null :
				savedInstanceState.getStringArrayList(STATE_RESULTS);
		Intent intent = new Intent(getApplicationContext(),FrontPage.class);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Prepare Cloud Speech API
		bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

		// Start listening to voices
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
				== PackageManager.PERMISSION_GRANTED) {
			startVoiceRecorder();
		} else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.RECORD_AUDIO)) {
			showPermissionMessageDialog();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
					REQUEST_RECORD_AUDIO_PERMISSION);
		}
	}

	@Override
	protected void onStop() {
		// Stop listening to voice
		stopVoiceRecorder();
		if(mSpeechService!=null) {
			// Stop Cloud Speech API
			mSpeechService.removeListener(mSpeechServiceListener);
			unbindService(mServiceConnection);
			mSpeechService = null;
		}
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
			if (permissions.length == 1 && grantResults.length == 1
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startVoiceRecorder();
			} else {
				showPermissionMessageDialog();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_file:
				mSpeechService.recognizeInputStream(getResources().openRawResource(R.raw.audio));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void startVoiceRecorder() {
		if (mVoiceRecorder != null) {
			mVoiceRecorder.stop();
		}
		mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
		mVoiceRecorder.start();
	}

	private void stopVoiceRecorder() {
		if (mVoiceRecorder != null) {
			mVoiceRecorder.stop();
			mVoiceRecorder = null;
		}
	}

	private void showPermissionMessageDialog() {
		MessageDialogFragment
				.newInstance(getString(R.string.permission_message))
				.show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
	}

	private void showStatus(final boolean hearingVoice) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
			}
		});
	}

	@Override
	public void onMessageDialogDismissed() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
				REQUEST_RECORD_AUDIO_PERMISSION);
	}

	private final SpeechService.Listener mSpeechServiceListener =
			new SpeechService.Listener() {
				@Override
				public void onSpeechRecognized(final String text, final boolean isFinal) {
					Log.e("Text", text);
					if(text.contains("start")){
						Intent intent = new Intent(getApplicationContext(),FrontPage.class);
						startActivity(intent);
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							textView.setText(text);
						}
					});
					if (isFinal) {
						mVoiceRecorder.dismiss();
					}
				}
			};

	private static class ViewHolder extends RecyclerView.ViewHolder {

		TextView text;

		ViewHolder(LayoutInflater inflater, ViewGroup parent) {
			super(inflater.inflate(R.layout.item_result, parent, false));
			text = (TextView) itemView.findViewById(R.id.text);
		}

	}




}
