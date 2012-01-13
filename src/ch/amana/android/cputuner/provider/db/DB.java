package ch.amana.android.cputuner.provider.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.CpuTunerProvider;

public interface DB {

	public static final String SQL_WILDCARD = "%";
	public static final String DATABASE_NAME = "cputuner";

	public static final String NAME_ID = "_id";
	public static final int INDEX_ID = 0;

	public static final String[] PROJECTION_ID = new String[] { NAME_ID };
	public static final String SELECTION_BY_ID = NAME_ID + "=?";

	public class CpuTunerOpenHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 16;

		private static final String CREATE_TRIGGERS_TABLE = "create table if not exists " + Trigger.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
				+ DB.Trigger.NAME_TRIGGER_NAME + " text, " + DB.Trigger.NAME_BATTERY_LEVEL + " int," + DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_BATTERY_PROFILE_ID + " long," + DB.Trigger.NAME_POWER_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_POW + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_POW + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_BAT + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_BAT + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_LCK + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_LCK + " long,"
				+ DB.Trigger.NAME_HOT_PROFILE_ID + " long default -1," + DB.Trigger.NAME_POWER_CURRENT_SUM_HOT + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_CNT_HOT + " long," + DB.Trigger.NAME_CALL_IN_PROGRESS_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_CALL + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_CALL + " long)";

		private static final String CREATE_CPUPROFILES_TABLE = "create table if not exists " + CpuProfile.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, "
				+ DB.CpuProfile.NAME_PROFILE_NAME + " text, " + DB.CpuProfile.NAME_GOVERNOR + " text," + DB.CpuProfile.NAME_FREQUENCY_MAX + " int,"
				+ DB.CpuProfile.NAME_FREQUENCY_MIN + " int," + DB.CpuProfile.NAME_WIFI_STATE + " int," + DB.CpuProfile.NAME_GPS_STATE + " int,"
				+ DB.CpuProfile.NAME_BLUETOOTH_STATE + " int," + DB.CpuProfile.NAME_MOBILEDATA_3G_STATE + " int,"
				+ DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + " int DEFAULT 0,"
				+ DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN + " int DEFAULT 0,"
				+ DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE + " int, " + DB.CpuProfile.NAME_VIRTUAL_GOVERNOR
				+ " int default -1," + DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE + " int, " + DB.CpuProfile.NAME_SCRIPT + " text, " + DB.CpuProfile.NAME_POWERSEAVE_BIAS
				+ " int," + DB.CpuProfile.NAME_AIRPLANEMODE_STATE + " int," + DB.CpuProfile.NAME_USE_NUMBER_OF_CPUS + " int)";

		private static final String CREATE_VIRTUAL_GOVERNOR_TABLE = "create table if not exists " + VirtualGovernor.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, "
				+ DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME + " text, " + DB.VirtualGovernor.NAME_REAL_GOVERNOR + " text,"
				+ DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP
				+ " int DEFAULT 98," + DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN + " int DEFAULT 95, " + DB.VirtualGovernor.NAME_SCRIPT + " text, "
				+ DB.VirtualGovernor.NAME_POWERSEAVE_BIAS + " int, " + DB.CpuProfile.NAME_USE_NUMBER_OF_CPUS + " int)";

		private static final String CREATE_CONFIGURATION_AUTOLOAD_TABLE = "create table if not exists " + ConfigurationAutoload.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, "
				+ DB.ConfigurationAutoload.NAME_HOUR + " int, " + DB.ConfigurationAutoload.NAME_MINUTE + " int, "
				+ DB.ConfigurationAutoload.NAME_WEEKDAY + " int, " + DB.ConfigurationAutoload.NAME_CONFIGURATION + " text, " + DB.ConfigurationAutoload.NAME_NEXT_EXEC + " long, "
				+ DB.ConfigurationAutoload.NAME_EXACT_SCEDULING + " int DEFAULT 0)";

