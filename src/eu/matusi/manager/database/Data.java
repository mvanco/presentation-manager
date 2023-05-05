package eu.matusi.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Data extends SQLiteOpenHelper
{

   private static final String   DATABASE_NAME     = "db";

   // A_ID A_NAME A_ORGANIZATORS A_LOCATION A_BEGIN A_DUR_BLOCKS A_DUR_PRES
   // A_WARNING A_NOTES
   public static final String    TABLE_ACTIONS     = "actions";
   public static final String    A_ID              = "_id";
   public static final String    A_NAME            = "name";
   public static final String    A_ORGANIZATORS    = "organizators";
   public static final String    A_LOCATION        = "location";
   public static final String    A_BEGIN           = "begin";
   public static final String    A_DUR_BLOCKS      = "dur_blocks";
   public static final String    A_DUR_PRES        = "dur_pres";
   public static final String    A_WARNING         = "warning";
   public static final String    A_NOTES           = "notes";

   // B_ID B_ACTION B_POSITION B_BEGIN B_DUR_BLOCKS B_TYPE P_NAME P_AUTHORS
   // P_DUR_PRES P_NOTES
   public static final String    TABLE_BLOCKS      = "blocks";
   public static final String    B_ID              = "_id";
   public static final String    B_ACTION          = "action";
   public static final String    B_POSITION        = "position";
   public static final String    B_BEGIN           = "begin";
   public static final String    B_DUR_BLOCKS      = "dur_blocks";
   public static final String    B_TYPE            = "type";
   public static final String    P_NAME            = "name";
   public static final String    P_AUTHORS         = "authors";
   public static final String    P_DUR_PRES        = "dur_pres";
   public static final String    P_POINTS          = "points";
   public static final String    P_NOTES           = "notes";
   
   public static final String    TABLE_RECORDS = "presentations";
   public static final String    R_ID = "_id";
   public static final String    R_NAME = "name";
   public static final String    R_DURATION = "duration";
   public static final String    R_SLIDES = "slides";
   public static final String    R_NOTES = "notes";
   
   public static final String    TABLE_NOTES = "notes";
   public static final String    N_ID = "_id";
   public static final String    N_RECORD = "record";
   public static final String    N_SLIDE = "slide";
   public static final String    N_CONTENT = "content";

   public static final int       TYPE_PRESENTATION = 0;
   public static final int       TYPE_BREAK        = 1;
   
   public static final int  INT_NULL = -1;
   
  
   public static String actionCols = A_ID + ", " + A_NAME + ", " + A_ORGANIZATORS + ", " + A_LOCATION + ", " + A_BEGIN + ", " + A_DUR_BLOCKS + ", " + A_DUR_PRES + ", "
         + A_WARNING + ", " + A_NOTES;
   
   public static String blockCols = B_ID + ", " + B_ACTION + ", " + B_POSITION + ", " + B_BEGIN + ", " + B_DUR_BLOCKS + ", " + B_TYPE + ", " + P_NAME + ", " + P_AUTHORS + ", "
         + P_DUR_PRES + ", " + P_NOTES;

   public Data(Context context)
   {
      super(context, DATABASE_NAME, null, 1);
   }

   @Override
   public void onCreate(SQLiteDatabase db)
   {
      String cols = A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + A_NAME + " TEXT, " + A_ORGANIZATORS + " TEXT, " + A_LOCATION + " TEXT, " + A_BEGIN
            + " TEXT, " + A_DUR_BLOCKS + " INTEGER, " + A_DUR_PRES + " INTEGER, " + A_WARNING + " INTEGER, " + // warning
                                                                                                               // before
                                                                                                               // set
                                                                                                               // seconds
            A_NOTES + " TEXT";

      db.execSQL("CREATE TABLE " + TABLE_ACTIONS + " (" + cols + ");");

      cols = B_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + B_ACTION + " INTEGER, " + B_POSITION + " INTEGER, " + B_TYPE + " INTEGER, " + B_BEGIN + " TEXT, "
            + B_DUR_BLOCKS + " INTEGER, " + P_NAME + " TEXT, " + P_AUTHORS + " TEXT, " + P_DUR_PRES + " INTEGER, " + P_POINTS + " INTEGER, " + P_NOTES + " TEXT";

      db.execSQL("CREATE TABLE " + TABLE_BLOCKS + " (" + cols + ");");

      cols = R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + R_NAME + " TEXT, " + R_DURATION + " INTEGER, "
      + R_SLIDES + " INTEGER, " + R_NOTES + " TEXT";

      db.execSQL("CREATE TABLE " + TABLE_RECORDS + " (" + cols + ");");
      
      cols = N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + N_RECORD + " INTEGER, "
      + N_SLIDE + " INTEGER, " + N_CONTENT + " TEXT";

      db.execSQL("CREATE TABLE " + TABLE_NOTES + " (" + cols + ");");
      
      
      //insertRecord(db, "nacvik 1", 5, 15, "poznamky");
      //insertRecord(db, "nacvik 2", 7, 25, "poznamky2");
      
      ContentValues cv = new ContentValues();

      // A_ID A_NAME A_ORGANIZATORS A_LOCATION A_BEGIN A_DUR_BLOCKS A_DUR_PRES
      // A_WARNING A_NOTES
      /*cv.put(A_NAME, "Obhajoba do IFJ");
      cv.put(A_ORGANIZATORS, "Matúš Vančo, Zuzana Vančová");
      cv.put(A_LOCATION, "VUT FIT, C229");
      cv.put(A_BEGIN, "2012-03-02 22:20");
      cv.put(A_DUR_BLOCKS, 25);
      cv.put(A_DUR_PRES, 10);
      cv.put(A_WARNING, 60);
      cv.put(A_NOTES, "Tuto budú ľubovoľné poznámky...");
      db.insert(TABLE_ACTIONS, A_NAME, cv);

      cv.put(A_NAME, "Obhajoby 1. projektu IZP");
      cv.put(A_ORGANIZATORS, "David Martinek");
      cv.put(A_LOCATION, "VUT FIT, M103");
      cv.put(A_BEGIN, "2012-06-05 23:20");
      cv.put(A_DUR_BLOCKS, 10);
      cv.put(A_DUR_PRES, 6);
      cv.put(A_WARNING, 120);
      cv.put(A_NOTES, "Téma projektu: jednoduchý parser štandardného vstupu");
      db.insert(TABLE_ACTIONS, A_NAME, cv);

      // B_ID B_ACTION B_POSITION B_BEGIN B_DUR_BLOCKS B_TYPE P_NAME P_AUTHORS
      // P_DUR_PRES P_NOTES
      cv.clear();
      cv.put(B_ACTION, 1);
      cv.put(B_POSITION, 0);
      cv.put(B_TYPE, TYPE_PRESENTATION);
      cv.put(P_NAME, "David Polesný");
      cv.put(P_AUTHORS, "David Polesný");
      cv.put(P_NOTES, "Ukázať aj nedokončené zadanie z cvík");
      db.insert(TABLE_BLOCKS, "name", cv);

      cv.put(B_ACTION, 1);
      cv.put(B_POSITION, 1);
      cv.put(B_TYPE, TYPE_PRESENTATION);
      cv.put(P_NAME, "Matej Chrenko");
      cv.put(P_AUTHORS, "Matej Chrenko");
      cv.put(P_NOTES, "Nepatrí oficiálne do cvík");
      db.insert(TABLE_BLOCKS, "name", cv);
      */
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
   {
      android.util.Log.w("Actions", "Upgrading database, which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS actions");
      onCreate(db);
   }

   public static Cursor getActionCursor(SQLiteDatabase db, int action)
   {

      // A_ID A_NAME A_ORGANIZATORS A_LOCATION A_BEGIN A_DUR_BLOCKS A_DUR_PRES
      // A_WARNING A_NOTES
      // Data.A_ID Data.A_NAME Data.A_ORGANIZATORS Data.A_LOCATION Data.A_BEGIN
      // Data.A_DUR_BLOCKS Data.A_DUR_PRES Data.A_WARNING Data.A_NOTES
      String cols = A_ID + ", " + A_NAME + ", " + A_ORGANIZATORS + ", " + A_LOCATION + ", " + A_BEGIN + ", " + A_DUR_BLOCKS + ", " + A_DUR_PRES + ", "
            + A_WARNING + ", " + A_NOTES;

      Cursor cursor = db.rawQuery("SELECT " + cols + " FROM " + TABLE_ACTIONS + "  WHERE " + A_ID + "=\"" + action + "\"", null);
      cursor.moveToFirst();
      return cursor;
   }

   public static Cursor getBlockCursor(SQLiteDatabase db, int presentation)
   {
      // B_ID B_ACTION B_POSITION B_BEGIN B_DUR_BLOCKS B_TYPE P_NAME P_AUTHORS
      // P_DUR_PRES P_NOTES
      String cols = B_ID + ", " + B_ACTION + ", " + B_POSITION + ", " + B_BEGIN + ", " + B_DUR_BLOCKS + ", " + B_TYPE + ", " + P_NAME + ", " + P_AUTHORS + ", "
            + P_DUR_PRES + ", " + P_POINTS + ", " + P_NOTES;

      Cursor cursor = db.rawQuery("SELECT " + cols + " FROM " + TABLE_BLOCKS + " WHERE " + B_ID + "=\"" + presentation + "\"", null);
      cursor.moveToFirst();
      return cursor;
   }

   public static String getString(Cursor cursor, String columnName)
   {
      int index = cursor.getColumnIndex(columnName);
      return cursor.getString(index);
   }

   public static int getInt(Cursor cursor, String columnName)
   {
      int index = cursor.getColumnIndex(columnName);
      return cursor.getInt(index);
   }

   public static boolean isNull(Cursor cursor, String columnName)
   {
      int index = cursor.getColumnIndex(columnName);
      return cursor.isNull(index);
   }

   public static Cursor getBlocksCursor(SQLiteDatabase db, int action)
   {
      String cols = Data.B_ID + ", " + Data.B_ACTION + ", " + Data.B_POSITION + ", " + Data.B_BEGIN + ", " + Data.B_DUR_BLOCKS + ", " + Data.B_TYPE + ", "
            + Data.P_NAME + ", " + Data.P_AUTHORS + ", " + Data.P_DUR_PRES + ", " + Data.P_NOTES;
      Cursor cursor = db.rawQuery("SELECT " + cols + " FROM " + Data.TABLE_BLOCKS + "  WHERE " + Data.B_ACTION + "=\"" + action + "\" ORDER BY "
            + Data.B_POSITION, null);
      cursor.moveToFirst();
      return cursor;
   }
   
   public String getNameRecord(int id) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor c = db.query(TABLE_RECORDS, new String[] {R_NAME}, "_id=?", new String[] {String.valueOf(id)}, null, null, null);
      
      String result;
      if (c.moveToFirst())
         result = getString(c, R_NAME);

      else
         result = null;

      c.close();
      
      return result;
   }
   
   public int getDurRecord(int id) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor c = db.query(TABLE_RECORDS, new String[] {R_DURATION}, "_id=?", new String[] {String.valueOf(id)}, null, null, null);
      
      int result;
      if (c.moveToFirst())
         result = getInt(c, R_DURATION);

      else
         result = INT_NULL;

      c.close();
      
      return result;
   }
   
   public int getSlidesRecord(int id) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor c = db.query(TABLE_RECORDS, new String[] {R_SLIDES}, "_id=?", new String[] {String.valueOf(id)}, null, null, null);
      
      int result;
      if (c.moveToFirst())
         result = getInt(c, R_SLIDES);

      else
         result = INT_NULL;

      c.close();
      
      return result;
   }
   
   public String getNotesRecord(int id) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor c = db.query(TABLE_RECORDS, new String[] {R_NOTES}, "_id=?", new String[] {String.valueOf(id)}, null, null, null);
      
      String result;
      if (c.moveToFirst())
         result = getString(c, R_NOTES);

      else
         result = null;

      c.close();
      
      return result;
   }
   
   public boolean insertRecord(String name, int dur, int slides, String notes) {
      SQLiteDatabase db = getWritableDatabase();
      
      ContentValues values = new ContentValues();
      
      //vráti -1 ked je niečo zle
      
      values.put(Data.R_NAME, name);
      values.put(Data.R_DURATION, dur);
      values.put(Data.R_SLIDES, slides);
      values.put(Data.R_NOTES, notes);

      boolean result = db.insert(TABLE_RECORDS, R_NAME, values) != -1;
      db.close();
      return result;
   }
   
   public boolean insertRecord(SQLiteDatabase db, String name, int dur, int slides, String notes) {
      ContentValues values = new ContentValues();
      
      //vráti -1 ked je niečo zle
      
      values.put(Data.R_NAME, name);
      values.put(Data.R_DURATION, dur);
      values.put(Data.R_SLIDES, slides);
      values.put(Data.R_NOTES, notes);

      return (db.insert(TABLE_RECORDS, R_NAME, values) != -1);
   }
   
   public boolean deleteRecord(int id) {
      SQLiteDatabase db = getWritableDatabase();
      String[] args = { String.valueOf(id) };

      boolean result = db.delete(TABLE_RECORDS, R_ID + "=?", args) == 1;
      db.close();
      return result;
   }
   
   public boolean updateRecord(int id, String name, int dur, int slides, String notes) {
      SQLiteDatabase db = getWritableDatabase();
      
      ContentValues values = new ContentValues();
      
      //
      if (name != null)
         values.put(Data.R_NAME, name);
      if (dur != -1)
         values.put(Data.R_DURATION, dur);
      if (slides != -1)
         values.put(Data.R_SLIDES, slides);
      if (notes != null)
         values.put(Data.R_NOTES, notes);
      
      boolean result = (db.update(Data.TABLE_RECORDS, values, "_id=?", new String[] {String.valueOf(id)}) == 1);
      
      db.close();
      return result;
   }
   
   public static String[] getRecordCols() {
      return new String[] {R_ID, R_NAME, R_DURATION, R_SLIDES, R_NOTES};  
   }
   
   public static String getRecordColsAsString() {
      return R_ID + ", " + R_NAME + ", " + R_DURATION + ", " + R_SLIDES + ", " + R_NOTES;
   }
   
   public Cursor getRecordCursor() {
      return getReadableDatabase().rawQuery("SELECT " + getRecordColsAsString() + " FROM " + Data.TABLE_RECORDS, null);
   }
   
   public boolean insertNote(int record, int slide, String content) {
      SQLiteDatabase db = getWritableDatabase();
      
      ContentValues values = new ContentValues();
      values.put(Data.N_CONTENT, content);
      
      boolean result;
      if (getNote(record, slide) != null) { //zaznam existuje
         result = db.update(Data.TABLE_NOTES, values, "record=? AND slide=?",
               new String[] {String.valueOf(record), String.valueOf(slide)}) == 1;
      }
      else {
         values.put(Data.N_RECORD, record);
         values.put(Data.N_SLIDE, slide);
         
         result = db.insert(TABLE_NOTES, N_RECORD, values) != -1;
      }
      db.close();
      return result;
   }
   
   public String getNote(int record, int slide) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor c = db.query(TABLE_NOTES, new String[] {N_CONTENT}, "record=? AND slide=?",
            new String[] {String.valueOf(record), String.valueOf(slide)}, null, null, null);
      
      String result;
      if (c.moveToFirst())
         result = getString(c, N_CONTENT);

      else
         result = null;

      c.close();
      
      return result;
   }
   
}