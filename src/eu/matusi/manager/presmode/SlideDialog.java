package eu.matusi.manager.presmode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import eu.matusi.manager.R;

public class SlideDialog extends Activity
{
   public static String PARAM_SLIDE = "slide";
   public static String RETURN = "return";
   
   EditText slideEdit;
  
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.pres_slide);
     
      int slide = getIntent().getExtras().getInt(PARAM_SLIDE);
      
      slideEdit = (EditText) findViewById(R.id.slide);
      
      slideEdit.setText(String.valueOf(slide));
      
      LayoutParams params = getWindow().getAttributes(); 
      params.height = LayoutParams.WRAP_CONTENT;
      params.width  = LayoutParams.FILL_PARENT;
      getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
      
   }
   
   public void onSubmit(View v)
   {      
      Intent resultData = new Intent();
      resultData.putExtra(RETURN, Integer.parseInt(slideEdit.getText().toString()));
      setResult(RESULT_OK, resultData);
      finish();
   }

   public void onCancel(View v)
   {
      setResult(RESULT_CANCELED);
      finish();
   }

}