		private static final String CREATE_SWITCH_LOG_TABLE = "create table if not exists " + SwitchLogDB.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, " + DB.SwitchLogDB.NAME_TIME + " long, " + DB.SwitchLogDB.NAME_MESSAGE + " text, "
				+ DB.SwitchLogDB.NAME_TRIGGER + " text DEFAULT NULL, " + DB.SwitchLogDB.NAME_PROFILE + " text DEFAULT NULL, " + DB.SwitchLogDB.NAME_VIRTGOV
				+ " text DEFAULT NULL, "
				+ DB.SwitchLogDB.NAME_BATTERY + " int DEFAULT -1, " + DB.SwitchLogDB.NAME_LOCKED + " int DEFAULT -1, " + DB.SwitchLogDB.NAME_AC + " int DEFAULT -1, "
				+ DB.SwitchLogDB.NAME_CALL + " int DEFAULT -1, "
				+ DB.SwitchLogDB.NAME_HOT + " int DEFAULT -1)";

		private static final String CREATE_TIS_INDEX_TABLE = "create table if not exists " + TimeInStateIndex.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, " + DB.TimeInStateIndex.NAME_TIME + " long, "
				+ DB.TimeInStateIndex.NAME_TRIGGER + " text DEFAULT NULL, " + DB.TimeInStateIndex.NAME_PROFILE + " text DEFAULT NULL, " + DB.TimeInStateIndex.NAME_VIRTGOV
				+ " text DEFAULT NULL)";

