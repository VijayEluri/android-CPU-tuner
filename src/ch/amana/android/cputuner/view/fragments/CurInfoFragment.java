package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CpuFrequencyChooser;
import ch.amana.android.cputuner.helper.CpuFrequencyChooser.FrequencyChangeCallback;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper.GovernorConfig;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.CpuHandlerMulticore;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.IGovernorModel;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;
import ch.amana.android.cputuner.service.TunerService;
import ch.amana.android.cputuner.view.activity.ConfigurationManageActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.ProfileAdaper;
import ch.amana.android.cputuner.view.widget.SpinnerWrapper;

import com.markupartist.android.widget.ActionBar.Action;

public class CurInfoFragment extends PagerFragment implements GovernorFragmentCallback, FrequencyChangeCallback, StateChangeListener {

	private CpuHandler cpuHandler;
	private SeekBar sbCpuFreqMax;
	private Spinner spCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private Spinner spCpuFreqMin;
	private TextView tvBatteryLevel;
	private TextView tvAcPower;
	private TextView tvCurrentTrigger;
	private TextView labelCpuFreqMax;
	private TextView tvBatteryCurrent;
	private PowerProfiles powerProfiles;
	private SpinnerWrapper spProfiles;
	private GovernorBaseFragment governorFragment;
	private GovernorHelperCurInfo governorHelper;
	private TextView tvPulse;
	private TableRow trPulse;
	private TableRow trMaxFreq;
	private TableRow trMinFreq;
	private TableRow trBatteryCurrent;
	private TableRow trConfig;
	private TextView tvConfig;
	private CpuFrequencyChooser cpuFrequencyChooser;
	private TableRow trBattery;
	private TableRow trPower;
	private ProfileAdaper profileAdapter;
	private TextView tvManualServiceChanges;

	private class GovernorHelperCurInfo implements IGovernorModel {

		@Override
		public int getGovernorThresholdUp() {
			return cpuHandler.getGovThresholdUp();
		}

		@Override
		public int getGovernorThresholdDown() {
			return cpuHandler.getGovThresholdDown();
		}

		@Override
		public void setGov(String gov) {
			cpuHandler.setCurGov(gov);
		}

		@Override
		public void setGovernorThresholdUp(String string) {
			try {
				setGovernorThresholdUp(Integer.parseInt(string));
			} catch (Exception e) {
				Logger.w("Cannot parse " + string + " as int");
			}
		}

		@Override
		public void setGovernorThresholdDown(String string) {
			try {
				setGovernorThresholdDown(Integer.parseInt(string));
			} catch (Exception e) {
				Logger.w("Cannot parse " + string + " as int");
			}
		}

		@Override
		public void setScript(String string) {
			// not used

		}

		@Override
		public String getGov() {
			return cpuHandler.getCurCpuGov();
		}

		@Override
		public String getScript() {
			// not used
			return "";
		}

		@Override
		public void setGovernorThresholdUp(int i) {
			cpuHandler.setGovThresholdUp(i);
		}

		@Override
		public void setGovernorThresholdDown(int i) {
			cpuHandler.setGovThresholdDown(i);
		}

		@Override
		public void setVirtualGovernor(long id) {
			FragmentActivity activity = getActivity();
			if (activity == null) {
				return;
			}
			Cursor c = null;
			try {
				c = activity.getContentResolver().query(VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, new String[] { id + "" },
						VirtualGovernor.SORTORDER_DEFAULT);
				if (c.moveToFirst()) {
					VirtualGovernorModel vgm = new VirtualGovernorModel(c);
					cpuHandler.applyGovernorSettings(vgm);
					powerProfiles.getCurrentProfile().setVirtualGovernor(id);
				}
			} finally {
				if (c != null) {
					c.close();
					c = null;
				}
			}
		}

		@Override
		public long getVirtualGovernor() {
			if (powerProfiles == null || powerProfiles.getCurrentProfile() == null) {
				return -1;
			}
			return powerProfiles.getCurrentProfile().getVirtualGovernor();
		}

