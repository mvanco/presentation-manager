package eu.matusi.manager;
//toto je moje
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;
import eu.matusi.manager.presmode.Main;

public class PresentationManager extends ListActivity {

	public static final int ADD_ACTION = 0;
	public static final int EDIT_ACTION = 1;
	private final static int EXPORT = 2;
	private final static int IMPORT = 3;
	private final static int CONF_EXPORT = 4;
	private final static String ACTION_ID = "action_id";

	private SQLiteDatabase db = null;
	private CursorAdapter adapter = null;
	private String orderBy = Data.A_NAME;
	private Calendar importBegin = null;
	private int importPosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		db = (new Data(this)).getWritableDatabase();

		Cursor actionsCursor = getActionsCursor();
		String[] arrCols = { Data.A_NAME, Data.A_ORGANIZATORS, Data.A_LOCATION,
				Data.A_BEGIN, Data.A_DUR_BLOCKS, Data.A_DUR_PRES };
		int[] arrIds = { R.id.name, R.id.organizators, R.id.locations,
				R.id.begin, R.id.duration, R.id.reservedtime };
		adapter = new SimpleCursorAdapter(this, R.layout.row, actionsCursor,
				arrCols, arrIds);
		setListAdapter(adapter);
		getListView().setDividerHeight(3);
		registerForContextMenu(getListView());
	}

	private Cursor getActionsCursor() {

		// Data.A_ID Data.A_NAME Data.A_ORGANIZATORS Data.A_LOCATION
		// Data.A_BEGIN Data.A_DUR_BLOCKS Data.A_DUR_PRES Data.A_WARNING
		// Data.A_NOTES
		String cols = Data.A_ID + ", " + Data.A_NAME + ", "
				+ Data.A_ORGANIZATORS + ", " + Data.A_LOCATION + ", "
				+ Data.A_BEGIN + ", " + Data.A_DUR_BLOCKS + ", "
				+ Data.A_DUR_PRES;
		Cursor cursor = db.rawQuery("SELECT " + cols + " FROM "
				+ Data.TABLE_ACTIONS + " ORDER BY " + orderBy, null);
		return cursor;
	}

	private void refresh() {
		adapter.changeCursor(getActionsCursor());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		db.close();
	}

	public void onButtonClick(View button) {
		Intent intent = new Intent(this, ActionDialog.class);
		intent.putExtra(ActionDialog.PARAM_EDIT_MODE, false);
		startActivityForResult(intent, ADD_ACTION);
	}

	public void onMod2Click(View v) {
		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		Intent intent = new Intent(this, Action.class);
		intent.putExtra(Action.PARAM_ACTION, (int) id);
		startActivity(intent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_ACTION && resultCode == RESULT_OK) {
			refresh();
		} else if (requestCode == EDIT_ACTION && resultCode == RESULT_OK) {
			refresh();
		} else if (requestCode == EXPORT) {
			if (resultCode == RESULT_OK) {
				String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
				makeExport(this, filePath + ".csv", data.getIntExtra(ACTION_ID, 1));
			} else if (resultCode == RESULT_CANCELED) {
			}
		} 
		else if (requestCode == CONF_EXPORT) {
		   if (resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            try {
               createConfExportFile(this, filePath + ".csv", data.getIntExtra(ACTION_ID, 1));
            }
            catch (createExportFileException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
		   else if (resultCode == RESULT_CANCELED) {
         }
		}
		else if (requestCode == IMPORT) {
         if (resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            makeImport(filePath);
            refresh();
         } else if (resultCode == RESULT_CANCELED) {
         }
      }
	}
	
	public static void makeExport(Context c, String filePath, int action) {
	   BufferedWriter fw = null;
      try {
         fw = new BufferedWriter(new FileWriter(new File(filePath)));
         if (fw != null)
            createExportFile(c, fw, action);
         fw.close();
      }
      catch (IOException e) {
         Toast.makeText(c, "Nemôžem vytvoriť daný súbor, nemám dostatočné oprávnenia", Toast.LENGTH_SHORT).show();
      }
      catch (createExportFileException e) {
         Toast.makeText(c, "Nenašiel som odpovedajúcu tabuľku akcie a aspoň jednej prezentácie", Toast.LENGTH_SHORT).show();
      }      
	}
	
	public static class createExportFileException extends Exception {
	   private static final long serialVersionUID = 1L;
	}
	
	private static void createExportFile (Context context, BufferedWriter fw, int action) throws createExportFileException {
	   StringBuilder exportBuilder = new StringBuilder();
	   SQLiteDatabase db = (new Data(context)).getWritableDatabase();
	   Cursor actionC = Data.getActionCursor(db, action);
	   if (actionC.moveToFirst()) {
	      exportBuilder.append(Data.getString(actionC, Data.A_NAME));
	      exportBuilder.append(" ;");
         exportBuilder.append(Data.getString(actionC, Data.A_ORGANIZATORS));
         exportBuilder.append(" ;");
         exportBuilder.append(Data.getString(actionC, Data.A_LOCATION));
         exportBuilder.append(" ;");
         exportBuilder.append(Data.getString(actionC, Data.A_BEGIN));
         exportBuilder.append(" ;");
         exportBuilder.append(Data.getInt(actionC, Data.A_DUR_BLOCKS));
         exportBuilder.append(" ;");
         exportBuilder.append(Data.getInt(actionC, Data.A_DUR_PRES));
         exportBuilder.append(" ;");
         exportBuilder.append(Data.getInt(actionC, Data.A_WARNING));
         exportBuilder.append(" ;");
         exportBuilder.append(Data.getString(actionC, Data.A_NOTES));
         exportBuilder.append(" \n");
	   }
	   else {
	      throw new createExportFileException();
	   }
	   
	   Cursor c = Data.getBlocksCursor(db, action);
	   if (c.moveToFirst()) {
         while (!c.isAfterLast()) {
            if (Data.getInt(c, Data.B_TYPE) == Data.TYPE_PRESENTATION) {
               if (!Data.isNull(c, Data.B_BEGIN))
                  exportBuilder.append(Data.getString(c, Data.B_BEGIN));
               exportBuilder.append(" ;");
               exportBuilder.append(Data.getString(c, Data.P_NAME));
               exportBuilder.append(" ;");
               exportBuilder.append(Data.getString(c, Data.P_AUTHORS));
               exportBuilder.append(" ;");
               exportBuilder.append(Data.getInt(c, Data.B_DUR_BLOCKS));
               exportBuilder.append(" ;");
               exportBuilder.append(Data.getInt(c, Data.P_DUR_PRES));
               exportBuilder.append(" ;");
               if (!Data.isNull(c, Data.P_POINTS))
                  exportBuilder.append(Data.getInt(c, Data.P_POINTS));
               exportBuilder.append(" ;");
               exportBuilder.append(Data.getString(c, Data.P_NOTES));

               exportBuilder.append(" \n");
               
            }
            c.moveToNext();
         }
         
         try {
            fw.write(exportBuilder.toString());
         }
         catch (IOException e) {
            Toast.makeText(context, "Nemôžem zapísať do súboru", Toast.LENGTH_SHORT).show();
         }
	   }
	   else {
	      throw new createExportFileException();
	   }
	   
	   db.close();
	   
	}
	
	
	public static class AddCol {
	   private SharedPreferences pref;
	   private Cursor cursor;
	   private boolean first = true;
	   private StringBuilder sb;
	   private Context context;
	   
	   AddCol(Context con, Cursor c) {
	      context = con;
	      cursor = c;
	      pref = PreferenceManager.getDefaultSharedPreferences(context);
	      sb = new StringBuilder();
	   }
	   
	   public void insert(String prefName, String colName, int type) {
	      if (pref.getBoolean("empty_col", false))
	         if (first) first = false; else sb.append(" ;");
	      if (pref.getBoolean(prefName, false)) {
	         if (!pref.getBoolean("empty_col", false)) if (first) first = false; else sb.append(" ;");
	         if (type == 0) sb.append(Data.getString(cursor, colName));
	         else if (type == 1) sb.append(Data.getInt(cursor, colName));
	         else {
	            if (!Data.isNull(cursor, colName))
                  sb.append(Data.getInt(cursor, colName));
	         }
	      }
	   }
	   
	   public String getString() {
	      return sb.toString();
	   }
	   
	   public void newLine() {
	      sb.append(" \n");
	      first = true;
	   }
	   
	   public void setCursor(Cursor c) {
	      cursor = c;
	   }
	   
	   
	   
	}
	
	
	  public static void createConfExportFile(Context context, String filePath, int action) throws createExportFileException {
	     
	      BufferedWriter fw = null;
	      try {
	         fw = new BufferedWriter(new FileWriter(new File(filePath)));
	         if (fw != null) {
	            
	         //zaciatok povodnej funkcie
	         SQLiteDatabase db = (new Data(context)).getWritableDatabase();
	         Cursor actionC = Data.getActionCursor(db, action);
	         Cursor c = Data.getBlocksCursor(db, action);
	         
	         AddCol addCol = new AddCol(context, actionC);

	         if (actionC.moveToFirst()) {
	            
	            addCol.insert("a_name", Data.A_NAME, 0);
	            addCol.insert("a_organizators", Data.A_ORGANIZATORS, 0);
	            addCol.insert("a_location", Data.A_LOCATION, 0);
	            addCol.insert("a_begin", Data.A_BEGIN, 0);
	            addCol.insert("a_dur_blocks", Data.A_DUR_BLOCKS, 1);
	            addCol.insert("a_dur_pres", Data.A_DUR_PRES, 1);
	            addCol.insert("a_warning", Data.A_WARNING, 1);
	            addCol.insert("a_notes", Data.A_NOTES, 0);
	            addCol.newLine();
	         }
	         else {
	            throw new createExportFileException();
	         }
	         
	         addCol.setCursor(c);
	         if (c.moveToFirst()) {
	            while (!c.isAfterLast()) {
	               if (Data.getInt(c, Data.B_TYPE) == Data.TYPE_PRESENTATION) {
	                  
	                  addCol.insert("p_begin", Data.B_BEGIN, 0);
	                  addCol.insert("p_name", Data.P_NAME, 0);
	                  addCol.insert("p_authors", Data.P_AUTHORS, 0);
	                  addCol.insert("p_dur_blocks", Data.B_DUR_BLOCKS, 1);
	                  addCol.insert("p_dur_pres", Data.P_DUR_PRES, 1);
	                  addCol.insert("p_points", Data.P_POINTS, 2);
	                  addCol.insert("p_notes", Data.P_NOTES, 0);
	                  addCol.newLine();
	               }
	               c.moveToNext();
	            }
	            
	            try {
	               fw.write(addCol.getString());
	            }
	            catch (IOException e) {
	               Toast.makeText(context, "Nemôžem zapísať do súboru", Toast.LENGTH_SHORT).show();
	            }
	         }
	         else {
	            throw new createExportFileException();
	         }
	         
	         
	         
	         db.close();
	         //koniec povodnej funkcie
	         }
	         fw.close();
	      }
	      catch (IOException e) {
	         Toast.makeText(context, "Nemôžem vytvoriť daný súbor, nemám dostatočné oprávnenia", Toast.LENGTH_SHORT).show();
	      }
	      catch (createExportFileException e) {
	         Toast.makeText(context, "Nenašiel som odpovedajúcu tabuľku akcie a aspoň jednej prezentácie", Toast.LENGTH_SHORT).show();
	      }  
	   }
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void makeImport(String filePath) {
	   BufferedReader fr = null;
	   BufferedReader fr2 = null;
	   try {
	      fr = new BufferedReader(new FileReader(new File(filePath)));
	      fr2 = new BufferedReader(new FileReader(new File(filePath)));
	   } catch (FileNotFoundException e) {
	      Toast.makeText(this, "Nenašiel som daný súbor", Toast.LENGTH_SHORT).show();
	   }
	   
	   if (fr != null) {
         if (isValidCSVFormat(fr)) {
            if (!createTablesFromCSV(fr2)) {
               Toast.makeText(this, "Prezentácie nenasledujú v CSV za sebou", Toast.LENGTH_SHORT).show();
            }
         }
         else {
            Toast.makeText(this, "Nesprávny formát súboru", Toast.LENGTH_SHORT).show();
         }
	   }
	}
	
	private boolean isValidCSVFormat(BufferedReader fr) {
	   String actionLine = null;
      try {
         actionLine = fr.readLine();
      }
      catch (IOException e) {
         return false;
      }
      if (actionLine == null)
         return false;
      List<String> colList = Arrays.asList(actionLine.split(";"));
      
      if ( colList.size() == 8 ) {
         
         if (colList.get(0).trim().equals(""))
            return false;
         
         if (Help.stringToCal(colList.get(3)) == null)
            return false;
         
         try {
            Integer.parseInt(colList.get(4).trim());
            Integer.parseInt(colList.get(5).trim());
            Integer.parseInt(colList.get(6).trim());
         } catch (NumberFormatException e) {
            return false;
         }
      }
      else {
         return false;
      }
      
      String presLine = null;
      try {
         while ((presLine = fr.readLine()) != null) {
            if (!isValidCSVPresRow(presLine))
               return false;
         }
      }
      catch (IOException e) {
         return false;
      }
      
	   return true;
	}
	
	private boolean isValidCSVPresRow(String row) {
      List<String> colList = Arrays.asList(row.split(";"));
      
      if ( colList.size() == 7 ) {
         if (!colList.get(0).trim().equals("") && Help.stringToCal(colList.get(0).trim()) == null)
            return false;
         
         if (colList.get(1).trim().equals(""))
            return false;
         
         try {
            Integer.parseInt(colList.get(3).trim());
            Integer.parseInt(colList.get(4).trim());
         } catch (NumberFormatException e) {
            return false;
         }
      }
      else {
         return false;
      }
      
      return true;
	}
	
   private boolean createTablesFromCSV(BufferedReader fr) {
      String actionLine = null;
      try {
         actionLine = fr.readLine();
      }
      catch (IOException e) {
      }
      
      int rowID = createActionTable(actionLine);
      
      String presLine = null;
      importPosition = 0;
      try {
         while ((presLine = fr.readLine()) != null) {
            
            if (!createPresTable(presLine, rowID)) {
               String[] args = { String.valueOf(rowID) };

               db.delete(Data.TABLE_BLOCKS, Data.B_ACTION + "=?", args);
               db.delete(Data.TABLE_ACTIONS, Data.A_ID + "=?", args);
               
               return false;
            }
         }
      }
      catch (IOException e) {
      }
      
      return true;
   }
   
   public int createActionTable(String line) {
      List<String> colList = Arrays.asList(line.split(";"));
      
      ContentValues cv = new ContentValues();
      cv.put(Data.A_NAME, colList.get(0).trim());
      cv.put(Data.A_ORGANIZATORS, colList.get(1).trim());
      cv.put(Data.A_LOCATION, colList.get(2).trim());
      cv.put(Data.A_BEGIN, colList.get(3).trim());
      cv.put(Data.A_DUR_BLOCKS, Integer.parseInt(colList.get(4).trim()));
      cv.put(Data.A_DUR_PRES, Integer.parseInt(colList.get(5).trim()));
      cv.put(Data.A_WARNING, Integer.parseInt(colList.get(6).trim()));
      if (!colList.get(7).trim().equals(""))
         cv.put(Data.A_NOTES, colList.get(7).trim());
      importBegin = Help.stringToCal(colList.get(3).trim());
      return (int) db.insert(Data.TABLE_ACTIONS, Data.A_ID, cv);
   }
   
   private boolean createPresTable(String line, int rowID) {
      List<String> colList = Arrays.asList(line.split(";"));
      String str = colList.get(0);

      // B_ID B_ACTION B_POSITION B_BEGIN B_DUR_BLOCKS B_TYPE P_NAME P_AUTHORS
      // P_DUR_PRES P_NOTES
      
      if (!colList.get(0).trim().equals("")) { //je zadana hodnota
         if (importBegin.equals(Help.stringToCal(colList.get(0).trim())) || importBegin.before(Help.stringToCal(colList.get(0).trim()))) { //je zadany spravny datum
            long startmilis = importBegin.getTimeInMillis();
            String s = importBegin.getTime().toString();
            long endmilis = Help.stringToCal(colList.get(0).trim()).getTimeInMillis();
            s = Help.stringToCal(colList.get(0).trim()).getTime().toString();
            int duration = (int) ((endmilis - startmilis) / 60000);
            ContentValues cv = new ContentValues();
            if (duration != 0) {
               cv.put(Data.B_ACTION, rowID);
               cv.put(Data.B_POSITION, importPosition++);
               cv.put(Data.B_BEGIN, Help.calToString(importBegin));
               cv.put(Data.B_DUR_BLOCKS, duration);
               cv.put(Data.B_TYPE, Data.TYPE_BREAK);
               db.insert(Data.TABLE_BLOCKS, Data.B_ID, cv);
               importBegin.setTimeInMillis(importBegin.getTimeInMillis() + duration * 60000);
            }
            
            
            //zisti dlzku, vlozi prestavku a prepocita importBegin
         }
         else { //je zadany zly datum
            return false;
         }
      }
      
      //vlozime s importBegin standardne
      ContentValues cv = new ContentValues();

      cv.put(Data.B_ACTION, rowID);
      cv.put(Data.B_POSITION, importPosition++);
      
      if (colList.get(0).trim().equals(""))
         cv.put(Data.B_BEGIN, Help.calToString(importBegin));
      
      else
         cv.put(Data.B_BEGIN, Help.calToString(Help.stringToCal(colList.get(0).trim())));
      
      
      cv.put(Data.B_DUR_BLOCKS, Integer.parseInt(colList.get(3).trim()));
      cv.put(Data.B_TYPE, Data.TYPE_PRESENTATION);

      cv.put(Data.P_NAME, colList.get(1).trim());
      cv.put(Data.P_AUTHORS, colList.get(2).trim());
      cv.put(Data.P_DUR_PRES, Integer.parseInt(colList.get(4).trim()));
      if (!colList.get(5).trim().equals(""))
         cv.put(Data.P_POINTS, Integer.parseInt(colList.get(5).trim()));
      if (colList.get(6).trim().equals(""))
         cv.put(Data.P_NOTES, colList.get(6).trim());
      
      db.insert(Data.TABLE_BLOCKS, Data.B_ID, cv);
      importBegin.setTimeInMillis(importBegin.getTimeInMillis() + Integer.parseInt(colList.get(3).trim()) * 60000);
      return true;
   }
   
   
   /*
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
	*/

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		TextView name_view = (TextView) v.findViewById(R.id.name);
		android.util.Log.w("Actions", String.valueOf(name_view.getText()));
		String.valueOf(name_view.getText());
		menu.add(Menu.NONE, 1, Menu.NONE, "Upraviť");
		menu.add(Menu.NONE, 2, Menu.NONE, "Vymazať");
		menu.add(Menu.NONE, 3, Menu.NONE, "Export");
		menu.add(Menu.NONE, 4, Menu.NONE, "Konfigurovateľný export");

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
	
	public static void editAction(Activity c, int id) {
      Intent intent = new Intent(c, ActionDialog.class);
      intent.putExtra(ActionDialog.PARAM_EDIT_MODE, true);
      intent.putExtra(ActionDialog.PARAM_ACTION, id);
      c.startActivityForResult(intent, EDIT_ACTION);
	}
	
	public static void exportActionFileDialog(Activity c, int id, int typeOfAction) {
      Intent intent4 = new Intent(c, FileDialog.class);
      intent4.putExtra(FileDialog.START_PATH, "/");

      // can user select directories or not
      intent4.putExtra(FileDialog.CAN_SELECT_DIR, false);

      // alternatively you can set file filter
      intent4.putExtra(FileDialog.FORMAT_FILTER, new String[] { "csv",
            "CSV" });
      intent4.putExtra(FileDialog.SELECTION_MODE,
            SelectionMode.MODE_CREATE);
      intent4.putExtra(ACTION_ID, id);
      c.startActivityForResult(intent4, typeOfAction);
	}
	
	public static void configuredExportAction(Activity c, int id) {
	   
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case 1:
		   editAction(this, (int) info.id);
			return (true);

		case 2:

			new AlertDialog.Builder(this)
					.setTitle(R.string.actions_delete_question)
					.setPositiveButton(R.string.submit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String[] args = { String.valueOf(info.id) };

									db.delete("actions", "_id=?", args);
									refresh();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// ignore, just dismiss
								}
							}).show();
			return (true);

		case 3:
		   exportActionFileDialog(this, (int) info.id, EXPORT);
			return (true);
		
		case 4:
		   exportActionFileDialog(this, (int) info.id, CONF_EXPORT);
		   return (true);
		
		}
		return (super.onContextItemSelected(item));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   menu.add(Menu.NONE, 1, Menu.NONE, "Nová akcia");
		menu.add(Menu.NONE, 2, Menu.NONE, "Usporiadať podľa názvu");
		menu.add(Menu.NONE, 3, Menu.NONE, "Usporiadať podľa času");
		menu.add(Menu.NONE, 4, Menu.NONE, "Nastavenia");
		menu.add(Menu.NONE, 5, Menu.NONE, "Import");
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	   case 1:
	      Intent intent = new Intent(this, ActionDialog.class);
	      intent.putExtra(ActionDialog.PARAM_EDIT_MODE, false);
	      startActivityForResult(intent, ADD_ACTION);
	      return (true);

		case 2:
			orderBy = Data.A_NAME;
			refresh();
			return (true);

		case 3:
			orderBy = Data.A_BEGIN;
			refresh();
			return (true);

		case 4:
			startActivity(new Intent(this, EditPreferences.class));
			return (true);
			

		case 5:
			Intent intent5 = new Intent(getBaseContext(), FileDialog.class);
			intent5.putExtra(FileDialog.START_PATH, "/");

			// can user select directories or not
			intent5.putExtra(FileDialog.CAN_SELECT_DIR, false);

			// alternatively you can set file filter
			intent5.putExtra(FileDialog.FORMAT_FILTER, new String[] { "csv",
					"CSV" });
			intent5.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
			startActivityForResult(intent5, IMPORT);
			return (true);


		}
		return (super.onContextItemSelected(item));
	}

}