package com.google.cloud.android.speech.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.cloud.android.speech.R;
import com.google.cloud.android.speech.Util.GoodBadTouch;
import com.google.cloud.android.speech.Util.SaveData;
import com.google.cloud.android.speech.customview.DataModel;
import com.google.cloud.android.speech.customview.PleaseSwipe;
import com.google.cloud.android.speech.fragments.Page;
import com.google.cloud.android.speech.fragments.PageEight;
import com.google.cloud.android.speech.fragments.PageFive;
import com.google.cloud.android.speech.fragments.PageFour;
import com.google.cloud.android.speech.fragments.PageOne;
import com.google.cloud.android.speech.fragments.PageSeven;
import com.google.cloud.android.speech.fragments.PageSix;
import com.google.cloud.android.speech.fragments.PageThree;
import com.google.cloud.android.speech.fragments.PageTwo;
import com.google.cloud.android.speech.googlecloud.MessageDialogFragment;
import com.google.cloud.android.speech.googlecloud.SpeechService;
import com.google.cloud.android.speech.googlecloud.VoiceRecorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;


public class PageTurner extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MessageDialogFragment.Listener {
	TextView textView;
	Button nextPage, Repeat, test;
	RelativeLayout ll;
	FrameLayout fragCase;
	Clock2 clock2;
	Calendar calendar;
	Long startTimeTouchable;
	Long lastWord = System.currentTimeMillis();
	boolean goodTouch = false;
	PleaseSwipe pleaseSwipe;
	public static ArrayList<Button> allButtons;
	GoodBadTouch goodBadTouch;
	MediaPlayer mediaPlayer = new MediaPlayer();
	ArrayList<Integer> pageTextRecording = new ArrayList<>();
	ArrayList<Integer> touchDelayRecording = new ArrayList<>();
	static ArrayList<DataModel> words_to_duration = new ArrayList<>();
	Long startTime = System.currentTimeMillis();
	boolean justClick = false;
	int clickCount = 0;
	android.app.FragmentManager fragmentManager;
	android.app.FragmentTransaction transaction;
	Page _CurrentPage = new PageOne();
	//  TextToSpeech tts;
	boolean canClick = false;
	Handler handler2;
	boolean canRecord = false;
	Typeface tf;
	private float x1, x2;
	static final int MIN_DISTANCE = 375;
	ArrayList<Page> allPages = new ArrayList<>();
	int currentPageIndex = 0;
	SaveData saveData;
	private float width = 0, height = 0;
	FrameLayout preview;
	private Handler handler;
	View myView;
	OrientationEventListener orientationEventListener;
	int deviceOrientation;
	int presentOrientation;
	Display display;
	Long timer_refresh_listen = System.currentTimeMillis();
	//word detection stuff
	ArrayList<String> wordsToSay = new ArrayList<>();//words to give interpreter without symbols.
	ArrayList<String> wordsToDisplay = new ArrayList<>();//words with symbols for displaying.
	ArrayList<Integer> wordsWithIssue = new ArrayList<>();//words the user skipped.
	int[] wordCorrectnessDetection = new int[0];//word value so we know what color to draw.
	int wordReadingProgress = 0;//index of word in sentence user is trying to say.
	Long wordDuration = System.currentTimeMillis();
	//------end globals

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

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_turner);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			int RECORD_AUDIO = 666;
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.RECORD_AUDIO},
					RECORD_AUDIO);
		}

		permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			int ACCESS_NETWORK_STATE = 333;
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
					ACCESS_NETWORK_STATE);
		}

		saveData = new SaveData(getApplicationContext());
		resetPages();
		goodBadTouch = new GoodBadTouch(getApplicationContext());
		calendar = Calendar.getInstance();
		pageTextRecording.add(R.raw.page1);
		pageTextRecording.add(R.raw.page2);
		pageTextRecording.add(R.raw.page3);
		pageTextRecording.add(R.raw.page4);
		pageTextRecording.add(R.raw.page5);
		pageTextRecording.add(R.raw.page6);
		pageTextRecording.add(R.raw.page7);
		pageTextRecording.add(R.raw.page8);

		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		touchDelayRecording.add(R.raw.slurp);
		handler = new Handler();
		handler2 = new Handler();

		clock2 = new Clock2(handler2);
		Button testButt = (Button) findViewById(R.id.testsave);
		testButt.setOnClickListener(new View.OnClickListener() {
			                            @Override
			                            public void onClick(View view) {
				                            Thread thread = new Thread(new Runnable() {
					                            @Override
					                            public void run() {
						                            //		                            saveData.savePage(goodBadTouch.get_Touches(), goodBadTouch.get_ReadEyeCoordinates(), goodBadTouch.get_PostReadEyeCoordinates(), goodBadTouch.getEarly(), Math.abs(startTimeTouchable - System.currentTimeMillis()), currentPageIndex + 1);
						                            saveData.saveSession(getApplicationContext(), calendar, Calendar.getInstance());
					                            }
				                            });
				                            thread.start();
			                            }
		                            }
		);
		ll = (RelativeLayout) findViewById(R.id.activity_page_turner);
		textView = (TextView) findViewById(R.id.textonpage);
		textView.setText(_CurrentPage.getString());
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - wordDuration > 4000) {
					wordCorrectnessDetection[wordReadingProgress] = -1;

					wordDuration = System.currentTimeMillis();
					wordReadingProgress++;
					redrawTextView();
					if (wordReadingProgress == wordsToSay.size()) {
						finishSpeaking();
					}
				}
			}
		});


		setNextWordList(_CurrentPage.getString());
		String[] tempWord = textView.getText().toString().split(" ");
		pleaseSwipe = (PleaseSwipe) findViewById(R.id.swipeAnimation);
		preview = (FrameLayout) findViewById(R.id.previewviewturn);
		myView = new View(PageTurner.this);
		pleaseSwipe.setVisibility(View.GONE);
		mediaPlayer = MediaPlayer.create(this, R.raw.page1);
		mediaPlayer.setVolume(10, 10);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setVolume(10, 10);
		clock2.run();
		tf = Typeface.createFromAsset(getAssets(), "fonts/calibri.otf");
		textView.setTypeface(tf);
		mediaPlayer = MediaPlayer.create(PageTurner.this, R.raw.page1);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnPreparedListener(this);
		fragmentManager = getFragmentManager();
		_CurrentPage.passMediaPlayer(getApplicationContext());
		Repeat = (Button) findViewById(R.id.repeatspeaks);
		Repeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
				mediaPlayer.start();
			}
		});

		fragCase = (FrameLayout) findViewById(R.id.fragmentcase);

		display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		getFragmentManager().beginTransaction().replace(R.id.fragmentcase, _CurrentPage).commit();

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

		// Stop Cloud Speech API
		if (mSpeechService != null) {
			mSpeechService.removeListener(mSpeechServiceListener);
		}
		unbindService(mServiceConnection);
		mSpeechService = null;

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

	public void processSpeech(String str) {
		Log.e("speech", str);
		Log.e(str, wordsToSay.get(wordReadingProgress));
		if (str.contains(wordsToSay.get(wordReadingProgress))) {
			if (Math.abs(wordDuration - System.currentTimeMillis()) < 4000) {
				wordCorrectnessDetection[wordReadingProgress] = 1;
			} else {
				wordCorrectnessDetection[wordReadingProgress] = -1;
			}
			words_to_duration.add(new DataModel(wordsToSay.get(wordReadingProgress),Math.abs(System.currentTimeMillis()-wordDuration)));
			wordDuration = System.currentTimeMillis();
			redrawTextView();
			wordReadingProgress++;
			if (wordReadingProgress == wordsToSay.size()) {
				finishSpeaking();

			}

		}

	}

	private final SpeechService.Listener mSpeechServiceListener =
			new SpeechService.Listener() {
				@Override
				public void onSpeechRecognized(final String text, final boolean isFinal) {
					processSpeech(text);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

						}
					});
					if (isFinal) {
						mVoiceRecorder.dismiss();
					}
				}
			};


	public void redrawTextView() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView.setText("");
				for (int i = 0; i < wordsToDisplay.size(); i++) {
					Spannable word = new SpannableString(wordsToDisplay.get(i) + " ");
					if (wordCorrectnessDetection[i] == -1) {
						word.setSpan(new ForegroundColorSpan(Color.rgb(153, 0, 0)), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else if (wordReadingProgress == i) {
						word.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else if (wordCorrectnessDetection[i] == 0) {
						word.setSpan(new ForegroundColorSpan(Color.BLACK), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else if (wordCorrectnessDetection[i] == 1) {
						word.setSpan(new ForegroundColorSpan(Color.rgb(0, 153, 51)), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					textView.append(word);
				}
			}
		});
	}

	public void finishSpeaking() {
		canClick = true;
		_CurrentPage.enabledisabletouch(true);
		Log.e("touch", "enabled");
		wordReadingProgress = 0;
	}

	private void orientationListener() {
		orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				deviceOrientation = orientation;
			}
		};

		if (orientationEventListener.canDetectOrientation()) {
			orientationEventListener.enable();
		}

		presentOrientation = 90 * (deviceOrientation / 360) % 360;
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Log.e("offset", "" + textView.getOffsetForPosition(event.getRawX(), event.getRawY()));


		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			if (!canClick) {
				goodTouch = false;
			}
		}

		startTime = System.currentTimeMillis();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				x1 = event.getX();
				break;
			case MotionEvent.ACTION_UP:
				x2 = event.getX();
				float deltaX = x2 - x1;
				fragCase.setBackground(null);
				if (deltaX < -MIN_DISTANCE) {
					pageTurnFowards();
				}
				break;
		}
		boolean ret = super.dispatchTouchEvent(event);
		return ret;

	}

	public void resetPages() {
		allPages.clear();
		allPages.add(new PageOne());
		allPages.add(new PageTwo());
		allPages.add(new PageThree());
		allPages.add(new PageFour());
		allPages.add(new PageFive());
		allPages.add(new PageSix());
		allPages.add(new PageSeven());
		allPages.add(new PageEight());
	}


	@Override
	public void onCompletion(MediaPlayer mp) {
		canClick = true;
		//_CurrentPage.enabledisabletouch(true);
		startTimeTouchable = System.currentTimeMillis();
	}


	@Override
	public void onPrepared(MediaPlayer mp) {
		if (mediaPlayer.isPlaying()) {

		} else {
			//mediaPlayer.start();

		}
	}

	public void pageTurnFowards() {

		resetPages();
		transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(R.animator.fadein, R.animator.fadeout);
		goodBadTouch.lastTouchWasAGoodSwipe();
		//	saveData.savePage(goodBadTouch.get_Touches(), goodBadTouch.get_ReadEyeCoordinates(), goodBadTouch.get_PostReadEyeCoordinates(), goodBadTouch.getEarly(), Math.abs(startTimeTouchable - System.currentTimeMillis()), currentPageIndex + 1);
		goodBadTouch.reset(currentPageIndex);

		if (currentPageIndex == allPages.size() - 1) {
			new Thread(new Runnable() {
				public void run() {
					saveData.saveSession(getApplicationContext(), calendar, Calendar.getInstance());
				}
			}).start();

			Intent i = new Intent(getApplicationContext(), AnalysisSpeechReport.class);
			startActivity(i);
		} else {

			_CurrentPage = allPages.get(++currentPageIndex);
			if (currentPageIndex == allPages.size() - 1) {
				_CurrentPage = new PageEight();
				ll.setBackground(getResources().getDrawable(R.drawable.pastellegreenback));
				textView.setTextColor(Color.WHITE);
				textView.setShadowLayer(10, 10, 10, Color.BLACK);

			} else {
				ll.setBackground(getResources().getDrawable(R.drawable.gre2));
				//  ll.setBackground(getDrawable(R.drawable.gre2));
				textView.setTextColor(Color.BLACK);
			}
			transaction.replace(fragCase.getId(), _CurrentPage);
			textView.setText(_CurrentPage.getString());
			transaction.commit();
			clickCount++;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
			mediaPlayer = MediaPlayer.create(this, pageTextRecording.get(currentPageIndex));
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);

			//      tts.speak(_CurrentPage.getString(), TextToSpeech.QUEUE_FLUSH, map);
			_CurrentPage.passMediaPlayer(getApplicationContext());

			_CurrentPage.enabledisabletouch(true);
			canClick = false;
			setNextWordList(_CurrentPage.getString());
			redrawTextView();

		}

	}

	private void setNextWordList(String words) {
		wordsToSay.clear();
		wordsToDisplay.clear();
		wordsWithIssue.clear();
		//TODO:save stuff before wiping
		wordReadingProgress = 0;
		wordsToDisplay = new ArrayList<>(Arrays.asList(words.toString().split(" ")));
		wordCorrectnessDetection = new int[wordsToDisplay.size()];

		for (int i = 0; i < wordsToDisplay.size(); i++) {
			wordsToSay.add(wordsToDisplay.get(i).replaceAll("[^a-zA-Z]", "").toLowerCase());
			wordCorrectnessDetection[i] = 0;
		}
	}

	class Clock2 implements Runnable {
		private Handler handler;
		DisplayMetrics metrics;

		public Clock2(Handler handler) {
			this.handler = handler;
			metrics = getApplicationContext().getResources().getDisplayMetrics();
		}

		public void run() {
			width = metrics.widthPixels;
			height = metrics.heightPixels;
			if (Math.abs(timer_refresh_listen - System.currentTimeMillis()) > 10000) {
				timer_refresh_listen = System.currentTimeMillis();
			}
			if (_CurrentPage.doneTouching()) {
				if (justClick == false) {
					startTime = System.currentTimeMillis();
					justClick = true;
				} else {
				}
				if (Math.abs(startTime - System.currentTimeMillis()) > 5000) {
					pleaseSwipe.update(false);
					pleaseSwipe.setVisibility(View.VISIBLE);
				} else {
					pleaseSwipe.setVisibility(View.GONE);
				}
			} else {
				justClick = false;
				pleaseSwipe.setVisibility(View.GONE);
			}
			handler.postDelayed(this, 140);
		}
	}
}

