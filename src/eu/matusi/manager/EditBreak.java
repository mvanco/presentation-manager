package eu.matusi.manager;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import eu.matusi.manager.help.Help;

import eu.matusi.manager.database.Data;

public class EditBreak extends Activity
{

   private SQLiteDatabase     db                 = null;
   String                     presentation;
   int                        action;
   String                     datetime;
   EditText                   editText;
   int                        duration;
   int                        block;

   public static final String PARAM_ACTION       = "action";
   public static final String PARAM_PRESENTATION = "presentaton";

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      db = (new Data(this)).getWritableDatabase();
      setContentView(R.layout.edit_break);

      editText = (EditText) findViewById(R.id.eb_edit);

      action = getIntent().getExtras().getInt(PARAM_ACTION);
      block = getIntent().getExtras().getInt(PARAM_PRESENTATION);
      String[] columns = { Data.B_DUR_BLOCKS, Data.P_NAME, Data.B_POSITION };
      String[] params = { String.valueOf(block), String.valueOf(action) };
      Cursor result = db.query(Data.TABLE_BLOCKS, columns, Data.B_ID + "=? AND " + Data.B_ACTION + "=?", params, null, null, null);

      if (result.getCount() > 0) {
         result.moveToFirst();
         duration = Data.getInt(result, Data.B_DUR_BLOCKS);
      }
      editText.setText(String.valueOf(duration));

   }

   @Override
   public void onDestroy()
   {
      super.onDestroy();
      db.close();
   }

   public void onSubmit(View v)
   {
      ContentValues values = new ContentValues();

      int newDuration = Integer.parseInt(editText.getText().toString());
      values.put(Data.B_DUR_BLOCKS, newDuration);

      String[] params = { String.valueOf(block), String.valueOf(action) };
      db.update(Data.TABLE_BLOCKS, values, Data.B_ID + "=? AND " + Data.B_ACTION + "=?", params);

      setResult(RESULT_OK);
      finish();

   }

   public void onCancel(View v)
   {
      setResult(RESULT_CANCELED);
      finish();
   }

}
