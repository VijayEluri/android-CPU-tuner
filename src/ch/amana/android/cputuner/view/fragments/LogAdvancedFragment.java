package ch.amana.android.cputuner.view.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.SwitchLogDB;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;

import com.markupartist.android.widget.ActionBar.Action;

public class LogAdvancedFragment extends PagerFragment implements StateChangeListener {

	private Cursor displayCursor;
	private SimpleCursorTreeAdapter adapter;
	private final Date now = new Date();
	private ExpandableListView elvLog;

	private static final SimpleDateFormat logTimeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat logDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Activity act = getActivity();
		if (act == null) {
			return;
		}

		CursorLoader cursorLoader = new CursorLoader(act, DB.SwitchLogDB.CONTENT_URI, DB.SwitchLogDB.PROJECTION_DEFAULT, null, null, DB.SwitchLogDB.SORTORDER_DEFAULT);
		displayCursor = cursorLoader.loadInBackground();

		adapter = new SimpleCursorTreeAdapter(
				act,
				displayCursor,
				R.layout.log_adv_item_main,
				new String[] { DB.SwitchLogDB.NAME_TIME, DB.SwitchLogDB.NAME_MESSAGE, DB.SwitchLogDB.NAME_BATTERY },
				new int[] { R.id.tvTime, R.id.tvMsg, R.id.tvBattery },
				R.layout.log_adv_item_child,
				new String[] { DB.SwitchLogDB.NAME_MESSAGE, DB.SwitchLogDB.NAME_TRIGGER, DB.SwitchLogDB.NAME_PROFILE, DB.SwitchLogDB.NAME_VIRTGOV, DB.SwitchLogDB.NAME_BATTERY,
						DB.SwitchLogDB.NAME_LOCKED, DB.SwitchLogDB.NAME_TIME },
				new int[] { R.id.tvMessage, R.id.tvTrigger, R.id.tvProfile, R.id.tvGovernor, R.id.tvBatteryLevel, R.id.tvState, R.id.tvDateTimeDetail }) {

			@Override
			protected Cursor getChildrenCursor(Cursor groupCursor) {
				String id = Integer.toString(groupCursor.getInt(DB.INDEX_ID));

				CursorLoader cursorLoaderChild = new CursorLoader(act, DB.SwitchLogDB.CONTENT_URI, SwitchLogDB.PROJECTION_DEFAULT, DB.SELECTION_BY_ID,
						new String[] { id }, SwitchLogDB.SORTORDER_DEFAULT);
				Cursor c = cursorLoaderChild.loadInBackground();
				if (c.moveToFirst() && c.getString(SwitchLogDB.INDEX_TRIGGER) != null) {
					return c;
				}

				return null;
			}
		};

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == DB.SwitchLogDB.INDEX_MESSAGE && view.getId() == R.id.tvMsg) {
					String profile = cursor.getString(DB.SwitchLogDB.INDEX_PROFILE);
					if (profile != null) {
						((TextView) view).setText(profile);
						return true;
					}
				} else if (columnIndex == DB.SwitchLogDB.INDEX_TIME) {
					now.setTime(cursor.getLong(DB.SwitchLogDB.INDEX_TIME));
					String timeString;
					if (view.getId() == R.id.tvDateTimeDetail) {
						timeString = logDateTimeFormat.format(now);
					} else {
						timeString = logTimeFormat.format(now);
					}
					((TextView) view).setText(timeString);
					return true;
				} else if (columnIndex == DB.SwitchLogDB.INDEX_BATTERY) {
					int bat = cursor.getInt(DB.SwitchLogDB.INDEX_BATTERY);
					if (bat > -1) {
						((TextView) view).setText(bat + "%");
					} else {
						((TextView) view).setText("");
					}
					return true;
				} else if (columnIndex == DB.SwitchLogDB.INDEX_LOCKED) {
					StringBuilder sb = new StringBuilder();
					sb.append(getString(cursor.getInt(DB.SwitchLogDB.INDEX_LOCKED) == 0 ? R.string.screenOn : R.string.screenOff));
					sb.append(", ").append(getString(cursor.getInt(DB.SwitchLogDB.INDEX_AC) == 0 ? R.string.battery : R.string.ac_power));
					if (cursor.getInt(DB.SwitchLogDB.INDEX_CALL) != 0) {
						sb.append(", ").append(R.string.call_active);
					}
					if (cursor.getInt(DB.SwitchLogDB.INDEX_CALL) != 0) {
						sb.append(", ").append(R.string.battery_hot);
					}
					((TextView) view).setText(sb.toString());
					return true;
				}
				return false;
			}
		});

		elvLog.setAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.switch_log_adv, container, false);
		elvLog = (ExpandableListView) v.findViewById(R.id.elvLog);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		final Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
		requestUpdate();
	}

	@Override
	public void onPause() {

		Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).removeStateChangeListener(this);
		}
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.refresh_option, menu);
		inflater.inflate(R.menu.log_advstat_option, menu);
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			requestUpdate();
			return true;
		case R.id.itemMark:
			markLog(act);
			return true;
		case R.id.itemClear:
			act.getContentResolver().delete(DB.SwitchLogDB.CONTENT_URI, null, null);
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_PROFILE)) {
			return true;
		}
		return false;
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>(2);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				markLog(view.getContext());
			}

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_mark;
			}
		});
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				requestUpdate();
			}

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}
		});
		return actions;
	}

	private void markLog(Context context) {
		Intent i = new Intent(SwitchLog.ACTION_ADD_TO_LOG);
		i.putExtra(SwitchLog.EXTRA_LOG_ENTRY, context.getString(R.string.msgMarkLog));
		i.putExtra(SwitchLog.EXTRA_FLUSH_LOG, true);
		context.sendBroadcast(i);
	}

	@Override
	public void profileChanged() {
		requestUpdate();
	}

	@Override
	public void deviceStatusChanged() {
		requestUpdate();
	}

	@Override
	public void triggerChanged() {
		requestUpdate();
	}

	private void requestUpdate() {
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				FragmentActivity activity = getActivity();
				if (activity != null) {
					activity.sendBroadcast(new Intent(SwitchLog.ACTION_FLUSH_LOG));
				}
			}
		}, 1000);
	}
}