		@Override
		public void setPowersaveBias(int powersaveBias) {
			cpuHandler.setPowersaveBias(powersaveBias);
		}

		@Override
		public int getPowersaveBias() {
			return cpuHandler.getPowersaveBias();
		}

		@Override
		public boolean hasScript() {
			return false;
		}

		@Override
		public void setUseNumberOfCpus(int position) {
			cpuHandler.setNumberOfActiveCpus(position);
		}

		@Override
		public int getUseNumberOfCpus() {
			return cpuHandler.getNumberOfActiveCpus();
		}

		@Override
		public CharSequence getDescription(Context ctx) {
			if (ctx == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(ctx.getString(R.string.labelGovernor)).append(" ").append(getGov());
			int governorThresholdUp = getGovernorThresholdUp();
			if (governorThresholdUp > 0) {
				sb.append("\n").append(ctx.getString(R.string.labelThreshsUp)).append(" ").append(governorThresholdUp);
			}
			int governorThresholdDown = getGovernorThresholdDown();
			if (governorThresholdDown > 0) {
				sb.append(" ").append(ctx.getString(R.string.labelDown)).append(" ").append(governorThresholdDown);
			}
			if (cpuHandler instanceof CpuHandlerMulticore) {
				int useNumberOfCpus = getUseNumberOfCpus();
				int numberOfCpus = cpuHandler.getNumberOfCpus();
				if (useNumberOfCpus < 1 || useNumberOfCpus > numberOfCpus) {
					useNumberOfCpus = numberOfCpus;
				}
				sb.append("\n").append(ctx.getString(R.string.labelActiveCpus)).append(" ").append(useNumberOfCpus);
				sb.append("/").append(numberOfCpus);
			}
			return sb.toString();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.cur_info, container, false);

		tvCurrentTrigger = (TextView) v.findViewById(R.id.tvCurrentTrigger);
		spProfiles = new SpinnerWrapper((Spinner) v.findViewById(R.id.spProfiles));
		tvBatteryLevel = (TextView) v.findViewById(R.id.tvBatteryLevel);
		tvAcPower = (TextView) v.findViewById(R.id.tvAcPower);
		tvBatteryCurrent = (TextView) v.findViewById(R.id.tvBatteryCurrent);
		tvBatteryLevel = (TextView) v.findViewById(R.id.tvBatteryLevel);
		spCpuFreqMax = (Spinner) v.findViewById(R.id.spCpuFreqMax);
		spCpuFreqMin = (Spinner) v.findViewById(R.id.spCpuFreqMin);
		labelCpuFreqMax = (TextView) v.findViewById(R.id.labelCpuFreqMax);
		sbCpuFreqMax = (SeekBar) v.findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) v.findViewById(R.id.SeekBarCpuFreqMin);
		trPulse = (TableRow) v.findViewById(R.id.TableRowPulse);
		tvPulse = (TextView) v.findViewById(R.id.tvPulse);
		trMaxFreq = (TableRow) v.findViewById(R.id.TableRowMaxFreq);
		trMinFreq = (TableRow) v.findViewById(R.id.TableRowMinFreq);
		trBatteryCurrent = (TableRow) v.findViewById(R.id.TableRowBatteryCurrent);
		trConfig = (TableRow) v.findViewById(R.id.TableRowConfig);
		tvConfig = (TextView) v.findViewById(R.id.tvConfig);
		trBattery = (TableRow) v.findViewById(R.id.TableRowBattery);
		trPower = (TableRow) v.findViewById(R.id.TableRowPower);
		tvManualServiceChanges = (TextView) v.findViewById(R.id.tvManualServiceChanges);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();
		cpuHandler = CpuHandler.getInstance();
		powerProfiles = PowerProfiles.getInstance(getActivity());

		cpuFrequencyChooser = new CpuFrequencyChooser(this, sbCpuFreqMin, spCpuFreqMin, sbCpuFreqMax, spCpuFreqMax);

