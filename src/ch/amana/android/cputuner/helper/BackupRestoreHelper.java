package ch.amana.android.cputuner.helper;

import java.io.File;
import java.io.InputStream;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import ch.almana.android.importexportdb.BackupRestoreCallback;
import ch.almana.android.importexportdb.constants.JsonConstants;
import ch.almana.android.importexportdb.exporter.ExportConfig;
import ch.almana.android.importexportdb.exporter.ExportConfig.ExportType;
import ch.almana.android.importexportdb.exporter.ExportDataTask;
import ch.almana.android.importexportdb.importer.DataJsonImporter;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GovernorConfigHelper.GovernorConfig;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.DB;
import ch.amana.android.cputuner.provider.DB.OpenHelper;

public class BackupRestoreHelper {

	public static final String DIRECTORY_CONFIGURATIONS = "configurations";
	private static final String JSON_TAG_RES_ID = "resId";

	public static final Object MUTEX = new Object();

	private final BackupRestoreCallback cb;

	private final ContentResolver contentResolver;

	public BackupRestoreHelper(BackupRestoreCallback cb) {
		super();
		this.cb = cb;
		this.contentResolver = cb.getContext().getContentResolver();
	}

	private void backup(File storagePath) {
		if (!storagePath.isDirectory()) {
			storagePath.mkdir();
		}
		// ModelAccess.getInstace(cb.getContext()).applyDelayedTriggerUpdates();
		SQLiteDatabase db = new OpenHelper(cb.getContext()).getWritableDatabase();
		ExportConfig config = new ExportConfig(db, DB.DATABASE_NAME, storagePath, ExportType.JSON);
		config.setExcludeTable(DB.SwitchLogDB.TABLE_NAME);
		config.setExcludeTable(DB.TimeInStateIndex.TABLE_NAME);
		config.setExcludeTable(DB.TimeInStateValue.TABLE_NAME);
		ExportDataTask exportDataTask = new ExportDataTask(cb);
		exportDataTask.execute(new ExportConfig[] { config });
	}

	public static File getStoragePath(Context ctx, String directory) {
		return new File(Environment.getExternalStorageDirectory(), ctx.getPackageName() + "/" + directory);
	}

	private void restore(DataJsonImporter dje, boolean inclAutoloadConfig) throws JSONException {
		Context ctx = cb.getContext();
		CpuTunerProvider.deleteAllTables(ctx, inclAutoloadConfig);
		synchronized (ModelAccess.virtgovCacheMutex) {
			synchronized (ModelAccess.profileCacheMutex) {
				synchronized (ModelAccess.triggerCacheMutex) {
					loadVirtualGovernors(dje);
					loadCpuProfiles(dje);
					loadTriggers(dje);
					if (inclAutoloadConfig) {
						loadAutoloadConfig(dje);
					}
				}
			}
		}
		ModelAccess.getInstace(cb.getContext()).clearCache();
	}

	private String getTranslatedName(String resString) {
		Context ctx = cb.getContext();
		try {
			int id = ctx.getResources().getIdentifier(resString, "string", ctx.getPackageName());
			return ctx.getString(id);
		} catch (Exception e) {
			Logger.w("no translated name found looking for " + resString, e);
		}
		return null;
	}

