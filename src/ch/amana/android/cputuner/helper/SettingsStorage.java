package ch.amana.android.cputuner.helper;

import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.application.CpuTunerApplication;
import ch.amana.android.cputuner.hw.GpsHandler;
import ch.amana.android.cputuner.hw.PowerProfiles.ServiceType;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.receiver.StatisticsReceiver;

public class SettingsStorage {

	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

	private static final String PREF_KEY_USER_LEVEL = "prefKeyUserLevel";
	private static final String PREF_KEY_USER_LEVEL_SET = "prefKeyUserLevelSet";
	public static final String NO_VALUE = "noValue";
	public static final String ENABLE_PROFILES = "prefKeyEnableProfiles";
	public static final String ENABLE_STATUSBAR_NOTI = "prefKeyStatusbarNotifications";
	public static final String PREF_KEY_ENABLE_STATISTICS_SERVICE = "prefKeyEnableStatisticsService";
	public static final String PREF_KEY_ENABLE_SWITCH_LOG = "prefKeyEnableSwitchLog";
	public static final String PREF_KEY_APPWIDGET_COUNT = "prefKeyAppwidgetCount";

	public static final int NO_BATTERY_HOT_TEMP = 5000;

	public static final int TRACK_CURRENT_AVG = 1;
	public static final int TRACK_CURRENT_CUR = 2;
	public static final int TRACK_CURRENT_HIDE = 3;
	public static final int TRACK_BATTERY_LEVEL = 4;

	public static final int MULTICORE_CODE_AUTO = 2;
	public static final int MULTICORE_CODE_ENABLE = 1;
	public static final int MULTICORE_CODE_DISABLE = 0;

	private static final String PREF_DEFAULT_PROFILES_VERSION = "prefKeyDefaultProfileVersion";
	private static final String PREF_KEY_USE_VIRTUAL_GOVS = "prefKeyUseVirtualGovernors";

	private static final String PREF_KEY_CONFIGURATION = "prefKeyConfiguration";

	public static final String PREF_KEY_MIN_FREQ = "prefKeyMinFreq";
	public static final String PREF_KEY_MAX_FREQ = "prefKeyMaxFreq";

	public static final String PREF_KEY_MIN_FREQ_DEFAULT = PREF_KEY_MIN_FREQ + "Default";
	public static final String PREF_KEY_MAX_FREQ_DEFAULT = PREF_KEY_MAX_FREQ + "Default";

	private static final String PREF_STORE_LOCAL = "local";

	public static final String PREF_KEY_FIRST_RUN = "prefKeyFirstRun";

	public static final String PREF_KEY_ADV_STATS = "prefKeyAdvStats";
	public static final String PREF_KEY_WIDGET = "prefKeyHasWidget";

	public static final String PREF_KEY_TIMEINSTATE_BASELINE = "prefKeyTimeinstateBaseline";

	public static final String PREF_KEY_TOTALTRANSITIONS_BASELINE = "prefKeyTotaltransitionsBaseline";
	public static final String PREF_KEY_SWITCH_CPU_SETTINGS = "prefKeySwitchCpuSetting";

	public static final int STATUSBAR_NEVER = 0;
	public static final int STATUSBAR_RUNNING = 1;
	public static final int STATUSBAR_ALWAYS = 2;

	public static final int APPWIDGET_OPENACTION_CHOOSEPROFILES = 1;
	public static final int APPWIDGET_OPENACTION_CPUTUNER = 2;

