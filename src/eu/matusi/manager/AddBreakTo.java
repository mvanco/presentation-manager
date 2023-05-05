package eu.matusi.manager;

import java.util.Calendar;
import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddBreakTo extends Activity
{
   private int                action;
   private int                position;
   private SQLiteDatabase     db;
   private String             begin;

   private Calendar           cal                = Calendar.getInstance();
   private Button             dateButton;
   private Button             timeButton;

   public static final String PARAM_ACTION = "action";
   public static final String PARAM_POSITION = "position";

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.add_break_to);
      getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

      db = (new Data(this)).getWritableDatabase();

      action = getIntent().getExtras().getInt(PARAM_ACTION);
      position = getIntent().getExtras().getInt(PARAM_POSITION);
      
      getBreakParameters();

      cal = Help.stringToCal(begin);
      dateButton = (Button) findViewById(R.id.adt_begin_date);
      dateButton.setText(Help.getDate(cal));
      timeButton = (Button) findViewById(R.id.adt_begin_time);
      timeButton.setText(Help.getTime(cal));
   }

   private void getBreakParameters()
   {  
      String cols = Data.B_BEGIN;

      Cursor cursor = db.rawQuery("SELECT " + cols + " FROM " + Data.TABLE_BLOCKS + " WHERE " + Data.B_ACTION + "=\"" + action + "\" AND " + Data.B_POSITION + "=\"" + position + "\"", null);
      cursor.moveToFirst();
      
      begin = Data.getString(cursor, Data.B_BEGIN);
   }

   private boolean addPause(int position, int minutes)
   {
      if (minutes > 0) {
         Action.changePositions(db, action, position, true);
   
         ContentValues values = new ContentValues();
   
         values.put(Data.P_NAME, "Prestávka");
         values.put(Data.B_ACTION, action);
         values.put(Data.B_DUR_BLOCKS, minutes);
         values.put(Data.B_POSITION, position);
         values.put(Data.B_TYPE, Data.TYPE_BREAK);
   
         db.insert(Data.TABLE_BLOCKS, Data.P_NAME, values);
         return true;
      }
      else
         return false;
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
                                              cal.set(Calendar.YEAR, year);
                                              cal.set(Calendar.MONTH, monthOfYear);
                                              cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                              dateButton.setText(Help.getDate(cal));
                                              timeButton.setText(Help.getTime(cal));
                                           }
                                        };

   TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                                           public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                                           {
                                              cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                              cal.set(Calendar.MINUTE, minute);
                                              dateButton.setText(Help.getDate(cal));
                                              timeButton.setText(Help.getTime(cal));

                                           }
                                        };

   public void onDateClick(View v)
   {
      new DatePickerDialog(AddBreakTo.this, d, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
   }

   public void onTimeClick(View v)
   {
      new TimePickerDialog(AddBreakTo.this, t, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
   }

   public void onSubmit(View v)
   {
      Calendar start = Help.stringToCal(begin);
      long startmilis = start.getTimeInMillis();
      long endmilis = cal.getTimeInMillis();
      
      if ( !addPause(position, (int) ((endmilis - startmilis) / 60000)) )
         Toast.makeText(this, "Začiatok prestávky je nastavený na skôr ako koniec!", Toast.LENGTH_SHORT).show();

      setResult(RESULT_OK);
      finish();
   }

   public void onCancel(View v)
   {
      setResult(RESULT_CANCELED);
      finish();
   }

}