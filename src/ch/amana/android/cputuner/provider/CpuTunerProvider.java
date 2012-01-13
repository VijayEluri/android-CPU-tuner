package ch.amana.android.cputuner.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuTunerOpenHelper;
import ch.amana.android.cputuner.provider.db.DBBackendConfigurationAutoload;
import ch.amana.android.cputuner.provider.db.DBBackendCpuProfile;
import ch.amana.android.cputuner.provider.db.DBBackendSwitchLog;
import ch.amana.android.cputuner.provider.db.DBBackendTimeinstateIndex;
import ch.amana.android.cputuner.provider.db.DBBackendTimeinstateValue;
import ch.amana.android.cputuner.provider.db.DBBackendTrigger;
import ch.amana.android.cputuner.provider.db.DBBackendVirtualGovernor;

public class CpuTunerProvider extends ContentProvider {

	public static final String ACTION_INSERT_AS_NEW = "ch.amana.android.cputuner.ACTION_INSERT_AS_NEW";

	public static final String AUTHORITY = "ch.amana.android.cputuner";

	private static final int TRIGGER = 1;
	private static final int CPU_PROFILE = 2;
	private static final int VIRTUAL_GOVERNOR = 3;
	private static final int CONFIGURATION_AUTOLOAD = 4;
	private static final int SWITCH_LOG = 5;
	private static final int TIS_INDEX = 6;
	private static final int TIS_VALUE = 7;
	private static final int TIS_VALUE_GROUPED = 8;

	private static final UriMatcher sUriMatcher;

	private static boolean notifyChanges = true;

	private CpuTunerOpenHelper openHelper;