		governorHelper = new GovernorHelperCurInfo();
		SettingsStorage settings = SettingsStorage.getInstance(getContext());
		if (settings.isUseVirtualGovernors() && settings.isEnableProfiles()) {
			governorFragment = new VirtualGovernorFragment(this, governorHelper);
		} else {
			governorFragment = new GovernorFragment(this, governorHelper, true);
		}
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.llGovernorFragmentAncor, governorFragment);
		fragmentTransaction.commit();

		CursorLoader cursorLoader = new CursorLoader(act, DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
		Cursor cursor = cursorLoader.loadInBackground();

		profileAdapter = new ProfileAdaper(act, cursor);
		spProfiles.setAdapter(profileAdapter);

		spProfiles.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, final int pos, final long id) {
				if (id == PowerProfiles.AUTOMATIC_PROFILE && !SettingsStorage.getInstance().isEnableProfiles()) {
					return;
				}
				Intent i = new Intent(TunerService.ACTION_TUNERSERVICE_MANUAL_PROFILE);
				i.putExtra(TunerService.EXTRA_IS_MANUAL_PROFILE, id != PowerProfiles.AUTOMATIC_PROFILE);
				i.putExtra(TunerService.EXTRA_PROFILE_ID, id);
				act.startService(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		//		updateProfileSpinner();
		OnClickListener startBattery = new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent i = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
					startActivity(i);
				} catch (Throwable e) {
					// 'old' -> fallback
					try {
						Intent i = new Intent();
						i.setClassName("com.android.settings", "com.android.settings.fuelgauge.PowerUsageSummary");
						startActivity(i);
					} catch (Throwable e1) {
					}
				}

			}
		};
		trBattery.setOnClickListener(startBattery);
		trBatteryCurrent.setOnClickListener(startBattery);
		trPower.setOnClickListener(startBattery);

		trConfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context ctx = getActivity();
				Intent intent = new Intent(ctx, ConfigurationManageActivity.class);
				intent.putExtra(ConfigurationManageActivity.EXTRA_CLOSE_ON_LOAD, true);
				ctx.startActivity(intent);
			}
		});

		tvManualServiceChanges.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Builder alertBuilder = new AlertDialog.Builder(act);
				alertBuilder.setTitle(R.string.title_reset_manual_service_switches);
				alertBuilder.setMessage(R.string.msg_reset_manual_service_switches);
				alertBuilder.setNegativeButton(android.R.string.no, null);
				alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						powerProfiles.initActiveStates();
						updateView();
					}
				});
				AlertDialog alert = alertBuilder.create();
				alert.show();
			}
		});

		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
	}

	@Override
	public void onDestroy() {
		Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() != null) {
			governorFragment.updateVirtGov(true);
			updateView();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (governorFragment != null) {
			governorFragment.updateVirtGov(false);
		}
	}

	@Override
	public void updateView() {
		deviceStatusChanged();
		profileChanged();
		triggerChanged();
	}

	@Override
	public void deviceStatusChanged() {
		if (tvAcPower == null) {
			return;
		}
		tvAcPower.setText(getText(powerProfiles.isAcPower() ? R.string.yes : R.string.no));
		StringBuilder bat = new StringBuilder();
		bat.append(powerProfiles.getBatteryLevel()).append("%");
		bat.append(" (");
		if (powerProfiles.isBatteryHot()) {
			bat.append(getString(R.string.label_hot)).append(" ");
		}
		bat.append(powerProfiles.getBatteryTemperature()).append(" °C)");
		tvBatteryLevel.setText(bat.toString());
		StringBuilder currentText = new StringBuilder();
		BatteryHandler batteryHandler = BatteryHandler.getInstance();
		int currentNow = batteryHandler.getBatteryCurrentNow();
		if (currentNow > 0) {
			currentText.append(currentNow).append(" mA/h");
		}
		if (batteryHandler.hasAvgCurrent()) {
			int currentAvg = batteryHandler.getBatteryCurrentAverage();
			if (currentAvg > 0) {
				if (currentText.length() > 0) {
					currentText.append("; ");
				}
				currentText.append(getString(R.string.label_avgerage)).append(" ").append(currentAvg).append(" mA/h");
			}
		}
		if (currentText.length() > 0) {
			trBatteryCurrent.setVisibility(View.VISIBLE);
			tvBatteryCurrent.setText(currentText.toString());
		} else {
			trBatteryCurrent.setVisibility(View.GONE);
		}
	}

	@Override
	public void triggerChanged() {
		profileChanged();
	};

	@Override
	public void profileChanged() {
		if (tvPulse == null) {
			return;
		}
		final Activity act = getActivity();
		SettingsStorage settings = SettingsStorage.getInstance();
		if (PulseHelper.getInstance(act).isPulsing()) {
			trPulse.setVisibility(View.VISIBLE);
			int res = PulseHelper.getInstance(act).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
			tvPulse.setText(res);
		} else {
			trPulse.setVisibility(View.GONE);
		}
		if (settings.hasCurrentConfiguration()) {
			trConfig.setVisibility(View.VISIBLE);
			tvConfig.setText(settings.getCurrentConfiguration());
		} else {
			trConfig.setVisibility(View.GONE);
		}
		if (settings.isEnableProfiles()) {
			updateProfileSpinner();
			tvCurrentTrigger.setText(powerProfiles.getCurrentTriggerName());
			tvCurrentTrigger.setTextColor(Color.LTGRAY);
		} else {
			tvCurrentTrigger.setText(R.string.notEnabled);
			tvCurrentTrigger.setTextColor(Color.RED);
		}
		if (powerProfiles.hasManualServicesChanges()) {
			tvManualServiceChanges.setVisibility(View.VISIBLE);
		} else {
			tvManualServiceChanges.setVisibility(View.GONE);
		}

		cpuFrequencyChooser.setMinCpuFreq(cpuHandler.getMinCpuFreq());
		cpuFrequencyChooser.setMaxCpuFreq(cpuHandler.getMaxCpuFreq());

		GovernorConfig governorConfig = GovernorConfigHelper.getGovernorConfig(cpuHandler.getCurCpuGov());
		if (governorConfig.hasNewLabelCpuFreqMax()) {
			labelCpuFreqMax.setText(governorConfig.getNewLabelCpuFreqMax(act));
		} else {
			labelCpuFreqMax.setText(R.string.labelMax);
		}
		if (governorConfig.hasMinFrequency()) {
			trMinFreq.setVisibility(View.VISIBLE);
		} else {
			trMinFreq.setVisibility(View.GONE);
		}
		if (governorConfig.hasMaxFrequency()) {
			trMaxFreq.setVisibility(View.VISIBLE);
		} else {
			trMaxFreq.setVisibility(View.GONE);
		}

		governorFragment.updateView();
	}

	private void updateProfileSpinner() {
		ProfileModel currentProfile = powerProfiles.getCurrentProfile();
		if (currentProfile != PowerProfiles.DUMMY_PROFILE) {
			if (powerProfiles.isManualProfile()) {
				spProfiles.setSelectionDbId(currentProfile.getDbId());
			} else {
				spProfiles.setAdapter(profileAdapter);
				spProfiles.setSelection(0);
			}
		}
	}

	@Override
	public void updateModel() {
		// not used
	}

	@Override
	public void setMaxCpuFreq(int val) {
		if (val != cpuHandler.getMaxCpuFreq()) {
			if (cpuHandler.setMaxCpuFreq(val)) {
				Toast.makeText(getContext(), getString(R.string.msg_setting_cpu_max_freq, val), Toast.LENGTH_LONG).show();
			}
			updateView();
		}
	}

	@Override
	public void setMinCpuFreq(int val) {
		if (val != cpuHandler.getMinCpuFreq()) {
			if (cpuHandler.setMinCpuFreq(val)) {
				Toast.makeText(getContext(), getString(R.string.setting_cpu_min_freq, val), Toast.LENGTH_LONG).show();
			}
			updateView();
		}
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>(2);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				updateView();
			}

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}
		});
		return actions;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.refresh_option, menu);
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		switch (item.getItemId()) {

		case R.id.itemRefresh:
			updateView();
			return true;

		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_INDEX)) {
			return true;
		}
		return false;
	}
}