	private static SettingsStorage instance;
	private final Context context;
	private boolean checkedBluetooth = false;
	private boolean enableSwitchBluetooth;
	private boolean checkedGps = false;
	private boolean enableSwitchGps;
	private boolean checkedBeta = false;
	private boolean enableBeta;
	private boolean checkedProfiles = false;
	private boolean enableProfiles;
	private int trackCurrent = -1;
	private boolean checkedStatusbarNotifications = false;
	private boolean statusbarNotifications;
	private boolean allowManualServiceChanges;
	private boolean checkedAllowManualServiceChanges = false;
	private boolean checkUserLevel = false;
	private boolean checkedSwitchWifiOnConnectedNetwork = false;
	private boolean checkedSwitchProfileWhilePhoneNotIdle = false;
	private boolean checkBatteryHotTemp = false;
	int userLevel;
	private boolean switchWifiOnConnectedNetwork;
	private boolean switchProfileWhilePhoneNotIdle;
	private int batteryHotTemp;
	private boolean enableCallInProgress;
	private boolean checkedenableCallInProgress = false;
	private boolean checkedPulseDelayOn = false;
	private long pulseDelayOn;
	private boolean checkedPulseDelayOff = false;
	private long pulseDelayOff;
	private boolean checkedEnableUserspaceGovernor = false;
	private boolean enableUserspaceGovernor;
	private boolean checkedProfileSwitchLogSize = false;
	private int profileSwitchLogSize;
	private boolean checkedStatusbarAddTo = false;
	private int statusbarAddTo;
	private boolean checkedPowerStrongerThanScreenoff = false;
	private boolean loadedSwitchCpuSetting = false;
	private boolean enableSwitchCpuSetting = false;
	private boolean checkedEnableSwitchCpuSetting = false;
	private boolean enableEnableSwitchLog;
	private boolean checkedEnableSwitchLog = false;
	private boolean enableEnableStatistics;
	private boolean checkedEnableStatistics = false;
	private boolean checkedRunSwitchInBackground = false;
	private boolean runSwitchInBackground;

	private int uid = -1;

	private boolean powerStrongerThanScreenoff;

	private ProfileModel switchCpuSetting = ProfileModel.NO_PROFILE;

	public void forgetValues() {
		checkedBeta = false;
		checkedProfiles = false;
		trackCurrent = -1;
		checkedStatusbarNotifications = false;
		checkedAllowManualServiceChanges = false;
		checkUserLevel = false;
		checkedSwitchWifiOnConnectedNetwork = false;
		checkedSwitchProfileWhilePhoneNotIdle = false;
		checkBatteryHotTemp = false;
		checkedenableCallInProgress = false;
		checkedPulseDelayOn = false;
		checkedPulseDelayOff = false;
		checkedEnableUserspaceGovernor = false;
		checkedProfileSwitchLogSize = false;
		checkedStatusbarAddTo = false;
		checkedPowerStrongerThanScreenoff = false;
		checkedEnableSwitchCpuSetting = false;
		checkedEnableSwitchLog = false;
		checkedEnableStatistics = false;
		checkedRunSwitchInBackground = false;
	}

	public static SettingsStorage getInstance(Context ctx) {
		if (instance == null) {
			instance = new SettingsStorage(ctx.getApplicationContext());
		}
		return instance;
	}

	public static SettingsStorage getInstance() {
		return instance;
	}

