package eu.matusi.manager;

import eu.matusi.manager.database.Data;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;




public class PointsDialog extends Activity
{
   private SQLiteDatabase db;
   private int presentation = -1;
   private static String PARAM_PRESENTATION = "presentation";
   private EditText pointsEditText;
  
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.points_dialog);

      db = (new Data(this)).getWritableDatabase();

      presentation = getIntent().getExtras().getInt(PARAM_PRESENTATION);
      
      pointsEditText = (EditText) findViewById(R.id.po_points_edit);
      if (getPoints() != -1)
         pointsEditText.setText(String.valueOf(getPoints()));
   }
   
   private int getPoints() {
      Cursor cursor = Data.getBlockCursor(db, presentation);
      if ( cursor.getCount() > 0 && !Data.isNull(cursor, Data.P_POINTS) )
         return Data.getInt(cursor, Data.P_POINTS);

      else
         return -1;
   }

 
   @Override
   public void onDestroy()
   {
      super.onDestroy();

      db.close();
   }
  
   public void onSubmit(View v)
   {
      try {
         ContentValues cv = new ContentValues();
         String pointsStr = pointsEditText.getText().toString();
         int points = Integer.parseInt(pointsStr);
         cv.put(Data.P_POINTS, points);
         db.update(Data.TABLE_BLOCKS, cv, Data.B_ID + "=?", new String[] {String.valueOf(presentation)});
      }
      catch (NumberFormatException ex) {}
      setResult(RESULT_OK);
      finish();
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

}