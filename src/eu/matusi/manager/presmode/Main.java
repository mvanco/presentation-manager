package eu.matusi.manager.presmode;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import eu.matusi.manager.ActionDialog;
import eu.matusi.manager.EditPreferences;
import eu.matusi.manager.FileDialog;
import eu.matusi.manager.PresentationManager;
import eu.matusi.manager.R;
import eu.matusi.manager.SelectionMode;
import eu.matusi.manager.database.Data;

public class Main extends ListActivity {
   
   Data db;
   private final int ADD_RECORD = 0;
   private final int EDIT_RECORD = 1;
   private final int DELETE_RECORD = 2;
   CursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pres_main);
		
		db = new Data(this);
		
		int[] arrIds = { R.id.name, R.id.duration, R.id.slides };
		String[] colls = { Data.R_NAME, Data.R_DURATION, Data.R_SLIDES};
		adapter = new SimpleCursorAdapter(this, R.layout.pres_row, db.getRecordCursor(),
		      colls, arrIds);
      setListAdapter(adapter);
      
      registerForContextMenu(getListView());
	}
	
	@Override
	public void onResume() {
	   super.onResume();
	   adapter.changeCursor(db.getRecordCursor());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public void onMod1Click(View v) {
		Intent intent = new Intent(this, PresentationManager.class);
		startActivity(intent);
	}
	
	public void onAddRecordClick(View v) {
	   Intent intent = new Intent(this, RecordDialog.class);
	   intent.putExtra(RecordDialog.PARAM_ID, -1);
	   startActivityForResult(intent, ADD_RECORD );
	}
	
	@Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
      menu.add(Menu.NONE, EDIT_RECORD, Menu.NONE, "Upraviť");
      menu.add(Menu.NONE, DELETE_RECORD, Menu.NONE, "Vymazať");
   }

   @Override
   public boolean onContextItemSelected(MenuItem item) {
      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      switch (item.getItemId()) {

      case EDIT_RECORD:
         Intent intent = new Intent(this, RecordDialog.class);
         intent.putExtra(RecordDialog.PARAM_ID, (int) info.id);
         startActivityForResult(intent, EDIT_RECORD);
         return (true);
         
      case DELETE_RECORD:
         db.deleteRecord((int) info.id);
         adapter.changeCursor(db.getRecordCursor());
         return (true);
         
      default:
         return (super.onContextItemSelected(item));
         
      }
   }
	
   public void onListItemClick(ListView parent, View v, int position, long id) {
      Intent intent = new Intent(this, Timer.class);
      intent.putExtra(Timer.PARAM_ID, (int) id);
      startActivity(intent);
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(Menu.NONE, 1, Menu.NONE, "Nová prezentácia");
      menu.add(Menu.NONE, 2, Menu.NONE, "Usporiadať podľa názvu");
      menu.add(Menu.NONE, 3, Menu.NONE, "Usporiadať podľa času");
      menu.add(Menu.NONE, 4, Menu.NONE, "Nastavenia");
      return (super.onCreateOptionsMenu(menu));
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case 1:
         onAddRecordClick(null);
         return (true);

      case 2:

         return (true);

      case 3:

         return (true);

      case 4:
         startActivity(new Intent(this, EditPreferences.class));
         return (true);
      }
      return (super.onContextItemSelected(item));
   }
   
}