	@Override
	public boolean onCreate() {
		openHelper = new CpuTunerOpenHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Logger.logStacktrace("Deleting entry " + uri);
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			count = DBBackendTrigger.delete(openHelper, uri, selection, selectionArgs);
			break;
		case CPU_PROFILE:
			count = DBBackendCpuProfile.delete(openHelper, uri, selection, selectionArgs);
			break;
		case VIRTUAL_GOVERNOR:
			count = DBBackendVirtualGovernor.delete(openHelper, uri, selection, selectionArgs);
			break;
		case CONFIGURATION_AUTOLOAD:
			count = DBBackendConfigurationAutoload.delete(openHelper, uri, selection, selectionArgs);
			break;
		case SWITCH_LOG:
			count = DBBackendSwitchLog.delete(openHelper, uri, selection, selectionArgs);
			break;
		case TIS_INDEX:
			count = DBBackendTimeinstateIndex.delete(openHelper, uri, selection, selectionArgs);
			break;
		case TIS_VALUE:
			count = DBBackendTimeinstateValue.delete(openHelper, uri, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		notifyChange(uri);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			return DBBackendTrigger.getType(uri);
		case CPU_PROFILE:
			return DBBackendCpuProfile.getType(uri);
		case VIRTUAL_GOVERNOR:
			return DBBackendVirtualGovernor.getType(uri);
		case CONFIGURATION_AUTOLOAD:
			return DBBackendConfigurationAutoload.getType(uri);
		case SWITCH_LOG:
			return DBBackendSwitchLog.getType(uri);
		case TIS_INDEX:
			return DBBackendTimeinstateIndex.getType(uri);
		case TIS_VALUE:
			return DBBackendTimeinstateValue.getType(uri);
		case TIS_VALUE_GROUPED:
			return DBBackendTimeinstateValue.getType(uri);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Uri ret;
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			ret = DBBackendTrigger.insert(openHelper, uri, initialValues);
			break;
		case CPU_PROFILE:
			ret = DBBackendCpuProfile.insert(openHelper, uri, initialValues);
			break;
		case VIRTUAL_GOVERNOR:
			ret = DBBackendVirtualGovernor.insert(openHelper, uri, initialValues);
			break;
		case CONFIGURATION_AUTOLOAD:
			ret = DBBackendConfigurationAutoload.insert(openHelper, uri, initialValues);
			break;
		case SWITCH_LOG:
			ret = DBBackendSwitchLog.insert(openHelper, uri, initialValues);
			break;
		case TIS_INDEX:
			ret = DBBackendTimeinstateIndex.insert(openHelper, uri, initialValues);
			break;
		case TIS_VALUE:
			ret = DBBackendTimeinstateValue.insert(openHelper, uri, initialValues);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		notifyChange(uri);
		return ret;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor c;
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			c = DBBackendTrigger.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case CPU_PROFILE:
			c = DBBackendCpuProfile.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case VIRTUAL_GOVERNOR:
			c = DBBackendVirtualGovernor.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case CONFIGURATION_AUTOLOAD:
			c = DBBackendConfigurationAutoload.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case SWITCH_LOG:
			c = DBBackendSwitchLog.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case TIS_INDEX:
			c = DBBackendTimeinstateIndex.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case TIS_VALUE:
			c = DBBackendTimeinstateValue.query(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		case TIS_VALUE_GROUPED:
			c = DBBackendTimeinstateValue.queryGrouped(openHelper, uri, projection, selection, selectionArgs, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			count = DBBackendTrigger.update(openHelper, uri, values, selection, selectionArgs);
			break;
		case CPU_PROFILE:
			count = DBBackendCpuProfile.update(openHelper, uri, values, selection, selectionArgs);
			break;
		case VIRTUAL_GOVERNOR:
			count = DBBackendVirtualGovernor.update(openHelper, uri, values, selection, selectionArgs);
			break;
		case CONFIGURATION_AUTOLOAD:
			count = DBBackendConfigurationAutoload.update(openHelper, uri, values, selection, selectionArgs);
			break;
		case SWITCH_LOG:
			count = DBBackendSwitchLog.update(openHelper, uri, values, selection, selectionArgs);
			break;
		case TIS_INDEX:
			count = DBBackendTimeinstateIndex.update(openHelper, uri, values, selection, selectionArgs);
			break;
		case TIS_VALUE:
			count = DBBackendTimeinstateValue.update(openHelper, uri, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		notifyChange(uri);
		return count;
	}

	private void notifyChange(Uri uri) {
		if (notifyChanges && SettingsStorage.getInstance().isEnableProfiles()
				&& sUriMatcher.match(uri) < CONFIGURATION_AUTOLOAD) {
			PowerProfiles.getInstance(getContext()).reapplyProfile(true);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		// BackupRestoreHelper.backup(getContext()); 
	}

	public static void setNotifyChanges(boolean b) {
		notifyChanges = b;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, DB.Trigger.CONTENT_ITEM_NAME, TRIGGER);
		sUriMatcher.addURI(AUTHORITY, DB.Trigger.CONTENT_ITEM_NAME + "/#", TRIGGER);
		sUriMatcher.addURI(AUTHORITY, DB.CpuProfile.CONTENT_ITEM_NAME, CPU_PROFILE);
		sUriMatcher.addURI(AUTHORITY, DB.CpuProfile.CONTENT_ITEM_NAME + "/#", CPU_PROFILE);
		sUriMatcher.addURI(AUTHORITY, DB.VirtualGovernor.CONTENT_ITEM_NAME, VIRTUAL_GOVERNOR);
		sUriMatcher.addURI(AUTHORITY, DB.VirtualGovernor.CONTENT_ITEM_NAME + "/#", VIRTUAL_GOVERNOR);
		sUriMatcher.addURI(AUTHORITY, DB.ConfigurationAutoload.CONTENT_ITEM_NAME, CONFIGURATION_AUTOLOAD);
		sUriMatcher.addURI(AUTHORITY, DB.ConfigurationAutoload.CONTENT_ITEM_NAME + "/#", CONFIGURATION_AUTOLOAD);
		sUriMatcher.addURI(AUTHORITY, DB.SwitchLogDB.CONTENT_ITEM_NAME, SWITCH_LOG);
		sUriMatcher.addURI(AUTHORITY, DB.SwitchLogDB.CONTENT_ITEM_NAME + "/#", SWITCH_LOG);
		sUriMatcher.addURI(AUTHORITY, DB.TimeInStateIndex.CONTENT_ITEM_NAME, TIS_INDEX);
		sUriMatcher.addURI(AUTHORITY, DB.TimeInStateIndex.CONTENT_ITEM_NAME + "/#", TIS_INDEX);
		sUriMatcher.addURI(AUTHORITY, DB.TimeInStateValue.CONTENT_ITEM_NAME, TIS_VALUE);
		sUriMatcher.addURI(AUTHORITY, DB.TimeInStateValue.CONTENT_ITEM_NAME + "/#", TIS_VALUE);
		sUriMatcher.addURI(AUTHORITY, DB.TimeInStateValue.CONTENT_ITEM_NAME_GROUPED, TIS_VALUE_GROUPED);
		sUriMatcher.addURI(AUTHORITY, DB.TimeInStateValue.CONTENT_ITEM_NAME_GROUPED + "/#", TIS_VALUE_GROUPED);
	}

	public static void deleteAllTables(Context ctx, boolean deleteAutoloadConfig) {
		Logger.logStacktrace("delete all tables");
		ContentResolver resolver = ctx.getContentResolver();
		resolver.delete(DB.Trigger.CONTENT_URI, null, null);
		resolver.delete(DB.CpuProfile.CONTENT_URI, null, null);
		resolver.delete(DB.VirtualGovernor.CONTENT_URI, null, null);
		resolver.delete(DB.TimeInStateIndex.CONTENT_URI, null, null);
		resolver.delete(DB.TimeInStateValue.CONTENT_URI, null, null);
		if (deleteAutoloadConfig) {
			resolver.delete(DB.ConfigurationAutoload.CONTENT_URI, null, null);
		}
	}
}
	