package eu.matusi.manager.dragndrop;

import eu.matusi.manager.R;
import eu.matusi.manager.database.Data;
import eu.matusi.manager.help.Help;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DragNDropListActivity extends ListActivity {

	private int action;
	private SQLiteDatabase db = null;
	ListView listView;
	
	public static final String PARAM_ACTION = "action";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dragndroplistview);

		db = (new Data(this)).getWritableDatabase();

		action = getIntent().getExtras().getInt(PARAM_ACTION);
		
		Cursor blocksCursor = Data.getBlocksCursor(db, action);

		ArrayList<String> content = new ArrayList<String>();
		String item;
		int order = 0;
		while (!blocksCursor.isAfterLast()) {
			if (Data.getInt(blocksCursor, Data.B_TYPE) == Data.TYPE_BREAK) {
				content.add("<" + Data.getInt(blocksCursor, Data.B_DUR_BLOCKS) + " min. prestávka>");
			} else {
				content.add(Data.getString(blocksCursor, Data.P_NAME));
			}
			blocksCursor.moveToNext();
		}

		setListAdapter(new DragNDropAdapter(this,
				new int[] { R.layout.dragitem },
				new int[] { R.id.draggable_textview }, content));// new
																	// DragNDropAdapter(this,content)
		listView = getListView();

		if (listView instanceof DragNDropListView) {
			((DragNDropListView) listView).setDropListener(mDropListener);
			((DragNDropListView) listView).setRemoveListener(mRemoveListener);
			((DragNDropListView) listView).setDragListener(mDragListener);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		db.close();
	}

	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdapter) {
				((DragNDropAdapter) adapter).onDrop(from, to);
				getListView().invalidateViews();
			}
		}
	};

	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdapter) {
				((DragNDropAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = 0xe0103010;
		int defaultBackgroundColor;

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.ImageView01);
			if (iv != null)
				iv.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.ImageView01);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);
		}

	};

	public void onSubmit(View v) {
		TextView tv;
		
		String[] args = { String.valueOf(action), String.valueOf(Data.TYPE_BREAK) };
		db.delete(Data.TABLE_BLOCKS, Data.B_ACTION + "=? AND " + Data.B_TYPE + "=?", args);
		
		String name;
		
		for (int i = 0; i < listView.getChildCount(); i++) {
			tv = (TextView) listView.getChildAt(i).findViewById(R.id.draggable_textview);
			
			ContentValues values = new ContentValues();
			name = tv.getText().toString();
			
			if (name.matches("<.*>")) {
				values.put(Data.P_NAME, "Prestávka");
				values.put(Data.B_ACTION, action);
				String durationStr = name.split(" ")[0].substring(1);
				String dur = Help.durationToString(Integer.parseInt(durationStr));
				values.put(Data.B_DUR_BLOCKS, dur);
				values.put(Data.B_POSITION, i);
				System.out.println("vkladam prestavku s sequence_id " + i + " a trvanim " + dur);
				values.put(Data.B_BEGIN, " ");
				values.put(Data.B_TYPE, Data.TYPE_BREAK);

				db.insert(Data.TABLE_BLOCKS, Data.B_ID, values);
				setResult(RESULT_OK);
				finish();
			}
			else {
				String[] params = { String.valueOf(action), name };
				values.put(Data.B_POSITION, i);
				values.put(Data.B_TYPE, Data.TYPE_PRESENTATION);
				db.update(Data.TABLE_BLOCKS, values, Data.B_ACTION + "=? AND " + Data.P_NAME + "=?", params);	
				
				//System.out.println("updatujem prezentaciu s menom " + name + " na sequence_id " + i);
			}
		}
		
		setResult(RESULT_OK);
		finish();
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
}