	protected SettingsStorage(Context ctx) {
		super();
		context = ctx;
		if (getPreferences().contains("prefKeyPowerUser")) {
			Editor editor = getPreferences().edit();
			if (getPreferences().getBoolean("prefKeyPowerUser", false)) {
				editor.putString(PREF_KEY_USER_LEVEL, "3");
			} else {
				editor.putString(PREF_KEY_USER_LEVEL, "2");
			}
			editor.remove("prefKeyPowerUser");
			editor.commit();
		}
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	private SharedPreferences getLocalPreferences() {
		return context.getSharedPreferences(PREF_STORE_LOCAL, 0);
	}

	public void setEnableProfiles(boolean b) {
		enableProfiles = b;
		Editor editor = getPreferences().edit();
		editor.putBoolean(ENABLE_PROFILES, b);
		editor.commit();
		if (enableProfiles) {
			CpuTunerApplication.startCpuTuner(context);
		} else {
			CpuTunerApplication.stopCpuTuner(context);
		}
	}

	public boolean isEnableProfiles() {
		if (!checkedProfiles) {
			checkedProfiles = true;
			enableProfiles = getPreferences().getBoolean(ENABLE_PROFILES, false);
		}
		return enableProfiles;
	}

	public int isStatusbarAddto() {
		if (!checkedStatusbarAddTo) {
			checkedStatusbarAddTo = true;
			try {
				statusbarAddTo = Integer.parseInt(getPreferences().getString("prefKeyStatusbarAddToChoice", "1"));
			} catch (Exception e) {
				statusbarAddTo = 1;
			}
		}
		return statusbarAddTo;
	}

	public void setStatusbarAddto(int i) {
		statusbarAddTo = i;
		Editor editor = getPreferences().edit();
		editor.putString("prefKeyStatusbarAddToChoice", Integer.toString(i));
		editor.commit();
	}

	public boolean isStatusbarNotifications() {
		if (!checkedStatusbarNotifications) {
			checkedStatusbarNotifications = true;
			statusbarNotifications = getPreferences().getBoolean(ENABLE_STATUSBAR_NOTI, false);
		}
		return statusbarNotifications;
	}

	public boolean isEnableBeta() {
		if (!checkedBeta) {
			checkedBeta = true;
			enableBeta = "speedup".equals((getPreferences().getString("prefKeyEnableBeta", "").trim()));
		}
		return enableBeta;
	}

	public void setUserLevel(int level) {
		checkUserLevel = false;
		Editor editor = getPreferences().edit();
		editor.putString(PREF_KEY_USER_LEVEL, Integer.toString(level));
		editor.putBoolean(PREF_KEY_USER_LEVEL_SET, true);
		editor.commit();
	}

	public boolean isUserLevelSet() {
		return getPreferences().getBoolean(PREF_KEY_USER_LEVEL_SET, false);
	}

	public int getUserLevel() {
		if (!checkUserLevel) {
			checkUserLevel = true;
			try {
				userLevel = Integer.parseInt(getPreferences().getString(PREF_KEY_USER_LEVEL, "2"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse prefKeyUserLevel as int", e);
				userLevel = 2;
			}
		}
		return userLevel;
	}

	public int getTrackCurrentType() {
		if (trackCurrent < 0) {
			String trackCurrentStr = getPreferences().getString("prefKeyCalcPowerUsageType", "3");
			try {
				trackCurrent = Integer.parseInt(trackCurrentStr);
			} catch (Exception e) {
				Logger.w("Cannot parse prefKeyCalcPowerUsage as int", e);
				trackCurrent = 1;
			}
		}
		return trackCurrent;
	}

	public boolean isEnableSwitchMobiledataConnection() {
		return true;
	}

	public boolean isEnableSwitchMobiledata3G() {
		return true;
	}

	public boolean isEnableSwitchBackgroundSync() {
		return true;
	}

	public boolean isEnableSwitchBluetooth() {
		if (!checkedBluetooth) {
			checkedBluetooth = true;
			enableSwitchBluetooth = BluetoothAdapter.getDefaultAdapter() != null;
		}
		return enableSwitchBluetooth;
	}

	public boolean isEnableSwitchGps() {
		if (!checkedGps) {
			checkedGps = true;
			enableSwitchGps = GpsHandler.isEnableSwitchGps(context);
		}
		return enableSwitchGps;
	}

	public boolean isEnableSwitchWifi() {
		// FIXME check if wifi is present
		return true;
	}

	public String getCpuFreqs() {
		return getPreferences().getString("prefKeyCpuFreq", "");
	}

	public boolean isAllowManualServiceChanges() {
		if (!checkedAllowManualServiceChanges) {
			checkedAllowManualServiceChanges = true;
			allowManualServiceChanges = getPreferences().getBoolean("prefKeyAllowManualServiceChanges", false);
		}
		return allowManualServiceChanges;
	}

	public boolean isInstallAsSystemAppEnabled() {
		return RootHandler.isSystemApp(context) || (isEnableBeta() && isPowerUser());
	}

	public int getMinimumSensibeFrequency() {
		try {
			return Integer.parseInt(getPreferences().getString("prefKeyMinSensibleFrequency", "400"));
		} catch (NumberFormatException e) {
			Logger.w("Error parsing fot MinimumSensibeFrequency ", e);
			return 400;
		}
	}

	public boolean isBeginnerUser() {
		return getUserLevel() == 1;
	}

	public boolean isPowerUser() {
		return getUserLevel() > 2;
	}

	public boolean isSwitchWifiOnConnectedNetwork() {
		if (!checkedSwitchWifiOnConnectedNetwork) {
			checkedSwitchWifiOnConnectedNetwork = true;
			switchWifiOnConnectedNetwork = getPreferences().getBoolean("prefKeySwitchWifiOnConnectedNetwork", true);
		}
		return switchWifiOnConnectedNetwork;
	}

	public boolean isSwitchProfileWhilePhoneNotIdle() {
		if (!checkedSwitchProfileWhilePhoneNotIdle) {
			checkedSwitchProfileWhilePhoneNotIdle = true;
			switchProfileWhilePhoneNotIdle = getPreferences().getBoolean("prefKeySwitchProfileWhilePhoneNotIdle", false);
		}
		return switchProfileWhilePhoneNotIdle;
	}

	public int getBatteryHotTemp() {

		if (!checkBatteryHotTemp) {
			checkBatteryHotTemp = true;
			try {
				batteryHotTemp = Integer.parseInt(getPreferences().getString("prefKeyBatteryHotTemp", NO_BATTERY_HOT_TEMP + ""));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse prefKeyUserLevel as int", e);
				batteryHotTemp = NO_BATTERY_HOT_TEMP;
			}
		}
		return batteryHotTemp;
	}

	public int getDefaultProfilesVersion() {
		return getLocalPreferences().getInt(PREF_DEFAULT_PROFILES_VERSION, 0);
	}

	public void setDefaultProfilesVersion(int version) {
		Editor editor = getLocalPreferences().edit();
		editor.putInt(PREF_DEFAULT_PROFILES_VERSION, version);
		editor.commit();
	}

	public boolean isEnableCallInProgressProfile() {
		if (!checkedenableCallInProgress) {
			checkedenableCallInProgress = true;
			enableCallInProgress = getPreferences().getBoolean("prefKeyCallInProgressProfile", true);
		}
		return enableCallInProgress;
	}

	public long getPulseDelayOn() {

		if (!checkedPulseDelayOn) {
			checkedPulseDelayOn = true;
			try {
				pulseDelayOn = Long.parseLong(getPreferences().getString("prefKeyPulseDelayOn", "1"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse pulseDelayOn as int", e);
				pulseDelayOn = 1;
			}
		}
		return pulseDelayOn;
	}

	public long getPulseDelayOff() {

		if (!checkedPulseDelayOff) {
			checkedPulseDelayOff = true;
			try {
				pulseDelayOff = Long.parseLong(getPreferences().getString("prefKeyPulseDelayOff", "30"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse pulseDelayOn as int", e);
				pulseDelayOff = 1;
			}
		}
		return pulseDelayOff;
	}

	public boolean isEnableUserspaceGovernor() {
		if (!checkedEnableUserspaceGovernor) {
			checkedEnableUserspaceGovernor = true;
			enableUserspaceGovernor = getPreferences().getBoolean("prefKeyEnableUserspaceGovernor", false);
		}
		return enableUserspaceGovernor;
	}

	public boolean isEnableScriptOnProfileChange() {
		return isPowerUser();
	}

	public String getLanguage() {
		return getPreferences().getString("prefKeyLanguage", "");
	}

	public boolean isPulseMobiledataOnWifi() {
		return getPreferences().getBoolean("prefKeyPulseMobiledataOnWifi", true);
	}

	public boolean isUseVirtualGovernors() {
		return getPreferences().getBoolean(PREF_KEY_USE_VIRTUAL_GOVS, true);
	}

	public void setUseVirtualGovernors(boolean b) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(PREF_KEY_USE_VIRTUAL_GOVS, b);
		editor.commit();
	}

	public boolean isEnableAirplaneMode() {
		return true;
	}

	public boolean is24Hour() {
		// FIXME add to UI
		return true;
	}

	public int getProfileSwitchLogSize() {
		if (!checkedProfileSwitchLogSize) {
			checkedProfileSwitchLogSize = true;
			try {
				profileSwitchLogSize = Integer.parseInt(getPreferences().getString("prefKeyProfileSwitchLogSize", "10"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse prefKeyProfileSwitchLogSize as int", e);
				profileSwitchLogSize = 10;
			}
		}
		return profileSwitchLogSize;
	}

	public void setCurrentConfiguration(String configuration) {

		Editor edit = getPreferences().edit();
		edit.putString(PREF_KEY_CONFIGURATION, configuration);
		edit.commit();
	}

	public String getCurrentConfiguration() {
		return getPreferences().getString(PREF_KEY_CONFIGURATION, "");
	}

	public SimpleDateFormat getSimpledateformat() {
		return simpleDateFormat;
	}

	public boolean isSaveConfiguration() {
		return getPreferences().getBoolean("prefKeySaveConfigOnSwitch", true);
	}

	public boolean hasCurrentConfiguration() {
		String config = getCurrentConfiguration();
		return config != null && !config.trim().equals("");
	}

	public int isUseMulticoreCode() {
		try {
			return Integer.parseInt(getPreferences().getString("prefKeyMulticore", "2"));
		} catch (NumberFormatException e) {
			Logger.w("Cannot parse prefKeyMulticore as int", e);
			return 2;
		}
	}

	public int getNetworkStateOnWifi() {
		try {
			return Integer.parseInt(getPreferences().getString("prefKeyNetworkModeOnWifiConnected", "0"));
		} catch (NumberFormatException e) {
			Logger.w("Cannot parse prefKeyNetworkModeOnWifiConnected as int", e);
			return 0;
		}
	}

	public int getMinFrequencyDefault() {
		if (!isBeginnerUser()) {
			try {
				int ret = Integer.parseInt(getPreferences().getString(PREF_KEY_MIN_FREQ, "-1"));
				if (ret > 0) {
					return ret;
				}
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse PREF_KEY_MIN_FREQ as int", e);
			}
		}
		return getPreferences().getInt(PREF_KEY_MIN_FREQ_DEFAULT, -1);
	}

	public void setMinFrequencyDefault(int minCpuFreq) {
		Editor editor = getPreferences().edit();
		if ("".equals(getPreferences().getString(PREF_KEY_MIN_FREQ, ""))) {
			editor.putString(PREF_KEY_MIN_FREQ, Integer.toString(minCpuFreq));
		}
		editor.putInt(PREF_KEY_MIN_FREQ_DEFAULT, minCpuFreq);
		editor.commit();
	}

	public int getMaxFrequencyDefault() {
		if (!isBeginnerUser()) {
			try {
				int ret = Integer.parseInt(getPreferences().getString(PREF_KEY_MAX_FREQ, "-1"));
				if (ret > 0) {
					return ret;
				}
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse PREF_KEY_MAX_FREQ as int", e);
			}
		}
		return getPreferences().getInt(PREF_KEY_MAX_FREQ_DEFAULT, -1);
	}

	public void setMaxFrequencyDefault(int maxCpuFreq) {
		Editor editor = getPreferences().edit();
		if ("".equals(getPreferences().getString(PREF_KEY_MAX_FREQ, ""))) {
			editor.putString(PREF_KEY_MAX_FREQ, Integer.toString(maxCpuFreq));
		}
		editor.putInt(PREF_KEY_MAX_FREQ_DEFAULT, maxCpuFreq);
		editor.commit();
	}

	public void setEnableLogProfileSwitches(boolean b) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(PREF_KEY_ENABLE_SWITCH_LOG, b);
		editor.commit();
		if (b) {
			SwitchLog.start(context);
		} else {
			SwitchLog.stop(context);
		}
	}

	public boolean isEnableLogProfileSwitches() {
		if (!checkedEnableSwitchLog) {
			checkedEnableSwitchLog = true;
			enableEnableSwitchLog = getPreferences().getBoolean(PREF_KEY_ENABLE_SWITCH_LOG, true);
		}
		return enableEnableSwitchLog;
	}

	public boolean isFirstRun() {
		return getLocalPreferences().getBoolean(PREF_KEY_FIRST_RUN, true);
	}

	public void firstRunDone() {
		Editor editor = getLocalPreferences().edit();
		editor.putBoolean(PREF_KEY_FIRST_RUN, false);
		editor.commit();
	}

	public void setAdvancesStatistics(boolean b) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(PREF_KEY_ADV_STATS, b);
		editor.commit();
		if (b && isRunStatisticsService()) {
			StatisticsReceiver.register(context);
		} else {
			StatisticsReceiver.unregister(context);
		}
	}

	public boolean isAdvancesStatistics() {
		return getPreferences().getBoolean(PREF_KEY_ADV_STATS, false);
	}

	public void setHasWidget(boolean b) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(PREF_KEY_WIDGET, b);
		editor.commit();
	}

	public boolean hasWidget() {
		return getPreferences().getBoolean(PREF_KEY_WIDGET, false);
	}

	public void setTimeinstateBaseline(String timeInState) {
		Editor editor = getPreferences().edit();
		editor.putString(PREF_KEY_TIMEINSTATE_BASELINE, timeInState);
		editor.commit();
	}

	public String getTimeinstateBaseline() {
		return getPreferences().getString(PREF_KEY_TIMEINSTATE_BASELINE, "");
	}

	public void setTotaltransitionsBaseline(long tt) {
		Editor editor = getPreferences().edit();
		editor.putLong(PREF_KEY_TOTALTRANSITIONS_BASELINE, tt);
		editor.commit();
	}

	public long getTotaltransitionsBaseline() {
		return getPreferences().getLong(PREF_KEY_TOTALTRANSITIONS_BASELINE, 0);
	}

	public void migrateSettings() {
		if (getPreferences().contains("prefKeyStatusbarAddTo")) {
			Editor edit = getPreferences().edit();
			boolean add = getPreferences().getBoolean("prefKeyStatusbarAddTo", true);
			edit.putString("prefKeyStatusbarAddToChoice", Integer.toString(add ? STATUSBAR_RUNNING : STATUSBAR_NEVER));
			edit.remove("prefKeyStatusbarAddTo");
			edit.commit();
		}
	}

	public int getPulseInitalDelay() {
		try {
			return Integer.parseInt(getPreferences().getString("prefKeyInitialPulseDelay", "0"));
		} catch (NumberFormatException e) {
			Logger.w("Cannot parse prefKeyInitialPulseDelay as int", e);
			return 0;
		}
	}

	public boolean isPowerStrongerThanScreenoff() {
		if (!checkedPowerStrongerThanScreenoff) {
			checkedPowerStrongerThanScreenoff = true;
			powerStrongerThanScreenoff = getPreferences().getBoolean("prefKeyPowerStrongerThanScreenoff", true);
		}
		return powerStrongerThanScreenoff;
	}

	public boolean isLogPulse() {
		return getPreferences().getBoolean("prefKeyLogPulse", isAdvancesStatistics());
	}

	public boolean isEnableSwitchCpuSetting() {
		if (!checkedEnableSwitchCpuSetting) {
			checkedEnableSwitchCpuSetting = true;
			enableSwitchCpuSetting = getPreferences().getBoolean("prefKeyEnableSwitchCpuSetting", false);
		}
		return enableSwitchCpuSetting;
	}

	public ProfileModel getSwitchCpuSetting() {
		if (!loadedSwitchCpuSetting) {
			loadedSwitchCpuSetting = true;
			String json = getPreferences().getString(PREF_KEY_SWITCH_CPU_SETTINGS, null);
			if (json != null) {
				switchCpuSetting = new ProfileModel();
				try {
					switchCpuSetting.readFromJson(new JSONBundle(new JSONObject(json)));
				} catch (JSONException e) {
					Logger.w("Cannot real switch cpu settings from json string ", e);
				}
			}
		}
		return switchCpuSetting;
	}

	public void setSwitchCpuSetting(ProfileModel profile) {
		switchCpuSetting = new ProfileModel(profile);
		JSONObject json = new JSONObject();
		JSONBundle jsonBundle = new JSONBundle(json);
		profile.saveToJson(jsonBundle);
		Editor editor = getPreferences().edit();
		editor.putString(PREF_KEY_SWITCH_CPU_SETTINGS, json.toString());
		editor.commit();
	}

	public boolean hasHoloTheme() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public void setRunStatisticsService(boolean b) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(PREF_KEY_ENABLE_STATISTICS_SERVICE, b);
		editor.commit();
		enableEnableStatistics = b;
		if (b && isAdvancesStatistics()) {
			StatisticsReceiver.register(context);
		} else {
			StatisticsReceiver.unregister(context);
		}
	}

	public boolean isRunStatisticsService() {
		if (!checkedEnableStatistics) {
			checkedEnableStatistics = true;
			enableEnableStatistics = getPreferences().getBoolean(PREF_KEY_ENABLE_STATISTICS_SERVICE, false);
		}
		return enableEnableStatistics;
	}

	public int getAppwdigetOpenAction() {
		return APPWIDGET_OPENACTION_CHOOSEPROFILES;
	}

	public boolean isServiceEnabled(ServiceType type) {
		switch (type) {
		case wifi:
			return isEnableSwitchWifi();
		case bluetooth:
			return isEnableSwitchBluetooth();
		case mobiledataConnection:
			return isEnableSwitchMobiledataConnection();
		case backgroundsync:
			return isEnableSwitchBackgroundSync();
		case airplainMode:
			return isEnableAirplaneMode();
		case gps:
			return isEnableSwitchGps();
		case mobiledata3g:
			return isEnableSwitchMobiledata3G();
		default:
			return false;
		}
	}

	public boolean isShowWidgetIcon() {
		return getPreferences().getBoolean("prefKeyShowIcon", true);
	}

	public boolean isShowWidgetTrigger() {
		return getPreferences().getBoolean("prefKeyShowTrigger", true);
	}

	public boolean isShowWidgetProfile() {
		return getPreferences().getBoolean("prefKeyShowProfile", true);
	}

	public boolean isShowWidgetGovernor() {
		return getPreferences().getBoolean("prefKeyShowGovernor", false);
	}

	public boolean isShowWidgetBattery() {
		return getPreferences().getBoolean("prefKeyShowBattery", true);
	}

	public boolean isShowWidgetServices() {
		return getPreferences().getBoolean("prefKeyShowServices", true);
	}

	public String getVersionName() {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			Logger.i("Cannot get cpu tuner version", e);
		}
		return "";
	}

	public String getUnitSystem() {
		return getPreferences().getString("prefKeyUnitSystem", UnitsHelper.UNITS_DEFAULT);
	}

	public float getWidgetTextSize() {
		String s = getPreferences().getString("prefKeyWidgetTextSize", "3");
		// default is 3 medium
		float f = context.getResources().getDimension(R.dimen.widget_textsize_medium);
		if ("1".equals(s)) {
			f = context.getResources().getDimension(R.dimen.widget_textsize_tiny);
		} else if ("2".equals(s)) {
			f = context.getResources().getDimension(R.dimen.widget_textsize_small);
		} else if ("4".equals(s)) {
			f = context.getResources().getDimension(R.dimen.widget_textsize_big);
		}
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		f = f / metrics.scaledDensity;
		return f;
	}

	public boolean isRunSwitchInBackground() {
		if (!checkedRunSwitchInBackground) {
			checkedRunSwitchInBackground = true;
			runSwitchInBackground = getPreferences().getBoolean("prefKeyRunSwitchInBackground", true);
		}
		return runSwitchInBackground;
	}

	public boolean showWidgetLabels() {
		return getPreferences().getBoolean("prefKeyWidgetShowLabels", true);
	}

	public int getUserId() {
		if (uid < 0) {
			try {
				PackageManager pm = context.getPackageManager();
				String packageName = context.getPackageName();
				uid = pm.getApplicationInfo(packageName, 0).uid;
				//				int[] gids = pm.getPackageInfo(packageName, PackageManager.GET_GIDS).gids;
				//				if (gids.length > 0) {
				//					for (int i = 0; i < gids.length; i++) {
				//						if (gids[i] == uid) {
				//							gid = uid;
				//						}
				//					}
				//					if (gid < 0) {
				//						gid = gids[0];
				//					}
				//				} else {
				//					gid = uid;
				//				}
			} catch (NameNotFoundException e) {
				Logger.w("Cannot get UID", e);
			}
		}
		return uid;
	}
}
