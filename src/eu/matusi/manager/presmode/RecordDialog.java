package eu.matusi.manager.presmode;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import eu.matusi.manager.R;
import eu.matusi.manager.database.Data;

public class RecordDialog extends Activity
{
   Data db;
   
   public static String PARAM_ID = "identificator";
   
   private int record;
   
   EditText name;
   EditText duration;
   EditText slides;
   EditText notes;
  
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.pres_dialog);
      
      db = new Data(this);
     
      record = getIntent().getExtras().getInt(PARAM_ID);
      
      name = (EditText) findViewById(R.id.name);
      duration = (EditText) findViewById(R.id.duration);
      slides = (EditText) findViewById(R.id.slides);
      notes = (EditText) findViewById(R.id.notes);
      
      if (record != -1) {
         String nameStr = db.getNameRecord(record);
         name.setText(nameStr);
         duration.setText(String.valueOf(db.getDurRecord(record)));
         slides.setText(String.valueOf(db.getSlidesRecord(record)));
         notes.setText(db.getNotesRecord(record));
      }
   }
  
   
   public void onSubmit(View v)
   {
      String nameTmp = name.getText().toString().trim();
      String durationTmp = duration.getText().toString().trim();
      String slidesTmp = slides.getText().toString().trim();
      
      
      if (nameTmp.equals("") || durationTmp.equals("") || slidesTmp.equals("")) {
         Toast.makeText(this, "Musíte zadať povinné polia označené *", Toast.LENGTH_LONG).show();
      }
      else {
      
         try {
            int insDuration = Integer.parseInt(duration.getText().toString());
            int insSlides = Integer.parseInt(slides.getText().toString());
            
            if (record == -1)
               db.insertRecord(name.getText().toString(), insDuration, insSlides, notes.getText().toString());
            
            else
               db.updateRecord(record, name.getText().toString(), insDuration, insSlides, notes.getText().toString());
            
            setResult(RESULT_OK);
            finish();
         }
         catch (Exception ex) {
            //nemalo by nastat kedze je umoznene zadavat iba cisla
         }
         
      }
      

   }

   public void onCancel(View v)
   {
      setResult(RESULT_CANCELED);
      finish();
   }

}