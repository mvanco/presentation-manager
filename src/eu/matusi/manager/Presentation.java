package eu.matusi.manager;

import java.util.Calendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;

public class Presentation extends Activity implements OnChronometerTickListener {

	private final static int NOTES = 0;
	private final static int EDIT_PRESENTATION = 1;
	private final static int POINTS = 2;
	private static final int NOTIFY_ME_ID = 1337;

	int presentation;
	int action;
	private boolean play = false;
	Chronometer mChronometer;
	ProgressBar bar;
	double border;
	boolean through = false;
	int durationInt;

	boolean soundEnabled = false;
	boolean vibrationEnabled = false;
	boolean pointsEnabled = false;
	
	NotificationManager mgr = null;

	private SQLiteDatabase db = null;
	
	public static final String PARAM_PRESENTATION = "presentation";
	public static final String PARAM_ACTION = "action";
	
	LinearLayout pointLayout;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		


		db = (new Data(this)).getWritableDatabase();
		
		mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		setContentView(R.layout.presentation);

		presentation = getIntent().getExtras().getInt(PARAM_PRESENTATION);
		action = getIntent().getExtras().getInt(PARAM_ACTION);
		
		String[] columns3 = { Data.A_DUR_BLOCKS };
		String[] params3 = { String.valueOf(action) };
		Cursor result3 = db.query(Data.TABLE_ACTIONS, columns3, Data.A_ID + "=?", params3, null,
				null, null);
		
		result3.moveToFirst();
		if (result3.getCount() > 0) {
			durationInt = Data.getInt(result3, Data.A_DUR_BLOCKS);
		}

		TextView nameView = (TextView) findViewById(R.id.cp_name);
		TextView authorsView = (TextView) findViewById(R.id.cp_authors);
		TextView datetimeView = (TextView) findViewById(R.id.cp_datetime);
		TextView endtimeView = (TextView) findViewById(R.id.cp_endtime);

		String[] columns = { Data.B_ID, Data.P_NAME, Data.B_ACTION, Data.P_AUTHORS, Data.B_BEGIN, Data.B_DUR_BLOCKS, Data.P_NOTES, Data.P_DUR_PRES };
		String[] parms = { String.valueOf(action), String.valueOf(presentation) };
		Cursor result = db.query(Data.TABLE_BLOCKS, columns,
				Data.B_ACTION + "=? AND " + Data.B_ID + "=?", parms, null, null, null);

		int duration = 0;

		if (result.getCount() > 0) {
			result.moveToFirst();

			nameView.setText(Data.getString(result, Data.P_NAME));
			authorsView.setText(Data.getString(result, Data.P_AUTHORS));
			datetimeView.setText(Data.getString(result, Data.B_BEGIN));
			
			int durationPres;
         if (Data.isNull(result, Data.B_DUR_BLOCKS))
            durationPres = 5;
         else
            durationPres = Data.getInt(result, Data.B_DUR_BLOCKS);
			
			Calendar end = Help.stringToCal(Data.getString(result, Data.B_BEGIN));
         end.setTimeInMillis(end.getTimeInMillis() + durationPres * 60000);
			
			endtimeView.setText(Help.calToString(end));
			duration = Data.getInt(result, Data.P_DUR_PRES);
		}

		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mChronometer.setOnChronometerTickListener(this);
		

		bar = (ProgressBar) findViewById(R.id.progress);
		bar.setMax(duration * 60);

		String[] columns2 = { Data.A_WARNING };
		String[] parms2 = { String.valueOf(action) };
		Cursor result2 = db.query(Data.TABLE_ACTIONS, columns2, Data.A_ID + "=?", parms2, null,
				null, null);
		result2.moveToFirst();
		Double warning = (Data.getInt(result2, Data.A_WARNING) / 100.0);

		border = ( (duration * 60) - ((duration * 60) / 2 * warning) );
		
		String durationStr = String.valueOf(duration);