		private static final String CREATE_TIS_VALUE_TABLE = "create table if not exists " + TimeInStateValue.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, " + DB.TimeInStateValue.NAME_IDX + " long, " + DB.TimeInStateValue.NAME_STATE + " int, " + DB.TimeInStateValue.NAME_TIME + " long )";

		public CpuTunerOpenHelper(Context context) {
			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TRIGGERS_TABLE);
			db.execSQL(CREATE_CPUPROFILES_TABLE);
			db.execSQL(CREATE_VIRTUAL_GOVERNOR_TABLE);
			db.execSQL(CREATE_CONFIGURATION_AUTOLOAD_TABLE);
			db.execSQL(CREATE_SWITCH_LOG_TABLE);
			db.execSQL(CREATE_TIS_INDEX_TABLE);
			db.execSQL(CREATE_TIS_VALUE_TABLE);
			db.execSQL("create index idx_trigger_battery_level on " + Trigger.TABLE_NAME + " (" + Trigger.NAME_BATTERY_LEVEL + "); ");
			db.execSQL("create index idx_cpuprofiles_profilename on " + CpuProfile.TABLE_NAME + " (" + CpuProfile.NAME_PROFILE_NAME + "); ");
			db.execSQL("create index idx_switchlog_time on " + SwitchLogDB.TABLE_NAME + " (" + SwitchLogDB.NAME_TIME + "); ");
			db.execSQL("create index idx_tis_tigger on " + TimeInStateIndex.TABLE_NAME + " (" + TimeInStateIndex.NAME_TRIGGER + "); ");
			db.execSQL("create index idx_tis_profile on " + TimeInStateIndex.TABLE_NAME + " (" + TimeInStateIndex.NAME_PROFILE + "); ");
			Logger.i("Created tables ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1:
				Logger.w("Upgrading to DB Version 2...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_WIFI_STATE + " int;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_GPS_STATE + " int;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_BLUETOOTH_STATE + " int;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_MOBILEDATA_3G_STATE + " int;");
				// nobreak

			case 2:
				Logger.w("Upgrading to DB Version 3...");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_POW + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_POW + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_BAT + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_BAT + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_LCK + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_LCK + " long;");

			case 3:
				Logger.w("Upgrading to DB Version 4...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + " int DEFAULT 0;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN + " int DEFAULT 0;");

			case 4:
				Logger.w("Upgrading to DB Version 5...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_BACKGROUND_SYNC_STATE + " int default 0;");

			case 5:
				Logger.w("Upgrading to DB Version 6...");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_HOT_PROFILE_ID + " long default -1;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_HOT + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_HOT + " long;");

			case 6:
				Logger.w("Upgrading to DB Version 7...");
				db.execSQL(CREATE_VIRTUAL_GOVERNOR_TABLE);
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + DB.CpuProfile.NAME_VIRTUAL_GOVERNOR + " int default -1;");

			case 7:
				Logger.w("Upgrading to DB Version 8...");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_CALL_IN_PROGRESS_PROFILE_ID + " long default -1;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_CALL + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_CALL + " long;");

			case 8:
				Logger.w("Upgrading to DB Version 9...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE + " int default 0;");

			case 9:
				Logger.w("Upgrading to DB Version 10...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_SCRIPT + " text;");
				db.execSQL("alter table " + VirtualGovernor.TABLE_NAME + " add column " + VirtualGovernor.NAME_SCRIPT + " text;");

			case 10:
				Logger.w("Upgrading to DB Version 11...");
				try {
					db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_POWERSEAVE_BIAS + " int;");
					db.execSQL("alter table " + VirtualGovernor.TABLE_NAME + " add column " + VirtualGovernor.NAME_POWERSEAVE_BIAS + " int;");
				} catch (Throwable e) {
				}

			case 11:
				Logger.w("Upgrading to DB Version 12...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_AIRPLANEMODE_STATE + " int;");

			case 12:
				Logger.w("Upgrading to DB Version 13...");
				db.execSQL(CREATE_CONFIGURATION_AUTOLOAD_TABLE);

			case 13:
				Logger.w("Upgrading to DB Version 14...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_USE_NUMBER_OF_CPUS + " int;");
				db.execSQL("alter table " + VirtualGovernor.TABLE_NAME + " add column " + VirtualGovernor.NAME_USE_NUMBER_OF_CPUS + " int;");

			case 14:
				Logger.w("Upgrading to DB Version 15...");
				db.execSQL(CREATE_SWITCH_LOG_TABLE);
				db.execSQL("create index idx_switchlog_time on " + SwitchLogDB.TABLE_NAME + " (" + SwitchLogDB.NAME_TIME + "); ");

			case 15:
				Logger.w("Upgrading to DB Version 16...");
				db.execSQL(CREATE_TIS_INDEX_TABLE);
				db.execSQL(CREATE_TIS_VALUE_TABLE);
				db.execSQL("create index idx_tis_tigger on " + TimeInStateIndex.TABLE_NAME + " (" + TimeInStateIndex.NAME_TRIGGER + "); ");
				db.execSQL("create index idx_tis_profile on " + TimeInStateIndex.TABLE_NAME + " (" + TimeInStateIndex.NAME_PROFILE + "); ");

			default:
				Logger.w("Finished DB upgrading!");
				break;
			}
		}
	}

	public interface Trigger {

		public static final String TABLE_NAME = "triggers";

		public static final String CONTENT_ITEM_NAME = "trigger";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_TRIGGER_NAME = "triggerName";
		public static final String NAME_BATTERY_LEVEL = "batteryLevel";
		public static final String NAME_SCREEN_OFF_PROFILE_ID = "screenOffProfileId";
		public static final String NAME_BATTERY_PROFILE_ID = "batteryProfileId";
		public static final String NAME_POWER_PROFILE_ID = "powerProfileId";
		public static final String NAME_POWER_CURRENT_SUM_POW = "powerCurrentSumPower";
		public static final String NAME_POWER_CURRENT_CNT_POW = "powerCurrentCntPower";
		public static final String NAME_POWER_CURRENT_SUM_BAT = "powerCurrentSumBattery";
		public static final String NAME_POWER_CURRENT_CNT_BAT = "powerCurrentCntBattery";
		public static final String NAME_POWER_CURRENT_SUM_LCK = "powerCurrentSumLocked";
		public static final String NAME_POWER_CURRENT_CNT_LCK = "powerCurrentCntLocked";
		public static final String NAME_HOT_PROFILE_ID = "hotProfileId";
		public static final String NAME_POWER_CURRENT_SUM_HOT = "powerCurrentSumHot";
		public static final String NAME_POWER_CURRENT_CNT_HOT = "powerCurrentCntHot";
		public static final String NAME_CALL_IN_PROGRESS_PROFILE_ID = "callInProgressProfileId";
		public static final String NAME_POWER_CURRENT_SUM_CALL = "powerCurrentSumCall";
		public static final String NAME_POWER_CURRENT_CNT_CALL = "powerCurrentCntCall";

		public static final int INDEX_TRIGGER_NAME = 1;
		public static final int INDEX_BATTERY_LEVEL = 2;
		public static final int INDEX_SCREEN_OFF_PROFILE_ID = 3;
		public static final int INDEX_BATTERY_PROFILE_ID = 4;
		public static final int INDEX_POWER_PROFILE_ID = 5;
		public static final int INDEX_POWER_CURRENT_SUM_POW = 6;
		public static final int INDEX_POWER_CURRENT_CNT_POW = 7;
		public static final int INDEX_POWER_CURRENT_SUM_BAT = 8;
		public static final int INDEX_POWER_CURRENT_CNT_BAT = 9;
		public static final int INDEX_POWER_CURRENT_SUM_LCK = 10;
		public static final int INDEX_POWER_CURRENT_CNT_LCK = 11;
		public static final int INDEX_HOT_PROFILE_ID = 12;
		public static final int INDEX_POWER_CURRENT_SUM_HOT = 13;
		public static final int INDEX_POWER_CURRENT_CNT_HOT = 14;
		public static final int INDEX_CALL_IN_PROGRESS_PROFILE_ID = 15;
		public static final int INDEX_POWER_CURRENT_SUM_CALL = 16;
		public static final int INDEX_POWER_CURRENT_CNT_CALL = 17;

		public static final String[] colNames = new String[] { NAME_ID, NAME_TRIGGER_NAME, NAME_BATTERY_LEVEL, NAME_SCREEN_OFF_PROFILE_ID,
				NAME_BATTERY_PROFILE_ID, NAME_POWER_PROFILE_ID, NAME_POWER_CURRENT_SUM_POW, NAME_POWER_CURRENT_CNT_POW, NAME_POWER_CURRENT_SUM_BAT,
				NAME_POWER_CURRENT_CNT_BAT, NAME_POWER_CURRENT_SUM_LCK, NAME_POWER_CURRENT_CNT_LCK, NAME_HOT_PROFILE_ID, NAME_POWER_CURRENT_SUM_HOT,
				NAME_POWER_CURRENT_CNT_HOT, NAME_CALL_IN_PROGRESS_PROFILE_ID, NAME_POWER_CURRENT_SUM_CALL, NAME_POWER_CURRENT_CNT_CALL };
		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String[] PROJECTION_BATTERY_LEVEL = new String[] { NAME_ID, NAME_BATTERY_LEVEL };

		public static final String[] PROJECTION_ID_NAME = new String[] { NAME_ID, NAME_TRIGGER_NAME };

		public static final String[] PROJECTION_MINIMAL_HOT_PROFILE = new String[] { NAME_HOT_PROFILE_ID };

		public static final String SORTORDER_DEFAULT = NAME_BATTERY_LEVEL + " DESC";

		static final String SORTORDER_REVERSE = NAME_BATTERY_LEVEL + " ASC";

		static final String SORTORDER_MINIMAL_HOT_PROFILE = NAME_HOT_PROFILE_ID + " ASC";

		public static final String SELECTION_NAME = NAME_TRIGGER_NAME + "=?";

		public static final String SELECTION_BATTERYLEVEL = NAME_BATTERY_LEVEL + "=?";

	}

	public interface CpuProfile {

		public static final String TABLE_NAME = "cpuProfiles";

		public static final String CONTENT_ITEM_NAME = "cpuProfile";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_PROFILE_NAME = "profileName";
		public static final String NAME_GOVERNOR = "governor";
		public static final String NAME_FREQUENCY_MAX = "frequencyMax";
		public static final String NAME_FREQUENCY_MIN = "frequencyMin";
		public static final String NAME_WIFI_STATE = "wifiState";
		public static final String NAME_GPS_STATE = "gpsState";
		public static final String NAME_BLUETOOTH_STATE = "bluetoothState";
		public static final String NAME_MOBILEDATA_3G_STATE = "mobiledataState";
		public static final String NAME_GOVERNOR_THRESHOLD_UP = "governorThresholdUp";
		public static final String NAME_GOVERNOR_THRESHOLD_DOWN = "governorThresholdDown";
		public static final String NAME_BACKGROUND_SYNC_STATE = "backgroundSyncState";
		public static final String NAME_VIRTUAL_GOVERNOR = "virtualGovernor";
		public static final String NAME_MOBILEDATA_CONNECTION_STATE = "mobiledataConnectionState";
		public static final String NAME_SCRIPT = "script";
		public static final String NAME_POWERSEAVE_BIAS = "powersaveBias";
		public static final String NAME_AIRPLANEMODE_STATE = "AIRPLANEMODE";
		public static final String NAME_USE_NUMBER_OF_CPUS = "useNumberOfCpus";

		public static final int INDEX_PROFILE_NAME = 1;
		public static final int INDEX_GOVERNOR = 2;
		public static final int INDEX_FREQUENCY_MAX = 3;
		public static final int INDEX_FREQUENCY_MIN = 4;
		public static final int INDEX_WIFI_STATE = 5;
		public static final int INDEX_GPS_STATE = 6;
		public static final int INDEX_BLUETOOTH_STATE = 7;
		public static final int INDEX_MOBILEDATA_3G_STATE = 8;
		public static final int INDEX_GOVERNOR_THRESHOLD_UP = 9;
		public static final int INDEX_GOVERNOR_THRESHOLD_DOWN = 10;
		public static final int INDEX_BACKGROUND_SYNC_STATE = 11;
		public static final int INDEX_VIRTUAL_GOVERNOR = 12;
		public static final int INDEX_MOBILEDATA_CONNECTION_STATE = 13;
		public static final int INDEX_SCRIPT = 14;
		public static final int INDEX_POWERSEAVE_BIAS = 15;
		public static final int INDEX_AIRPLANEMODE_STATE = 16;
		public static final int INDEX_USE_NUMBER_OF_CPUS = 17;

		public static final String[] colNames = new String[] { NAME_ID, NAME_PROFILE_NAME, NAME_GOVERNOR, NAME_FREQUENCY_MAX,
				NAME_FREQUENCY_MIN, NAME_WIFI_STATE, NAME_GPS_STATE, NAME_BLUETOOTH_STATE, NAME_MOBILEDATA_3G_STATE, NAME_GOVERNOR_THRESHOLD_UP,
				NAME_GOVERNOR_THRESHOLD_DOWN, NAME_BACKGROUND_SYNC_STATE, NAME_VIRTUAL_GOVERNOR,
				NAME_MOBILEDATA_CONNECTION_STATE, NAME_SCRIPT, NAME_POWERSEAVE_BIAS, NAME_AIRPLANEMODE_STATE, NAME_USE_NUMBER_OF_CPUS };
		public static final String[] PROJECTION_DEFAULT = colNames;
		public static final String[] PROJECTION_PROFILE_NAME = new String[] { NAME_ID, NAME_PROFILE_NAME };

		public static final String SORTORDER_DEFAULT = NAME_FREQUENCY_MAX + " DESC";

		static final String SORTORDER_REVERSE = NAME_PROFILE_NAME + " ASC";

		public static final String[] PROJECTION_ID_NAME = new String[] { NAME_ID, NAME_PROFILE_NAME };

		public static final String SELECTION_NAME = NAME_PROFILE_NAME + "=?";

	}

	public interface VirtualGovernor {

		public static final String TABLE_NAME = "virtualGovernor";

		public static final String CONTENT_ITEM_NAME = "virtualGovernor";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_VIRTUAL_GOVERNOR_NAME = "virtualGovernor";
		public static final String NAME_REAL_GOVERNOR = "governor";
		public static final String NAME_GOVERNOR_THRESHOLD_UP = "governorThresholdUp";
		public static final String NAME_GOVERNOR_THRESHOLD_DOWN = "governorThresholdDown";
		public static final String NAME_SCRIPT = "script";
		public static final String NAME_POWERSEAVE_BIAS = "powersaveBias";
		public static final String NAME_USE_NUMBER_OF_CPUS = "useNumberOfCpus";

		public static final int INDEX_VIRTUAL_GOVERNOR_NAME = 1;
		public static final int INDEX_REAL_GOVERNOR = 2;
		public static final int INDEX_GOVERNOR_THRESHOLD_UP = 3;
		public static final int INDEX_GOVERNOR_THRESHOLD_DOWN = 4;
		public static final int INDEX_SCRIPT = 5;
		public static final int INDEX_POWERSEAVE_BIAS = 6;
		public static final int INDEX_USE_NUMBER_OF_CPUS = 7;

		public static final String[] colNames = new String[] { NAME_ID, NAME_VIRTUAL_GOVERNOR_NAME, NAME_REAL_GOVERNOR,
				NAME_GOVERNOR_THRESHOLD_UP, NAME_GOVERNOR_THRESHOLD_DOWN,
				NAME_SCRIPT, NAME_POWERSEAVE_BIAS, NAME_USE_NUMBER_OF_CPUS };
		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String SORTORDER_DEFAULT = NAME_GOVERNOR_THRESHOLD_UP + " ASC";
		public static final String SORTORDER_REVERSE = NAME_GOVERNOR_THRESHOLD_UP + " DESC";

		public static final String[] PROJECTION_ID_NAME = new String[] { NAME_ID, NAME_VIRTUAL_GOVERNOR_NAME };

		public static final String SELECTION_NAME = NAME_VIRTUAL_GOVERNOR_NAME + "=?";

	}

	public interface ConfigurationAutoload {

		public static final String TABLE_NAME = "configurationAutoload";

		public static final String CONTENT_ITEM_NAME = TABLE_NAME;
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_HOUR = "hour";
		public static final String NAME_MINUTE = "minute";
		public static final String NAME_WEEKDAY = "weekday";
		public static final String NAME_CONFIGURATION = "configuration";
		public static final String NAME_NEXT_EXEC = "nextExecution";
		public static final String NAME_EXACT_SCEDULING = "exactSceduling";

		public static final int INDEX_HOUR = 1;
		public static final int INDEX_MINUTE = 2;
		public static final int INDEX_WEEKDAY = 3;
		public static final int INDEX_CONFIGURATION = 4;
		public static final int INDEX_NEXT_EXEC = 5;
		public static final int INDEX_EXACT_SCEDULING = 6;

		public static final String[] colNames = new String[] { NAME_ID, NAME_HOUR, NAME_MINUTE, NAME_WEEKDAY, NAME_CONFIGURATION, NAME_NEXT_EXEC, NAME_EXACT_SCEDULING };
		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String SORTORDER_DEFAULT = NAME_NEXT_EXEC + " ASC";
		public static final String SORTORDER_REVERSE = NAME_NEXT_EXEC + " DESC";

		public static final String SELECTION_TIME_WEEKDAY = NAME_HOUR + "=? and " + NAME_MINUTE + "=? and " + NAME_WEEKDAY + "=? ";
		public static final String SELECTION_NAME = DB.ConfigurationAutoload.NAME_CONFIGURATION + "=?";
	}

	public interface SwitchLogDB {

		public static final String TABLE_NAME = "switchLog";

		public static final String CONTENT_ITEM_NAME = TABLE_NAME;
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_TIME = "time";
		public static final String NAME_MESSAGE = "message";
		public static final String NAME_TRIGGER = "trigger";
		public static final String NAME_PROFILE = "profile";
		public static final String NAME_VIRTGOV = "virtGov";
		public static final String NAME_BATTERY = "battery";
		public static final String NAME_LOCKED = "locked";
		public static final String NAME_AC = "ac";
		public static final String NAME_CALL = "call";
		public static final String NAME_HOT = "hot";

		public static final int INDEX_TIME = 1;
		public static final int INDEX_MESSAGE = 2;
		public static final int INDEX_TRIGGER = 3;
		public static final int INDEX_PROFILE = 4;
		public static final int INDEX_VIRTGOV = 5;
		public static final int INDEX_BATTERY = 6;
		public static final int INDEX_LOCKED = 7;
		public static final int INDEX_AC = 8;
		public static final int INDEX_CALL = 9;
		public static final int INDEX_HOT = 10;

		public static final String[] colNames = new String[] { NAME_ID, NAME_TIME, NAME_MESSAGE, NAME_TRIGGER, NAME_PROFILE, NAME_VIRTGOV, NAME_BATTERY, NAME_LOCKED, NAME_AC,
				NAME_CALL, NAME_HOT };
		public static final String[] PROJECTION_DEFAULT = colNames;
		public static final String[] PROJECTION_NORMAL_LOG = new String[] { NAME_ID, NAME_TIME, NAME_MESSAGE };

		public static final String SORTORDER_DEFAULT = NAME_TIME + " DESC";
		public static final String SORTORDER_REVERSE = NAME_TIME + " ASC";

		public static final String SELECTION_BY_TIME = NAME_TIME + " < ?";

	}

	public interface TimeInStateIndex {

		public static final String TABLE_NAME = "TimeInStateIndex";

		public static final String CONTENT_ITEM_NAME = TABLE_NAME;
		public static final String CONTENT_ITEM_NAME_DISTINCT = TABLE_NAME + "_DISTINCT";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static String CONTENT_URI_STRING_DISTINCT = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME_DISTINCT;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
		public static Uri CONTENT_URI_DISTINCT = Uri.parse(CONTENT_URI_STRING_DISTINCT);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_TIME = "time";
		public static final String NAME_TRIGGER = "trigger";
		public static final String NAME_PROFILE = "profile";
		public static final String NAME_VIRTGOV = "virtGov";

		public static final int INDEX_TIME = 1;
		public static final int INDEX_TRIGGER = 2;
		public static final int INDEX_PROFILE = 3;
		public static final int INDEX_VIRTGOV = 4;

		public static final String[] colNames = new String[] { NAME_ID, NAME_TIME, NAME_TRIGGER, NAME_PROFILE, NAME_VIRTGOV };

		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String SORTORDER_DEFAULT = NAME_TIME + " DESC";
		public static final String SORTORDER_REVERSE = NAME_TIME + " ASC";

		public static final String SELECTION_TRIGGER_PROFILE_VIRTGOV = NAME_TRIGGER + " like ? and " + NAME_PROFILE + " like ? and " + NAME_VIRTGOV + " like ?";

	}

	public interface TimeInStateValue {

		public static final String TABLE_NAME = "TimeInStateValue";

		public static final String CONTENT_ITEM_NAME = TABLE_NAME;
		public static final String CONTENT_ITEM_NAME_GROUPED = TABLE_NAME + "GROUPED";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static String CONTENT_URI_STRING_GROUPED = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME_GROUPED;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
		public static Uri CONTENT_URI_GROUPED = Uri.parse(CONTENT_URI_STRING_GROUPED);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
		static final String CONTENT_TYPE_GROUPED = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME_GROUPED;
		static final String CONTENT_ITEM_TYPE_GROUPED = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME_GROUPED;

		public static final String NAME_IDX = "tisIndex";
		public static final String NAME_STATE = "state";
		public static final String NAME_TIME = "time";

		public static final int INDEX_IDX = 1;
		public static final int INDEX_STATE = 2;
		public static final int INDEX_TIME = 3;

		public static final String[] colNames = new String[] { NAME_ID, NAME_IDX, NAME_STATE, NAME_TIME };

		public static final String[] PROJECTION_TIME_SUM = new String[] { TABLE_NAME + "." + NAME_ID + " as " + NAME_ID, NAME_IDX, NAME_STATE,
				"total(" + TABLE_NAME + "." + NAME_TIME + ") as time" };

		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String SORTORDER_DEFAULT = NAME_STATE + " ASC";
		public static final String SORTORDER_REVERSE = NAME_STATE + " DESC";

		public static final String SELECTION_BY_ID_STATE = NAME_ID + " like ? and " + NAME_STATE + " like ?";

	}
}