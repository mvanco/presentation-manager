package eu.matusi.manager;

import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
//import eu.matusi.manager.database.DatabaseHelper;
//import android.database.sqlite.SQLiteDatabase;

public class EditActionNotes extends Activity {
	

	String presentation;
	EditText notes;
	

	private SQLiteDatabase db = null;
	int action;
	
	public static final String PARAM_ACTION = "action";
	
	ContentValues values = new ContentValues(7);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = (new Data(this)).getWritableDatabase();

		
		setContentView(R.layout.action_notes);
		
		notes = (EditText) findViewById(R.id.an_notes);
		
		action = getIntent().getExtras().getInt(PARAM_ACTION);
		String[] columns = { Data.A_NOTES };
		String[] parms = { String.valueOf(action) };
		Cursor result = db.query(Data.TABLE_ACTIONS, columns, Data.A_ID + "=?", parms, null, null, null);
		
		result.moveToFirst();
		notes.setText(Data.getString(result, Data.A_NOTES));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onSubmit(View v) {
		values.put(Data.A_NOTES, notes.getText().toString());
		String[] params = { String.valueOf(action) };
		db.update(Data.TABLE_ACTIONS, values, Data.A_ID + "=?", params);
		setResult(RESULT_OK);
		finish();
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

}