	private void loadVirtualGovernors(DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.VirtualGovernor.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			VirtualGovernorModel vgm = new VirtualGovernorModel();
			JSONObject jsonObject = table.getJSONObject(i);
			vgm.readFromJson(new JSONBundle(jsonObject));
			if (jsonObject.has(JSON_TAG_RES_ID)) {
				String name = getTranslatedName(jsonObject.getString(JSON_TAG_RES_ID));
				vgm.setVirtualGovernorName(name);
			}
			contentResolver.insert(DB.VirtualGovernor.CONTENT_URI, vgm.getValues());
		}
	}

	private void loadCpuProfiles(DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.CpuProfile.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			ProfileModel pm = new ProfileModel();
			JSONObject jsonObject = table.getJSONObject(i);
			pm.readFromJson(new JSONBundle(jsonObject));
			if (jsonObject.has(JSON_TAG_RES_ID)) {
				String name = getTranslatedName(jsonObject.getString(JSON_TAG_RES_ID));
				pm.setProfileName(name);
			}
			contentResolver.insert(DB.CpuProfile.CONTENT_URI, pm.getValues());
		}
	}

	private void loadTriggers(DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.Trigger.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			TriggerModel tr = new TriggerModel();
			JSONObject jsonObject = table.getJSONObject(i);
			tr.readFromJson(new JSONBundle(jsonObject));
			if (jsonObject.has(JSON_TAG_RES_ID)) {
				String name = getTranslatedName(jsonObject.getString(JSON_TAG_RES_ID));
				tr.setName(name);
			}
			if (tr.getScreenUnlockedProfileId() == 0) {
				tr.setScreenUnlockedProfileId(tr.getScreenOffProfileId());
			}
			contentResolver.insert(DB.Trigger.CONTENT_URI, tr.getValues());
		}
	}

	private void loadAutoloadConfig(DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.ConfigurationAutoload.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			ConfigurationAutoloadModel cam = new ConfigurationAutoloadModel();
			cam.readFromJson(new JSONBundle(table.getJSONObject(i)));
			// FIXME insert or update
			contentResolver.insert(DB.ConfigurationAutoload.CONTENT_URI, cam.getValues());
		}
	}

	public void backupConfiguration(String name) {
		synchronized (MUTEX) {
			if (name == null) {
				return;
			}
			File storagePath = new File(getStoragePath(cb.getContext(), DIRECTORY_CONFIGURATIONS), name);
			if (!storagePath.isDirectory()) {
				storagePath.mkdirs();
			}
			backup(storagePath);
		}
	}

	public void restoreConfiguration(String name, boolean isUserConfig, boolean restoreAutoload) throws Exception {
		synchronized (MUTEX) {
			if (name == null) {
				return;
			}
			Logger.i("Loading configuration " + name);
			Context context = cb.getContext();
			try {
				CpuTunerProvider.setNotifyChanges(false);
				if (isUserConfig) {
					File file = new File(getStoragePath(context, DIRECTORY_CONFIGURATIONS), name);
					DataJsonImporter dje = new DataJsonImporter(DB.DATABASE_NAME, file);
					restore(dje, restoreAutoload);
				} else {
					String fileName = DIRECTORY_CONFIGURATIONS + "/" + name + "/" + DB.DATABASE_NAME + JsonConstants.FILE_NAME;
					InputStream is = context.getAssets().open(fileName);
					DataJsonImporter dje = new DataJsonImporter(DB.DATABASE_NAME, is);
					restore(dje, restoreAutoload);
					fixGovernors();
					fixProfiles();
					InstallHelper.updateProfilesFromVirtGovs(context);
				}
				PowerProfiles.getInstance(context).reapplyProfile(true);
				cb.hasFinished(true);
			} catch (Exception e) {
				Logger.e("Cannot restore configuration", e);
				cb.hasFinished(false);
				throw new Exception("Cannot restore configuration", e);
			} finally {
				CpuTunerProvider.setNotifyChanges(true);
				StringBuilder msg = new StringBuilder(context.getString(R.string.msg_config_loaded)).append(" ");
				msg.append(name.replaceFirst("\\d+_", ""));
				msg.append(" ").append(context.getString(R.string.msg_config));
				SwitchLog.addToLog(context, msg.toString());
			}
		}
	}

	private void fixProfiles() {
		SettingsStorage settings = SettingsStorage.getInstance();
		ContentValues values = new ContentValues(2);
		int maxFreq = settings.getMaxFrequencyDefault();
		values.put(DB.CpuProfile.NAME_FREQUENCY_MAX, maxFreq);
		int minFreq = settings.getMinFrequencyDefault();
		values.put(DB.CpuProfile.NAME_FREQUENCY_MIN, minFreq);
		Logger.i("Changing frequencies of default profile to min " + minFreq + " and max " + maxFreq);
		Cursor c = null;
		try {
			c = contentResolver.query(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, null, null, null);
			while (c.moveToNext()) {
				contentResolver.update(DB.CpuProfile.CONTENT_URI, values, DB.SELECTION_BY_ID, new String[] { Long.toString(c.getLong(DB.INDEX_ID)) });
			}
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

	private void fixGovernors() {
		Cursor c = null;
		String[] availCpuGov = CpuHandler.getInstance().getAvailCpuGov();
		TreeMap<String, Boolean> availGovs = new TreeMap<String, Boolean>();
		for (String gov : availCpuGov) {
			availGovs.put(gov, Boolean.TRUE);
		}
		try {
			c = contentResolver.query(DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_DEFAULT, null, null, null);
			while (c.moveToNext()) {
				String govs = c.getString(DB.VirtualGovernor.INDEX_REAL_GOVERNOR);
				try {
					JSONObject jsonObject = new JSONObject(govs);
					for (int i = 0; i <= Build.VERSION.SDK_INT; i++) {
						String idx = Integer.toString(i);
						if (jsonObject.has(idx)) {
							govs = jsonObject.getString(idx);
							Logger.v("For SDK_LEVEL " + idx + " found config governors " + govs);
						}
					}
				} catch (JSONException e) {
				}
				String[] govitems = govs.split("\\|");
				boolean found = false;
				for (String gov : govitems) {
					Boolean avail = availGovs.get(gov);
					if (avail != null && avail) {
						Logger.i("Using " + gov);
						ContentValues values = new ContentValues(1);
						values.put(DB.VirtualGovernor.NAME_REAL_GOVERNOR, gov);
						// check for thresholds
						GovernorConfig governorConfig = GovernorConfigHelper.getGovernorConfig(gov);
						if (!governorConfig.hasThreshholdUpFeature()) {
							values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, -1);
						}
						if (!governorConfig.hasThreshholdDownFeature()) {
							values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, -1);
						}
						if (contentResolver.update(DB.VirtualGovernor.CONTENT_URI, values, DB.SELECTION_BY_ID, new String[] { Long.toString(c.getLong(DB.INDEX_ID)) }) > 0) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					// we did not find a compatible gov so use none
					ContentValues values = new ContentValues(1);
					values.put(DB.VirtualGovernor.NAME_REAL_GOVERNOR, RootHandler.NOT_AVAILABLE);
					contentResolver.update(DB.VirtualGovernor.CONTENT_URI, values, DB.SELECTION_BY_ID, new String[] { Long.toString(c.getLong(DB.INDEX_ID)) });
				}
			}
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}

	}

}
