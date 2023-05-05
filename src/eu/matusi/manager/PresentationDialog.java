package eu.matusi.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class PresentationDialog extends Activity
{
   private int                presentation;
   private int                action;
   private boolean            editMode;

   private EditText           name;
   private CheckBox           checkbox;
   private EditText           authors;
   private EditText           notes;

   private SQLiteDatabase     db;

   private int                implicitDurBlocks;
   private int                implicitDurPres;
   private Integer            durationBlocks;
   private Integer            durationPres;
   private int                position;

   private Calendar           begin              = Calendar.getInstance();
   private Button             dateButton;
   private Button             timeButton;

   private EditText           durBlocksEdit;
   private EditText           durPresEdit;
   private String             beginStr;
   
   private EditText           pointsEdit;

   public static final String PARAM_EDIT_MODE    = "edit_mode";
   public static final String PARAM_ACTION       = "action";

   public static final String PARAM_PRESENTATION = "presentation";

   public static final String PARAM_POSITION     = "position";
   
   boolean pointsEnabled = false;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.presentation_dialog);

      db = (new Data(this)).getWritableDatabase();

      action = getIntent().getExtras().getInt(PARAM_ACTION);
      position = getIntent().getExtras().getInt(PARAM_POSITION);

      getActionParameters();

      name = (EditText) findViewById(R.id.ep_name_edit);
      authors = (EditText) findViewById(R.id.ep_authors_edit);
      notes = (EditText) findViewById(R.id.ep_notes_edit);
      checkbox = (CheckBox) findViewById(R.id.ep_check);

      begin = initCal();
      dateButton = (Button) findViewById(R.id.ep_begin_date);
      dateButton.setText(Help.getDate(begin));
      timeButton = (Button) findViewById(R.id.ep_begin_time);
      timeButton.setText(Help.getTime(begin));

      durBlocksEdit = (EditText) findViewById(R.id.ep_duration_edit);
      durPresEdit = (EditText) findViewById(R.id.ep_reserved_edit);
      
      
      


      OnCheckedChangeListener checkedListener = new OnCheckedChangeListener() {

         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            if (isChecked) {
               name.setText(authors.getText().toString());
               name.setFocusable(false);
               name.setClickable(false);
            }
            else {
               name.setFocusableInTouchMode(true);
               name.setFocusable(true);
               name.setClickable(true);
            }
         }

      };
      checkbox.setOnCheckedChangeListener(checkedListener);

      authors.addTextChangedListener(new TextWatcher() {

         public void afterTextChanged(Editable s)
         {
            if (checkbox.isChecked()) {
               name.setText(authors.getText().toString());
            }
         }

         public void beforeTextChanged(CharSequence s, int start, int count, int after)
         {}

         public void onTextChanged(CharSequence s, int start, int before, int count)
         {}

      });

      durBlocksEdit.setText(String.valueOf(implicitDurBlocks));
      durPresEdit.setText(String.valueOf(implicitDurPres));

      editMode = getIntent().getExtras().getBoolean(PARAM_EDIT_MODE);
      if (editMode) {
         presentation = getIntent().getExtras().getInt(PARAM_PRESENTATION);
         String[] columns = { Data.B_ID, Data.B_ACTION, Data.B_POSITION, Data.B_BEGIN, Data.B_DUR_BLOCKS, Data.B_TYPE, Data.P_NAME, Data.P_AUTHORS,
               Data.P_DUR_PRES, Data.P_NOTES };

         String[] parms = { String.valueOf(presentation), String.valueOf(action) };
         Cursor result = db.query(Data.TABLE_BLOCKS, columns, Data.B_ID + "=? AND " + Data.B_ACTION + "=? ", parms, null, null, null);

         if (result.getCount() > 0) {
            result.moveToFirst();

            if (!Data.isNull(result, Data.B_DUR_BLOCKS))
               durBlocksEdit.setText(String.valueOf(Data.getInt(result, Data.B_DUR_BLOCKS)));

            if (!Data.isNull(result, Data.P_DUR_PRES))
               durPresEdit.setText(String.valueOf(Data.getInt(result, Data.P_DUR_PRES)));

            durationPres = Data.getInt(result, Data.P_DUR_PRES);
            name.setText(Data.getString(result, Data.P_NAME));
            authors.setText(Data.getString(result, Data.P_AUTHORS));
            notes.setText(Data.getString(result, Data.P_NOTES));

            begin = Help.stringToCal(Data.getString(result, Data.B_BEGIN));
            dateButton.setText(Help.getDate(begin));
            timeButton.setText(Help.getTime(begin));

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
         }
      }
      SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(this);

	  pointsEnabled = prefs.getBoolean("points", false);
	
      LinearLayout pointLayout = (LinearLayout) findViewById(R.id.ep_point_layout);
      if (pointsEnabled) {
    	  Log.d("MyErrors", "YourOutput");
    	  LayoutInflater inflater = getLayoutInflater();
    	  View current = inflater.inflate(R.layout.points, pointLayout, false);
    	  pointLayout.addView(current);
    	  pointsEdit = (EditText) findViewById(R.id.points_edit);
    	  if (getPoints() != -1)
    	     pointsEdit.setText(String.valueOf(getPoints()));
      }
   }
   
   private int getPoints() {
      Cursor cursor = Data.getBlockCursor(db, presentation);
      if ( cursor.getCount() > 0 && !Data.isNull(cursor, Data.P_POINTS) )
         return Data.getInt(cursor, Data.P_POINTS);

      else
         return -1;
   }

   private void getActionParameters()
   {
      Cursor result = db.query(Data.TABLE_ACTIONS, new String[] { Data.A_DUR_BLOCKS, Data.A_DUR_PRES, Data.A_BEGIN }, Data.A_ID + "=?",
            new String[] { String.valueOf(action) }, null, null, null);

      result.moveToFirst();
      if (result.getCount() > 0) {
         implicitDurBlocks = Data.getInt(result, Data.A_DUR_BLOCKS);
         implicitDurPres = Data.getInt(result, Data.A_DUR_PRES);
         beginStr = Data.getString(result, Data.A_BEGIN);
      }
   }

   @Override
   public void onDestroy()
   {
      super.onDestroy();

      db.close();
   }

   DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
                                           public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                                           {
                                              begin.set(Calendar.YEAR, year);
                                              begin.set(Calendar.MONTH, monthOfYear);
                                              begin.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                              dateButton.setText(Help.getDate(begin));
                                              timeButton.setText(Help.getTime(begin));
                                           }
                                        };

   TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                                           public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                                           {
                                              begin.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                              begin.set(Calendar.MINUTE, minute);
                                              dateButton.setText(Help.getDate(begin));
                                              timeButton.setText(Help.getTime(begin));

                                           }
                                        };

   public void onDateClick(View v)
   {
      new DatePickerDialog(PresentationDialog.this, d, begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH)).show();
   }

   public void onTimeClick(View v)
   {
      new TimePickerDialog(PresentationDialog.this, t, begin.get(Calendar.HOUR_OF_DAY), begin.get(Calendar.MINUTE), true).show();
   }

   public void onSubmit(View v)
   {
      if (editMode) {
         String[] args = { String.valueOf(presentation) };
         db.delete(Data.TABLE_BLOCKS, Data.B_ID + "=?", args);
         Action.changePositions(db, action, position + 1, false);
         Action.staticOrganize(db, action, implicitDurBlocks, beginStr);
      }
      DateFormat df = DateFormat.getDateTimeInstance();
      getNewPresParams(df.format(begin.getTime()));
      if (newBreakDur > 0) {
         addPause(newPosition, newBreakDur);
         Action.changePositions(db, action, newPosition + 1, true);
      }
      else {
         Action.changePositions(db, action, newPosition, true);
      }

      ContentValues values = new ContentValues();
      
      String tmpName = name.getText().toString().trim();
      String tmpDurationBlocks = durBlocksEdit.getText().toString().trim();
      String tmpDurationPres = durPresEdit.getText().toString().trim();

      if (tmpName.equals("") || tmpDurationBlocks.equals("") || tmpDurationPres.equals("")) {
         Toast.makeText(this, "Polia označené * sú povinné!", Toast.LENGTH_LONG).show();
      }
      else {
         values.put(Data.P_NAME, name.getText().toString());
         values.put(Data.B_ACTION, action);
         values.put(Data.B_TYPE, Data.TYPE_PRESENTATION);
         values.put(Data.P_AUTHORS, authors.getText().toString());
         values.put(Data.P_NOTES, notes.getText().toString());
         values.put(Data.B_DUR_BLOCKS, Integer.parseInt(durBlocksEdit.getText().toString()));
         values.put(Data.P_DUR_PRES, Integer.parseInt((durPresEdit.getText().toString())));
         
         if (pointsEnabled) {
            if (!pointsEdit.getText().toString().equals(""))
               values.put(Data.P_POINTS, Integer.parseInt(pointsEdit.getText().toString()));
         }

         if (newBreakDur > 0) {
            values.put(Data.B_POSITION, newPosition + 1);
         }
         else {
            values.put(Data.B_POSITION, newPosition);
         }
         
         Log.d("MyErrors", "vkladam" + String.valueOf(newPosition));
         db.insert(Data.TABLE_BLOCKS, Data.P_NAME, values);

         setResult(RESULT_OK);
         finish();
      }
   }

   public void onCancel(View v)
   {
      setResult(RESULT_CANCELED);
      finish();
   }
   
	@Override
	public void onResume() {
		super.onResume();
	}

   private void addPause(int position, int minutes)
   {
      Action.changePositions(db, action, position, true);

      ContentValues values = new ContentValues();

      values.put(Data.P_NAME, "Prestávka");
      values.put(Data.B_ACTION, action);
      values.put(Data.B_DUR_BLOCKS, minutes);
      values.put(Data.B_POSITION, position);
      values.put(Data.B_TYPE, Data.TYPE_BREAK);

      db.insert(Data.TABLE_BLOCKS, Data.P_NAME, values);
   }

   private int newPosition;
   private int newBreakDur;

   // parameter is for example "1.12.2012 8:00:00"
   private boolean getNewPresParams(String time)
   {
      DateFormat df = DateFormat.getDateTimeInstance();
      Date findTime = new Date();
      try {
         findTime = df.parse(time);
      }
      catch (ParseException e) {}

      int position;
      int durationBlock;
      String begin;

      Calendar beginCal = Calendar.getInstance();
      Calendar endCal = Calendar.getInstance();

      Cursor result = Data.getBlocksCursor(db, action);
      Cursor actionCursor = Data.getActionCursor(db, action);
      if (result.getCount() == 0) {
    	  newBreakDur = 0;
      }
      else {
	      while (!result.isAfterLast()) {
	
	         position = Data.getInt(result, Data.B_POSITION);
	         begin = Data.getString(result, Data.B_BEGIN);
	
	         if (Data.isNull(result, Data.B_DUR_BLOCKS))
	            durationBlock = Data.getInt(actionCursor, Data.A_DUR_BLOCKS);
	         else
	            durationBlock = Data.getInt(result, Data.B_DUR_BLOCKS);
	
	         beginCal = Help.stringToCal(begin);
	         endCal = Help.stringToCal(begin);
	         endCal.setTimeInMillis(endCal.getTimeInMillis() + durationBlock * 60000);
	         this.newPosition = position;
	         if (findTime.equals(beginCal.getTime()) || (findTime.after(beginCal.getTime()) && findTime.before(endCal.getTime()))) {
	            this.newPosition = position;
	            long startmilis = beginCal.getTimeInMillis();
	            long endmilis = findTime.getTime();
	
	            newBreakDur = (int) ((endmilis - startmilis) / 60000);
	            return true;
	         }
	         result.moveToNext();
	      }
	      if (findTime.equals(endCal.getTime()) || findTime.after(endCal.getTime())) {
	         this.newPosition++;
	         long startmilis = endCal.getTimeInMillis();
	         long endmilis = findTime.getTime();
	         newBreakDur = (int) ((endmilis - startmilis) / 60000);
	         return true;
	      }
      }
      return false;
   }

   private Calendar initCal()
   {
      Cursor blocksCursor = Data.getBlocksCursor(db, action);
      Cursor actionCursor = Data.getActionCursor(db, action);
      Calendar cal;
      blocksCursor.moveToFirst();
      if (blocksCursor.getCount() > 0) {
         for (int i = 1; i < blocksCursor.getCount(); i++) {
            blocksCursor.moveToNext();
         }
         cal = Help.stringToCal(Data.getString(blocksCursor, Data.B_BEGIN));   
         int duration = 0;
            if (Data.isNull(blocksCursor, Data.P_DUR_PRES))
               duration = Data.getInt(actionCursor, Data.A_DUR_BLOCKS);
            else
               duration = Data.getInt(blocksCursor, Data.B_DUR_BLOCKS);
      
            cal.setTimeInMillis(cal.getTimeInMillis() + duration * 60000);
      }
      else {
         cal = Help.stringToCal(Data.getString(actionCursor, Data.A_BEGIN));
      }
      return cal;
   }

}