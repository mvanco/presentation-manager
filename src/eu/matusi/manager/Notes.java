package eu.matusi.manager;

import eu.matusi.manager.database.Data;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
//import eu.matusi.manager.database.DatabaseHelper;
//import android.database.sqlite.SQLiteDatabase;

public class Notes extends Activity {
	

	int presentation;
	int action;
	EditText notes;
	
	public static final String PARAM_PRESENTATION = "presentation";
	public static final String PARAM_ACTION = "action";
	

	private SQLiteDatabase db = null;
	
	ContentValues values = new ContentValues(7);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = (new Data(this)).getWritableDatabase();

		setContentView(R.layout.notes);
		
		presentation = getIntent().getExtras().getInt(PARAM_PRESENTATION);
		
		notes = (EditText) findViewById(R.id.n_notes);
		
		
		String[] columns = { Data.P_NOTES };
		String[] parms = { String.valueOf(presentation) };
		Cursor result = db.query(Data.TABLE_BLOCKS, columns,
				Data.B_ID + "=?", parms, null, null, null);

		if (result.getCount() > 0) {
			result.moveToFirst();
			
			notes.setText(Data.getString(result, Data.P_NOTES));			
		}
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onSubmit(View v) {
		
		/*ContentValues values = new ContentValues(7);
		
		db.insert("presentations", "name", values);*/
		
		
		String[] params = { String.valueOf(presentation) };
		values.put(Data.P_NOTES, notes.getText().toString());
		db.update(Data.TABLE_BLOCKS, values, Data.B_ID + "=?", params);

		setResult(RESULT_OK);
		finish();
	}

	public void onCancel(View v) {

		setResult(RESULT_CANCELED);
		finish();
	}

}