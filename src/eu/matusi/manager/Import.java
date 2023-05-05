package eu.matusi.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import eu.matusi.manager.database.Data;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Import extends Activity {

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
		    if (root.canRead() && createDirIfNotExists("Presentation Manager")) {
		    	
		        File gpxfile = new File(root, "Presentation Manager/" + et.getText().toString() + ".csv");
		        FileReader gpxwriter = new FileReader(gpxfile);
		        BufferedReader in = new BufferedReader(gpxwriter);
		        
		        String line;
		        ContentValues values = new ContentValues();
		        int sequenceID = 1;
		        while ((line = in.readLine()) != null) {
		        	String[] array = (line + " ").split(",");
					values.put("name", array[0]);
					values.put("action", action);
					values.put("authors", array[1]);
					values.put("notes", array[2]);
					values.put("sequence_id", sequenceID++);
					values.put("datetime", " ");

					db.insert("presentations", "name", values);
		        }
		        in.close();
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
