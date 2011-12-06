package ch.amana.android.cputuner.view.activity;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.ConfigurationAutoload;
import ch.amana.android.cputuner.service.ConfigurationAutoloadService;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class ConfigurationAutoloadListActivity extends ListActivity {

	private SimpleCursorAdapter adapter;
	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list);
		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setTitle(R.string.prefConfigurationsAutoLoad);
		actionBar.setHomeAction(new ActionBar.Action() {

			@Override
			public void performAction(View view) {
				onBackPressed();
			}

			@Override
			public int getDrawable() {
				return R.drawable.cputuner_back;
			}
		});

		Intent intent = new Intent(this, ConfigurationManageActivity.class);
		actionBar.addAction(new ActionBar.IntentAction(this, intent,
				android.R.drawable.ic_menu_manage));
		actionBar.addAction(new ActionBar.IntentAction(this, new Intent(Intent.ACTION_INSERT, DB.ConfigurationAutoload.CONTENT_URI), android.R.drawable.ic_menu_add));

		CursorLoader cursorLoader = new CursorLoader(this, DB.ConfigurationAutoload.CONTENT_URI, DB.ConfigurationAutoload.PROJECTION_DEFAULT, null, null,
				DB.ConfigurationAutoload.SORTORDER_DEFAULT);
		cursor = cursorLoader.loadInBackground();

		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, new String[] { DB.ConfigurationAutoload.NAME_HOUR,
				DB.ConfigurationAutoload.NAME_NEXT_EXEC }, new int[] { android.R.id.text1, android.R.id.text2 });

		getListView().setAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);

	}

	@Override
	protected void onPause() {
		cursor.deactivate();
		super.onPause();
	}

	@Override
	protected void onResume() {
		ConfigurationAutoloadService.scheduleNextEvent(this);
		cursor.requery();
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == DB.ConfigurationAutoload.INDEX_HOUR) {
					StringBuffer sb = new StringBuffer();
					appendDigit(sb, cursor.getInt(ConfigurationAutoload.INDEX_HOUR));
					sb.append(":");
					appendDigit(sb, cursor.getInt(ConfigurationAutoload.INDEX_MINUTE));
					sb.append("\t");
					sb.append(cursor.getString(ConfigurationAutoload.INDEX_CONFIGURATION));
					((TextView) view).setText(sb.toString());
					return true;
				} else if (columnIndex == DB.ConfigurationAutoload.INDEX_NEXT_EXEC) {
					long nextExecution = cursor.getLong(DB.ConfigurationAutoload.INDEX_NEXT_EXEC) - System.currentTimeMillis();
					nextExecution /= 60 * 1000;
					StringBuilder sb = new StringBuilder();
					ConfigurationAutoloadModel cam = new ConfigurationAutoloadModel(cursor);
					if (cam.isWeekday(Calendar.SUNDAY)) {
						sb.append(getString(R.string.day_sun)).append(" ");
					}
					if (cam.isWeekday(Calendar.MONDAY)) {
						sb.append(getString(R.string.day_mon)).append(" ");
					}
					if (cam.isWeekday(Calendar.TUESDAY)) {
						sb.append(getString(R.string.day_tue)).append(" ");
					}
					if (cam.isWeekday(Calendar.WEDNESDAY)) {
						sb.append(getString(R.string.day_wed)).append(" ");
					}
					if (cam.isWeekday(Calendar.THURSDAY)) {
						sb.append(getString(R.string.day_thu)).append(" ");
					}
					if (cam.isWeekday(Calendar.FRIDAY)) {
						sb.append(getString(R.string.day_fri)).append(" ");
					}
					if (cam.isWeekday(Calendar.SATURDAY)) {
						sb.append(getString(R.string.day_sat)).append(" ");
					}
					if (sb.length() > 0) {
						sb.append("\n");
					}
					if (sb.length() > 0) {
						sb.insert(0, " ");
						sb.insert(0, getString(R.string.label_weekdays));
					}
					sb.append("Next run: ");
					StringBuilder ne = new StringBuilder();
					ne.append(nextExecution % 60).append(" min");
					nextExecution = nextExecution / 60;
					if (nextExecution > 0) {
						ne.insert(0, " h ").insert(0, nextExecution % 24);
						nextExecution = nextExecution / 24;
						if (nextExecution > 0) {
							ne.insert(0, " d ").insert(0, nextExecution);
						}
					}
					sb.append(ne);
					((TextView) view).setText(sb.toString());
					return true;
				}
				return false;
			}

			private void appendDigit(StringBuffer sb, int i) {
				if (i < 10) {
					sb.append("0");
				}
				sb.append(i);
			}

		});
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.list_option, menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (handleCommonMenu(item)) {
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_SETTINGS_CONFIGURATION)) {
			return true;
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.ConfigurationAutoload.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	private boolean handleCommonMenu(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			startActivity(new Intent(Intent.ACTION_INSERT, DB.ConfigurationAutoload.CONTENT_URI));
			return true;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo
			menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.db_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Logger.e("bad menuInfo", e);
			return false;
		}

		final Uri uri = ContentUris.withAppendedId(DB.ConfigurationAutoload.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			delete(uri);
			return true;

		case R.id.menuItemEdit:
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			return true;

		case R.id.menuItemInsertAsNew:
			startActivity(new Intent(CpuTunerProvider.ACTION_INSERT_AS_NEW, uri));
			return true;

		default:
			return handleCommonMenu(item);
		}

	}

	private void delete(final Uri uri) {
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.menuItemDelete);
		alertBuilder.setMessage(R.string.msg_delete_selected_item);
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}
}