		if (durationStr.length() == 1) {
		   durationStr = "0" + durationStr;
		}
		String newTime = new String(durationStr + ":00");

		mChronometer.setFormat("%s/" + newTime);
		mChronometer.setText("00:00/" + newTime);
		
		pointLayout = (LinearLayout) findViewById(R.id.cp_points);

	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		soundEnabled = prefs.getBoolean("sound", false);
		vibrationEnabled = prefs.getBoolean("vibration", false);
		pointsEnabled = prefs.getBoolean("points", false);
		
		
      
      if (pointsEnabled) {
    	  Log.d("MyErrors", "YourOutput");
 	     pointLayout.removeAllViews();
    	  View current = getLayoutInflater().inflate(R.layout.cp_points, null, false);
    	  pointLayout.addView(current);
      }
		
		mgr.cancel(NOTIFY_ME_ID);
	}
	
	public void onPointsClick(View v) {
	   Intent intent = new Intent(this, PointsDialog.class);
      intent.putExtra("presentation", presentation);
      startActivityForResult(intent, POINTS);
	}

	public void onChronometerTick(Chronometer chronometer) {
		bar.incrementProgressBy(1);

		if (!through && bar.getProgress() > border) {

			Notification note = new Notification(R.drawable.red_ball,
					"Status message!", System.currentTimeMillis());
			PendingIntent i = PendingIntent.getActivity(this, 0, null, 0);
			note.setLatestEventInfo(this, "Upozornenie na koniec prezentácie",
					"Prezentácia sa blíži ku koncu...", i);
			
			if (soundEnabled)
				note.defaults |= Notification.DEFAULT_SOUND;
			
			mgr.notify(NOTIFY_ME_ID, note);

			if (vibrationEnabled) {
				int dot = 200; // Length of a Morse Code "dot" in milliseconds
				int dash = 500; // Length of a Morse Code "dash" in milliseconds
				int short_gap = 200; // Length of Gap Between dots/dashes
				int medium_gap = 500; // Length of Gap Between Letters
				int long_gap = 1000; // Length of Gap Between Words
				long[] pattern = {
					0, // Start immediately
					dash, short_gap, dot, medium_gap, dash, short_gap, dot,
					medium_gap, dash, short_gap, dot, long_gap
				};
				((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(pattern, -1);
			}
			
			through = true;
			
		} else if (bar.getProgress() == bar.getMax()) {
			; // TODO: niečo spraviť ked vyprší celý limit
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onPlayPause(View v) {
		ImageView image = (ImageView) v;
		if (play) {
			image.setImageResource(R.drawable.pause);
			mChronometer.stop();

		} else {
			image.setImageResource(R.drawable.play);

			long stoppedMilliseconds = 0;
			String chronoText = mChronometer.getText().toString();

			String array[] = chronoText.substring(0, 5).split(":");
			if (array.length == 2) {
				stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
						+ Integer.parseInt(array[1]) * 1000;
			} else if (array.length == 3) {
				stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60
						* 1000 + Integer.parseInt(array[1]) * 60 * 1000
						+ Integer.parseInt(array[2]) * 1000;
			}

			mChronometer.setBase(SystemClock.elapsedRealtime()
					- stoppedMilliseconds);
			mChronometer.start();

		}
		through = false;
		play = !play;
	}

	public void onNotesClick(View v) {
		Intent intent = new Intent(this, Notes.class);
		intent.putExtra("presentation", presentation);
		intent.putExtra("action", action);
		startActivityForResult(intent, NOTES);
	}

	public void onEditPresentationClick(View v) {
		Intent intent = new Intent(this, PresentationDialog.class);
		intent.putExtra(PresentationDialog.PARAM_EDIT_MODE, true);
		intent.putExtra("presentation", presentation);
		intent.putExtra("action", action);
		startActivityForResult(intent, EDIT_PRESENTATION);
	}
}