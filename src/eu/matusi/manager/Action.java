package eu.matusi.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import eu.matusi.manager.PresentationManager.createExportFileException;
import eu.matusi.manager.database.Data;
import eu.matusi.manager.dragndrop.DragNDropListActivity;
import eu.matusi.manager.help.Help;
import eu.matusi.manager.presmode.Main;

public class Action extends Activity
{

   private final static int   EDIT_PRESENTATION = 0;
   private final static int   EDIT_BREAK        = 1;
   private final static int   ADD_PRESENTATION  = 2;
   private final static int   NOTES             = 3;
   private final static int   REARRANGE         = 4;
   private static final int   ADD_BREAK_TO      = 7;
   
   private final static int EXPORT = 8;
   private final int CONF_EXPORT = 9;
   private final static String ACTION_ID = "action_id";

   private SQLiteDatabase     db;
   
   private int                action;
   private String             actionBegin;
   private int                actionDurationBlocks;
   
   CursorAdapter              adapter;
   private int                currentID;
   
   

   public static final String PARAM_ACTION      = "action";

   Calendar                   begin             = Calendar.getInstance();
   Calendar                   end               = Calendar.getInstance();
   
   Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         setCurrentPresentation();
      }
   };
   
   private boolean reminderIsRunning = false;


   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      LinearLayout ll = (LinearLayout) findViewById(R.id.all);
      //ll.removeAllViews();
      setContentView(R.layout.action);

      db = (new Data(this)).getWritableDatabase();

      getActionParameters();

      
      
      Cursor c = Data.getBlocksCursor(db, action);
      
      getBlocksAdapter(c);
     

      ListView lv = (ListView) findViewById(R.id.mylist);
      lv.setAdapter(adapter);

      lv.setDividerHeight(3);

      OnItemClickListener l = new OnItemClickListener() {

         public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
         {

            Cursor result = Data.getBlockCursor(db, (int) arg3);
            int x = Data.getInt(result, Data.B_TYPE);
            if (Data.getInt(result, Data.B_TYPE) == Data.TYPE_BREAK) {
               Intent intent = new Intent(Action.this, EditBreak.class);
               intent.putExtra(EditBreak.PARAM_PRESENTATION, (int) arg3);
               intent.putExtra(EditBreak.PARAM_ACTION, action);
               startActivityForResult(intent, EDIT_BREAK);

            }
            else {
               Intent intent2 = new Intent(Action.this, PresentationDialog.class);
               intent2.putExtra(PresentationDialog.PARAM_EDIT_MODE, true);
               intent2.putExtra(PresentationDialog.PARAM_ACTION, action);
               intent2.putExtra(PresentationDialog.PARAM_PRESENTATION, (int) arg3);
               intent2.putExtra(PresentationDialog.PARAM_POSITION, arg2);
               startActivityForResult(intent2, EDIT_PRESENTATION);
            }

         }

      };
      lv.setOnItemClickListener(l);

      registerForContextMenu(lv);

      refresh();
      
      /*if (c.getCount() == 0) {
         Log.d("e", "dostal som sa");
         ll = (LinearLayout) findViewById(R.id.all);
         ll.removeAllViews();
         Button button = new Button(this);
         button.setText("Pridať prezentáciu");
         LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
         button.setLayoutParams(lp);
         button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v)
            {
               Intent intent = new Intent(Action.this, PresentationDialog.class);
               intent.putExtra(PresentationDialog.PARAM_EDIT_MODE, false);
               intent.putExtra(PresentationDialog.PARAM_ACTION, action);
               intent.putExtra(PresentationDialog.PARAM_POSITION, getBlocksCursor().getCount());
               startActivityForResult(intent, ADD_PRESENTATION);
               
            }
            
         });
         
         ll.addView(button);
      }*/
      
   }
   
   public void onStart() {
      super.onStart();
      
      Thread reminder = new Thread(new Runnable() {

         public void run()
         {
            // TODO Auto-generated method stub
            try {
               while (reminderIsRunning) {
                  
                  handler.sendMessage(handler.obtainMessage());
                  Thread.sleep(2000);
               }
            }
            catch (Throwable t) {}
         }
         
      });
      
      reminderIsRunning = true;
      reminder.start();
   }
   
   public void onStop() {
      super.onStop();
      
      reminderIsRunning = false;
   }
   
   private Cursor getBlocksCursor() {
      return Data.getBlocksCursor(db, action);
   }

   private void getBlocksAdapter(Cursor blocksCursor)
   {
      String[] arrCols = { Data.P_NAME, Data.B_BEGIN };
      int[] arrIds = { R.id.p_name, R.id.p_datetime };
      adapter = new MySimpleCursorAdapter(this, R.layout.action_row, blocksCursor, arrCols, arrIds);
   }

   class MySimpleCursorAdapter extends SimpleCursorAdapter
   {
      MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
      {
         super(context, layout, c, from, to);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {

         View row = convertView;

         if (row == null) {
            LayoutInflater inflater = getLayoutInflater();

            row = inflater.inflate(R.layout.action_row, parent, false);
         }

         TextView name = (TextView) row.findViewById(R.id.p_name);
         TextView begin = (TextView) row.findViewById(R.id.p_datetime);
         ImageView image = (ImageView) row.findViewById(R.id.p_imageview);

         String cols = Data.B_BEGIN + ", " + Data.P_NAME + ", " + Data.B_TYPE;

        
         Cursor cursor = db.rawQuery("SELECT " + cols + " FROM " + Data.TABLE_BLOCKS + " WHERE " + Data.B_ACTION + "=\"" + action + "\" AND " + Data.B_POSITION
               + "=\"" + position + "\"", null);
         cursor.moveToFirst();
         if (cursor.getCount() == 0)
          Log.d("MyErrors", "uz viem kde je pes");
         if (Data.getInt(cursor, Data.B_TYPE) == Data.TYPE_PRESENTATION) {
            name.setText(Data.getString(cursor, Data.P_NAME));
            name.setTextColor(Color.RED);
            image.setImageResource(R.drawable.presentation);
         }
         else {
            name.setText("Prestávka");
            name.setTextColor(Color.GRAY);
            image.setImageResource(R.drawable.coffee);
         }
         begin.setText(Data.getString(cursor, Data.B_BEGIN));
         return (row);
      }

   }

   private void getActionParameters()
   {
      action = getIntent().getExtras().getInt(PARAM_ACTION);
      
      Cursor result = Data.getActionCursor(db, action);
      actionBegin = Data.getString(result, Data.A_BEGIN);
      actionDurationBlocks = Data.getInt(result, Data.A_DUR_BLOCKS);
   }

   @Override
   public void onResume()
   {
      super.onResume();

   }

   public static void staticOrganize(SQLiteDatabase db, int action, int implicitDurBlocks, String beginStr)
   {
      Calendar cal = Help.stringToCal(beginStr);
      Cursor blocksCursor = Data.getBlocksCursor(db, action);
      blocksCursor.moveToFirst();
      while (!blocksCursor.isAfterLast()) {

         ContentValues values = new ContentValues();
         values.put(Data.B_BEGIN, Help.calToString(cal));

         String position = Data.getString(blocksCursor, Data.B_POSITION);
         String[] params = { String.valueOf(action), position };
         db.update(Data.TABLE_BLOCKS, values, Data.B_ACTION + "=? AND " + Data.B_POSITION + "=?", params);

         if (Data.isNull(blocksCursor, Data.B_DUR_BLOCKS)) {
            cal.add(Calendar.MINUTE, implicitDurBlocks);
         }
         else {
            cal.add(Calendar.MINUTE, Data.getInt(blocksCursor, Data.B_DUR_BLOCKS));
         }

         blocksCursor.moveToNext();
      }
      blocksCursor.close();
   }

   private void organize()
   {
      //Log.d("YourTag", "YourOutput");
      Calendar cal = Help.stringToCal(actionBegin);
      Cursor blocksCursor = Data.getBlocksCursor(db, action);
      while (!blocksCursor.isAfterLast()) {

         ContentValues values = new ContentValues();
         values.put(Data.B_BEGIN, Help.calToString(cal));

         String position = Data.getString(blocksCursor, Data.B_POSITION);
         String[] params = { String.valueOf(action), position };
         db.update(Data.TABLE_BLOCKS, values, Data.B_ACTION + "=? AND " + Data.B_POSITION + "=?", params);

         if (Data.isNull(blocksCursor, Data.B_DUR_BLOCKS)) {
            cal.add(Calendar.MINUTE, actionDurationBlocks);
         }
         else {
            cal.add(Calendar.MINUTE, Data.getInt(blocksCursor, Data.B_DUR_BLOCKS));
         }

         blocksCursor.moveToNext();
      }
      blocksCursor.close();
      adapter.changeCursor(getBlocksCursor());
   }

   private void setCurrentPresentation()
   {
      LayoutInflater inflater = getLayoutInflater();

      ViewGroup layout = (ViewGroup) findViewById(R.id.cp_current_presentation);

      String name;
      String authors;
      int durationPres;

      Date now = new Date();

      Cursor result = Data.getBlocksCursor(db, action);
      while (!result.isAfterLast()) {

         name = Data.getString(result, Data.P_NAME);
         authors = Data.getString(result, Data.P_AUTHORS);
         String beginPresentation = Data.getString(result, Data.B_BEGIN);

         if (Data.isNull(result, Data.B_DUR_BLOCKS))
            durationPres = actionDurationBlocks;
         else
            durationPres = Data.getInt(result, Data.B_DUR_BLOCKS);

         Calendar begin = Help.stringToCal(beginPresentation);
         Calendar end = Help.stringToCal(beginPresentation);
         end.setTimeInMillis(end.getTimeInMillis() + durationPres * 60000);

         if (now.after(begin.getTime()) && now.before(end.getTime()) && Data.getInt(result, Data.B_TYPE) == Data.TYPE_PRESENTATION) {
            currentID = Data.getInt(result, Data.B_ID);
            View current = inflater.inflate(R.layout.current_presentation, layout, false);
            TextView nameView = (TextView) current.findViewById(R.id.cp_name);
            TextView authorsView = (TextView) current.findViewById(R.id.cp_authors);
            TextView beginView = (TextView) current.findViewById(R.id.cp_datetime);

            nameView.setText(name);
            authorsView.setText(authors);
            beginView.setText(beginPresentation);
            layout.removeAllViews();
            layout.addView(current);
            return;
         }
         else {
            layout.removeAllViews();
         }
         result.moveToNext();
      }
      result.close();

   }

   public void refresh()
   {
      organize();
      removeDuplicateBreaks();
      organize();
      setCurrentPresentation();
   }

   @Override
   public void onDestroy()
   {
      super.onDestroy();

      db.close();
   }

   public void onClickCurrent(View v)
   {
      Intent intent = new Intent(this, Presentation.class);      
      intent.putExtra(Presentation.PARAM_PRESENTATION, currentID);
      intent.putExtra(Presentation.PARAM_ACTION, action);
      startActivity(intent);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {

      menu.add(Menu.NONE, 1, Menu.NONE, "Nová prezentácia");
      menu.add(Menu.NONE, 2, Menu.NONE, "Upraviť akciu");
      menu.add(Menu.NONE, 3, Menu.NONE, "Poznámky");
      menu.add(Menu.NONE, 4, Menu.NONE, "Preusporiadať");
      menu.add(Menu.NONE, 5, Menu.NONE, "Export");
      menu.add(Menu.NONE, 6, Menu.NONE, "Konfigurovateľný export");
      menu.add(Menu.NONE, 7, Menu.NONE, "Vymazať");
      return (super.onCreateOptionsMenu(menu));
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case 1:
            Intent intent = new Intent(this, PresentationDialog.class);
            intent.putExtra(PresentationDialog.PARAM_EDIT_MODE, false);
            intent.putExtra(PresentationDialog.PARAM_ACTION, action);
            intent.putExtra(PresentationDialog.PARAM_POSITION, getBlocksCursor().getCount());
            startActivityForResult(intent, ADD_PRESENTATION);
            return (true);


         case 2:
            PresentationManager.editAction(this, action);
            refresh();
            return (true);

         case 3:
            Intent intent2 = new Intent(this, EditActionNotes.class);
            intent2.putExtra(EditActionNotes.PARAM_ACTION, action);
            startActivityForResult(intent2, NOTES);
            return (true);

         case 4:
            Intent intent3 = new Intent(this, DragNDropListActivity.class);
            intent3.putExtra(DragNDropListActivity.PARAM_ACTION, action);
            startActivityForResult(intent3, REARRANGE);
            return (true);
            
         case 5:
            Intent intent4 = new Intent(this, FileDialog.class);
            intent4.putExtra(FileDialog.START_PATH, "/");

            // can user select directories or not
            intent4.putExtra(FileDialog.CAN_SELECT_DIR, false);

            // alternatively you can set file filter
            intent4.putExtra(FileDialog.FORMAT_FILTER, new String[] { "csv",
                  "CSV" });
            intent4.putExtra(FileDialog.SELECTION_MODE,
                  SelectionMode.MODE_CREATE);
            intent4.putExtra(ACTION_ID, action);
            startActivityForResult(intent4, EXPORT);
           return (true);
           
         case 6:
            Intent intent5 = new Intent(this, FileDialog.class);
            intent5.putExtra(FileDialog.START_PATH, "/");

            // can user select directories or not
            intent5.putExtra(FileDialog.CAN_SELECT_DIR, false);

            // alternatively you can set file filter
            intent5.putExtra(FileDialog.FORMAT_FILTER, new String[] { "csv",
                  "CSV" });
            intent5.putExtra(FileDialog.SELECTION_MODE,
                  SelectionMode.MODE_CREATE);
            intent5.putExtra(ACTION_ID, action);
            startActivityForResult(intent5, CONF_EXPORT );
            return (true);
            
         case 7:
            new AlertDialog.Builder(this)
            .setTitle(R.string.actions_delete_question)
            .setPositiveButton(R.string.submit,
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog,
                           int whichButton) {
                        String[] args = { String.valueOf(action) };

                        db.delete("actions", "_id=?", args);
                        Intent intent4 = new Intent(Action.this, PresentationManager.class);
                        startActivity(intent4);
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

      }
      


      return (super.onContextItemSelected(item));

   }

   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      if (resultCode == RESULT_OK) {
         if (requestCode == ADD_BREAK_TO) {
            refresh();
         }
         if ((requestCode == ADD_PRESENTATION) || (requestCode == REARRANGE) || (requestCode == EDIT_PRESENTATION) || requestCode == EDIT_BREAK) {
            refresh();
         }
         if (requestCode == EXPORT) {
            if (resultCode == RESULT_OK) {
               if (requestCode == EXPORT) {
                  if (resultCode == RESULT_OK) {
                     String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                     PresentationManager.makeExport(this, filePath + ".csv", data.getIntExtra(ACTION_ID, 1));
                  } else if (resultCode == RESULT_CANCELED) {
                  }
               }
            } else if (resultCode == RESULT_CANCELED) {
            }
         }
         else if (requestCode == CONF_EXPORT) {
            if (resultCode == RESULT_OK) {
               String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
               try {
                  PresentationManager.createConfExportFile(this, filePath + ".csv", data.getIntExtra(ACTION_ID, 1));
               }
               catch (createExportFileException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            else if (resultCode == RESULT_CANCELED) {
            }
         }
      }
      setCurrentPresentation();

   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
   {
      TextView name_view = (TextView) v.findViewById(R.id.p_name);
      android.util.Log.w("Actions", String.valueOf(name_view.getText()));
      String.valueOf(name_view.getText());
      menu.add(Menu.NONE, 1, Menu.NONE, "Nová prestávka s trvaním...");
      menu.add(Menu.NONE, 2, Menu.NONE, "Nová prestávka do času...");
      menu.add(Menu.NONE, 3, Menu.NONE, "Vymazať");
   }

   public static int dialogSingleChoice = 0;

   @Override
   public boolean onContextItemSelected(MenuItem item)
   {

      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

      switch (item.getItemId())
      {

         case 1:

            LayoutInflater inflater = LayoutInflater.from(this);
            View addView = inflater.inflate(R.layout.add_break, null);
            final DialogWrapper wrapper = new DialogWrapper(addView);

            new AlertDialog.Builder(this).setTitle(R.string.add_break_dialog).setView(addView)
                  .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton)
                     {
                        addPause(info.position, wrapper.getInt());
                        refresh();
                     }
                  }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton)
                     {
                        // ignore, just dismiss
                     }
                  }).show();

            return (true);

         case 2:
            Intent intent = new Intent(this, AddBreakTo.class);
            intent.putExtra(AddBreakTo.PARAM_ACTION, action);
            intent.putExtra(AddBreakTo.PARAM_POSITION, info.position);
            startActivityForResult(intent, ADD_BREAK_TO);

            return (true);

         case 3:
            Cursor c = Data.getBlockCursor(db, (int) info.id);
            if (Data.getInt(c, Data.B_TYPE) == Data.TYPE_BREAK) {
               new AlertDialog.Builder(this).setTitle(R.string.presentations_delete_question)
                     .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                           String[] args = { String.valueOf(info.id) };

                           db.delete(Data.TABLE_BLOCKS, Data.B_ID + "=?", args);
                           changePositions(info.position + 1, false);
                           refresh();
                        }
                     }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                           // ignore, just dismiss
                        }
                     }).show();
            }
            else {
               CharSequence[] items = { "Nasledujúce prezentácie nemeniť", "Nasledujúce prezentácie presunúť dopredu" };
               dialogSingleChoice = 0;

               new AlertDialog.Builder(this).setTitle(R.string.presentations_delete_question).setSingleChoiceItems(items, 0, new OnClickListener() {

                  public void onClick(DialogInterface dialog, int which)
                  {
                     Action.dialogSingleChoice = which;

                  }
               }).setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton)
                  {
                     Cursor c = Data.getBlockCursor(db, (int) info.id);
                     int duration = Data.getInt(c, Data.B_DUR_BLOCKS);

                     String[] args = { String.valueOf(info.id) };

                     db.delete(Data.TABLE_BLOCKS, Data.B_ID + "=?", args);
                     changePositions(info.position + 1, false);
                     refresh();
                     if (Action.dialogSingleChoice == 0) {
                        addPause(info.position, duration);
                        refresh();
                     }
                  }
               }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton)
                  {
                     // ignore, just dismiss
                  }
               }).show();

            }

            return (true);

      }

      return (super.onContextItemSelected(item));

   }

   public static void changePositions(SQLiteDatabase db, int action, int position, boolean increment)
   {
      String[] columns = { Data.B_ID, Data.B_POSITION };
      String[] parms = { String.valueOf(action), String.valueOf(position) };
      Cursor result = db.query(Data.TABLE_BLOCKS, columns, Data.B_ACTION + "=? AND " + Data.B_POSITION + ">=?", parms, null, null, null);
      result.moveToFirst();
      while (!result.isAfterLast()) {

         String[] params = { String.valueOf(Data.getInt(result, Data.B_ID)) };
         ContentValues values = new ContentValues();

         if (increment)
            values.put(Data.B_POSITION, Data.getInt(result, Data.B_POSITION) + 1);
         else
            values.put(Data.B_POSITION, Data.getInt(result, Data.B_POSITION) - 1);

         db.update(Data.TABLE_BLOCKS, values, Data.B_ID + "=?", params);
         result.moveToNext();
      }
      result.close();
   }

   private void changePositions(int position, boolean increment)
   {
      changePositions(db, action, position, increment);
   }

   private void removeDuplicateBreaks()
   {
      Cursor blocks = Data.getBlocksCursor(db, action);
      boolean row = false;
      int length = 0;
      int startPosition = 0;
      int endPosition = 0;
      long startTime = 0;
      long endTime = 0;
      while (!blocks.isAfterLast()) {
         if (Data.getInt(blocks, Data.B_TYPE) == Data.TYPE_BREAK) {
            if (!row) {
               length = 0;
               startPosition = Data.getInt(blocks, Data.B_POSITION);
               Calendar startCal = Help.stringToCal(Data.getString(blocks, Data.B_BEGIN));
               startTime = startCal.getTimeInMillis();
               // zaciatok radu prestavok
               row = true;
            }
            length++;
         }
         else {
            if (row) {
               if (length >= 2) {
                  endPosition = Data.getInt(blocks, Data.B_POSITION);
                  Calendar endCal = Help.stringToCal(Data.getString(blocks, Data.B_BEGIN));
                  endTime = endCal.getTimeInMillis();
                  // mam rad prestavok
                  removeRowBreaks(startPosition, endPosition, endTime - startTime);
                  return;
               }
               row = false;
            }
         }

         blocks.moveToNext();
      }
      /*
       * if (row && length >= 2) { endPosition = Data.getInt(blocks,
       * Data.B_POSITION); //mam rad prestavok removeRowBreaks(startPosition,
       * endPosition); }
       */
   }

   private void removeRowBreaks(int startPosition, int endPosition, long timeMilis)
   {
      for (int i = startPosition; i < endPosition; i++) {
         db.delete(Data.TABLE_BLOCKS, Data.B_POSITION + "=?", new String[] { String.valueOf(startPosition) });
         changePositions(startPosition + 1, false);
         refresh();
      }

      changePositions(startPosition, true);

      ContentValues values = new ContentValues();
      values.put(Data.P_NAME, "<Prestávka>");
      values.put(Data.B_ACTION, action);
      values.put(Data.B_BEGIN, "");
      values.put(Data.B_DUR_BLOCKS, timeMilis / 60000);
      values.put(Data.B_POSITION, startPosition);
      values.put(Data.B_TYPE, Data.TYPE_BREAK);

      db.insert(Data.TABLE_BLOCKS, Data.P_NAME, values);

   }

   private void addPause(int position, int minutes)
   {
      changePositions(position, true);

      ContentValues values = new ContentValues();

      values.put(Data.P_NAME, "<Prestávka>");
      values.put(Data.B_ACTION, action);
      values.put(Data.B_BEGIN, "");
      values.put(Data.B_DUR_BLOCKS, minutes);
      values.put(Data.B_POSITION, position);
      values.put(Data.B_TYPE, Data.TYPE_BREAK);

      db.insert(Data.TABLE_BLOCKS, Data.P_NAME, values);
   }

   class DialogWrapper
   {
      EditText field = null;
      View     base  = null;

      DialogWrapper(View base)
      {
         this.base = base;
         field = (EditText) base.findViewById(R.id.ab_edit);
      }

      int getInt()
      {
         return Integer.parseInt(getField().getText().toString());
      }

      private EditText getField()
      {
         if (field == null) {
            field = (EditText) base.findViewById(R.id.ab_edit);
         }

         return (field);
      }

   }




}