package eu.matusi.manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.matusi.manager.database.Data;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Export extends Activity {

	private SQLiteDatabase db = null;
	private static final String TAG = null;
	String action;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export);
		
		db = (new Data(this)).getWritableDatabase();
		action = getIntent().getExtras().getString("action");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onSubmit(View v) {
		EditText et = (EditText) findViewById(R.id.e_name_edit);
		try {
		    File root = Environment.getExternalStorageDirectory();
		    if (root.canWrite() && createDirIfNotExists("Presentation Manager")) {
		    	
		        File gpxfile = new File(root, "Presentation Manager/" + et.getText().toString() + ".csv");
		        FileWriter gpxwriter = new FileWriter(gpxfile);
		        BufferedWriter out = new BufferedWriter(gpxwriter);
		        out.write(export());
		        out.close();
		    }
		    else {
				setResult(RESULT_CANCELED);
				finish();
		    }
		} catch (IOException e) {
		    Log.e(TAG, "Could not write file " + e.getMessage());
		}
		setResult(RESULT_OK);
		finish();
	}

	private String export() {
		// TODO Auto-generated method stub
		String cols = "_id , name, action, authors, datetime, duration, notes";
		Cursor actionsCursor = db.rawQuery("SELECT " + cols
				+ " FROM presentations  WHERE action=\"" + action
				+ "\" ORDER BY sequence_id", null);
		actionsCursor.moveToFirst();
		StringBuilder sb = new StringBuilder();
		while (!actionsCursor.isAfterLast()) {
			if (!actionsCursor.getString(1).contentEquals("<Prestávka>")) {
				sb.append(actionsCursor.getString(1));
				sb.append(",");
				sb.append(actionsCursor.getString(3));
				sb.append(",");
				sb.append(actionsCursor.getString(6));
				sb.append("\n");
			}
			actionsCursor.moveToNext();
		}
		return sb.toString();
	}
	
	public boolean createDirIfNotExists(String path) {
	    boolean ret = true;

	    File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            Log.e("TravellerLog :: ", "Problem creating Image folder");
	            ret = false;
	        }
	    }
	    return ret;
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

}
