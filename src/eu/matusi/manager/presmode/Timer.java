package eu.matusi.manager.presmode;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import eu.matusi.manager.R;
import eu.matusi.manager.database.Data;



public class Timer extends Activity implements OnChronometerTickListener
{
   Data db;
   
   public static String PARAM_ID = "identificator";
   
   private static final int GOTO_SLIDE = 0;
   
   private int record;
   
   private int slide; //aktualny
   private int step; //aktualny priebeh v prezentacii
   private int slideCount; //celkovy pocet
   private int stepCount; //celkovy pocet
   private int nextSlideStep;
   
   TextView slideText;
   ProgressBar slideProgress;
   EditText notesEdit;
   ProgressBar presProgress;
   Chronometer chrono;
   ImageView image;
   
   
   
   private boolean play = false;
  
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.pres_timer);
      
      db = new Data(this);
     
      record = getIntent().getExtras().getInt(PARAM_ID);
      
      slideText = (TextView) findViewById(R.id.slide);
      slideProgress = (ProgressBar) findViewById(R.id.slide_progress);
      notesEdit = (EditText) findViewById(R.id.notes);
      presProgress = (ProgressBar) findViewById(R.id.pres_progress);
      chrono = (Chronometer) findViewById(R.id.chronometer);
      chrono.setOnChronometerTickListener(this);
      
      image = (ImageView) findViewById(R.id.play_button);
   
      initializeCounting();
      
      ImageView playButton = (ImageView) findViewById(R.id.play_button);
      final Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
      playButton.setOnLongClickListener(new OnLongClickListener() {

          public boolean onLongClick(View v) {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Timer.this);
             if (prefs.getBoolean("vibration", false))
                vibrator.vibrate(200);
             image.setImageResource(R.drawable.pause);
             chrono.stop();
             initializeCounting();
             play = false;
              return true;
          }
      });
   }
   
   @Override
   public void onPause() {
      db.insertNote(record, slide, notesEdit.getText().toString());
      super.onPause();
   }
   
   private void initializeCounting() {
      String newTime = new String(db.getDurRecord(record) + ":00");
      
      chrono.setFormat("%s/" + newTime);
      chrono.setText("00:00/" + newTime);
      
      slide = 1;
      step = 0;
      slideCount = db.getSlidesRecord(record);
      stepCount = db.getDurRecord(record) * 60;
      
      slideText.setText("1");
      slideProgress.setMax(stepCount / slideCount); //tieto 2 hodnoty nastavujeme, pri zmene slidu
      slideProgress.setProgress(0);
      presProgress.setMax(stepCount); //toto sa uz nemeni
      presProgress.setProgress(0);
      
      notesEdit.setText(db.getNote(record, slide));
   }
   
   public void onChronometerTick(Chronometer chronometer) {
      step++;
      if (step >= nextSlideStep())
         setSlide(slide + 1);

      else
         slideProgress.incrementProgressBy(1);
      
      presProgress.incrementProgressBy(1);
   }
   
   private int nextSlideStep() { //vrati najblizsi krok
      return nextSlideStep; //funkcia uz nema zmysel ale nachadza sa tu kvoli zachovaniu rozhrania s technickou spravou
   }
   
   /*private void testPrint() {
      //Log.d("e", "slide=" + slide);
      //Log.d("e", "step=" + step);
      Log.d("e", "prepinam na slide " + slide);
      //Log.d("e", "nextSlideStep=" + nextSlideStep());
      Log.d("e", "slideProgress=" + slideProgress.getMax());
      Log.d("e", "step=" + step);
      Log.d("e", "nextSlideStep=" + nextSlideStep());
      Log.d("e", "za krokov=" + (nextSlideStep() - step));
      //Log.d("e", "presProgress=" + presProgress.getMax());
      //Log.d("e", "????????????????????????????");
   }*/
   
   private void setSlide(int which) { //prepne slide, nemeni priebeh!
      
      if (which >= 1 && which <= slideCount) {
         db.insertNote(record, slide, notesEdit.getText().toString());
         slide = which;
         slideText.setText(String.valueOf(slide));
         int stepsToEnd = stepCount - step;
         int slidesToEnd = slideCount - slide + 1;
         slideProgress.setMax(stepsToEnd / slidesToEnd);
         nextSlideStep = step + (stepsToEnd / slidesToEnd);
         slideProgress.setProgress(0);
         notesEdit.setText(db.getNote(record, slide));
      }
   }
   
   public void onPreviousClick(View v) {
      setSlide(slide - 1);
      
   }
   
   public void onGivenClick(View v) {
      Intent intent = new Intent(this, SlideDialog.class);
      intent.putExtra(SlideDialog.PARAM_SLIDE, slide);
      startActivityForResult(intent, GOTO_SLIDE);
   }
   
   public void onNextClick(View v) {
      setSlide(slide + 1);
   }
   
   public void onPlayClick(View v) {
      
      if (play) {
         image.setImageResource(R.drawable.pause);
         chrono.stop();
         notesEdit.setFocusable(true);
         notesEdit.setFocusableInTouchMode(true);

      } else {
         image.setImageResource(R.drawable.play);

         long stoppedMilliseconds = 0;
         String chronoText = chrono.getText().toString();

         String array[] = chronoText.substring(0, 5).split(":");
         if (array.length == 2) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                  + Integer.parseInt(array[1]) * 1000;
         } else if (array.length == 3) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60
                  * 1000 + Integer.parseInt(array[1]) * 60 * 1000
                  + Integer.parseInt(array[2]) * 1000;
         }

         chrono.setBase(SystemClock.elapsedRealtime()
               - stoppedMilliseconds);
         chrono.start();
         notesEdit.setFocusable(false);
         notesEdit.setFocusableInTouchMode(false);
      }
      play = !play;
   }
   
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == GOTO_SLIDE && resultCode == RESULT_OK) {
         setSlide(data.getIntExtra(SlideDialog.RETURN, slide));
      }
   }

}