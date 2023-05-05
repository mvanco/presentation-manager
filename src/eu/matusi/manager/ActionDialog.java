package eu.matusi.manager;

import java.util.Date;
import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TimePicker;
import android.widget.Toast;

import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;

public class ActionDialog extends Activity {
	
	private EditText name;
	private EditText organizators;
	private EditText location;
	
	private Calendar begin = Calendar.getInstance();
	private Button dateButton;
	private Button timeButton;
	
	private EditText durationBlocks;
	private EditText durationPres;
	private EditText warningText;
	private SeekBar warningSeekBar;
	private EditText notes;
	private int progress = 50;
	
	private SQLiteDatabase db;
	
	public static final String PARAM_EDIT_MODE = "edit_mode";
	public static final String PARAM_ACTION = "action";
	
	private boolean editMode;
	private int action;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.action_dialog);
		
		db = (new Data(this)).getWritableDatabase();

		name = (EditText) findViewById(R.id.a_name_edit);
		organizators = (EditText) findViewById(R.id.a_organizators_edit);
		location = (EditText) findViewById(R.id.a_locations_edit);
		
		begin.setTime(new Date());
		dateButton = (Button) findViewById(R.id.a_begin_date);
		dateButton.setText(Help.getDate(begin));
		timeButton = (Button) findViewById(R.id.a_begin_time);
		timeButton.setText(Help.getTime(begin));
		
		durationBlocks = (EditText) findViewById(R.id.a_duration_edit);
		
		durationPres = (EditText) findViewById(R.id.a_reserved_edit);
		durationPres.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				setWarningTime(progress);
			}
		});
		
		warningText = (EditText) findViewById(R.id.a_warning_time);	
		warningSeekBar = (SeekBar) findViewById(R.id.a_warning_seekbar);
		warningSeekBar.setProgress(progress);
		warningSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setWarningTime(progress);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		notes = (EditText) findViewById(R.id.a_notes_edit);
		
		
		editMode = getIntent().getExtras().getBoolean(PARAM_EDIT_MODE);
		if (editMode) {
			action = getIntent().getExtras().getInt(PARAM_ACTION);//A_ID A_NAME A_ORGANIZATORS A_LOCATION A_BEGIN A_DUR_BLOCKS A_DUR_PRES A_WARNING A_NOTES
			Cursor cursor = Data.getActionCursor(db, action);
			name.setText(Data.getString(cursor, Data.A_NAME));
			organizators.setText(Data.getString(cursor, Data.A_ORGANIZATORS));
			location.setText(Data.getString(cursor, Data.A_LOCATION));
			begin = Help.stringToCal(Data.getString(cursor, Data.A_BEGIN));
			dateButton.setText(Help.getDate(begin));
			timeButton.setText(Help.getTime(begin));
			durationBlocks.setText(String.valueOf(Data.getInt(cursor, Data.A_DUR_BLOCKS)));	
			durationPres.setText(String.valueOf(Data.getInt(cursor, Data.A_DUR_PRES)));
			int warning = (int) Math.round(Data.getInt(cursor, Data.A_WARNING) / 60.0);
			warningText.setText(String.valueOf(warning));
			notes.setText(Data.getString(cursor, Data.A_NOTES));
		}
		if (editMode) {
		   dateButton.setOnClickListener(null);
		   dateButton.setClickable(false);
		   timeButton.setOnClickListener(null);
		   timeButton.setClickable(false);
		}
		   
	}

	public void setWarningTime(int progress) {
		this.progress = progress;
		warningText.setText("?");
		if (!durationPres.getText().toString().trim().contentEquals("")) {
			double warningDouble = Double.parseDouble(durationPres.getText().toString()) / 2;
			int warningInt = (int) Math.round(warningDouble / 100 * progress);
			warningText.setText(String.valueOf(warningInt));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.close();
	}

	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			begin.set(Calendar.YEAR, year);
			begin.set(Calendar.MONTH, monthOfYear);
			begin.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			dateButton.setText(Help.getDate(begin));
			timeButton.setText(Help.getTime(begin));
		}
	};

	TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			begin.set(Calendar.HOUR_OF_DAY, hourOfDay);
			begin.set(Calendar.MINUTE, minute);
			dateButton.setText(Help.getDate(begin));
			timeButton.setText(Help.getTime(begin));

		}
	};

	public void onDateClick(View v) {
		new DatePickerDialog(ActionDialog.this, d, begin.get(Calendar.YEAR),
				begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH))
				.show();
	}

	public void onTimeClick(View v) {
		new TimePickerDialog(ActionDialog.this, t,
				begin.get(Calendar.HOUR_OF_DAY), begin.get(Calendar.MINUTE),
				true).show();
	}

	public void onSubmit(View v) {
		ContentValues values = new ContentValues(7);

		String tmpName = name.getText().toString().trim();
		String tmpOrganizators = organizators.getText().toString().trim();
		String tmpLocation = location.getText().toString().trim();
		String tmpBegin = Help.calToString(begin);
		String tmpDurationBlocks = durationBlocks.getText().toString().trim();
		String tmpDurationPres = durationPres.getText().toString().trim();
		String tmpNotes = notes.getText().toString().trim();
		int progress = warningSeekBar.getProgress();
		
		if (tmpName.equals("") || tmpDurationBlocks.equals("") || tmpDurationPres.equals("")) {
			Toast.makeText(this, "Polia označené * sú povinné!", Toast.LENGTH_LONG).show();
		} else if (!editMode && !begin.after(Calendar.getInstance())) {
			Toast.makeText(this, "Začiatok akcie nemožno plánovať do minulosti!", Toast.LENGTH_LONG).show();
		} else if (Integer.parseInt(tmpDurationBlocks) <= Integer.parseInt(tmpDurationPres)) {
			Toast.makeText(this, "Trvanie prezentácie musí byť menšie ako trvanie bloku!", Toast.LENGTH_LONG).show();
		} else {
		   int intDurationPres = Integer.parseInt(tmpDurationPres);
		   int warning = (int) (intDurationPres / 2 * 60 / 100.0 * progress);
			values.put(Data.A_NAME, tmpName);
			values.put(Data.A_ORGANIZATORS, tmpOrganizators);
			values.put(Data.A_LOCATION, tmpLocation);
			values.put(Data.A_BEGIN, tmpBegin);
			values.put(Data.A_DUR_BLOCKS, Integer.parseInt(tmpDurationBlocks));
			values.put(Data.A_DUR_PRES, Integer.parseInt(tmpDurationPres));
			values.put(Data.A_WARNING, warning);
			values.put(Data.A_NOTES, tmpNotes);

			if (editMode) {
				String[] params = { String.valueOf(action) };
				db.update(Data.TABLE_ACTIONS, values, Data.A_ID + "=?", params);	
			}
			else {
				db.insert(Data.TABLE_ACTIONS, Data.A_NAME, values);
			}
			setResult(RESULT_OK);
			finish();
		}
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

}

