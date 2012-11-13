package ch.amana.android.cputuner.model;

import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.DB;

public class ConfigurationAutoloadModel {

	private long id = -1;

	private int hour;
	private int minute;
	private int weekday;
	private String configuration;
	private boolean exactScheduling = false;

	private long nextExecution = -1;

	public ConfigurationAutoloadModel() {
		super();
		setWeekdayBit(Calendar.MONDAY, true);
		setWeekdayBit(Calendar.TUESDAY, true);
		setWeekdayBit(Calendar.WEDNESDAY, true);
		setWeekdayBit(Calendar.THURSDAY, true);
		setWeekdayBit(Calendar.FRIDAY, true);
		setWeekdayBit(Calendar.SATURDAY, true);
		setWeekdayBit(Calendar.SUNDAY, true);
	}

	public ConfigurationAutoloadModel(Cursor c) {
		this();
		this.id = c.getLong(DB.INDEX_ID);
		this.hour = c.getInt(DB.ConfigurationAutoload.INDEX_HOUR);
		this.minute = c.getInt(DB.ConfigurationAutoload.INDEX_MINUTE);
		setWeekday(c.getInt(DB.ConfigurationAutoload.INDEX_WEEKDAY));
		this.configuration = c.getString(DB.ConfigurationAutoload.INDEX_CONFIGURATION);
		this.nextExecution = c.getLong(DB.ConfigurationAutoload.INDEX_NEXT_EXEC);
		setExactScheduling(c.getInt(DB.ConfigurationAutoload.INDEX_EXACT_SCEDULING));
	}

	public ConfigurationAutoloadModel(Bundle bundle) {
		this();
		readFromBundle(bundle);
	}

	public void saveToBundle(Bundle bundle) {
		if (id > -1) {
			bundle.putLong(DB.NAME_ID, id);
		} else {
			bundle.putLong(DB.NAME_ID, -1);
		}
		bundle.putInt(DB.ConfigurationAutoload.NAME_HOUR, getHour());
		bundle.putInt(DB.ConfigurationAutoload.NAME_MINUTE, getMinute());
		bundle.putInt(DB.ConfigurationAutoload.NAME_WEEKDAY, getWeekday());
		bundle.putString(DB.ConfigurationAutoload.NAME_CONFIGURATION, getConfiguration());
		bundle.putLong(DB.ConfigurationAutoload.NAME_NEXT_EXEC, getNextExecution());
		bundle.putBoolean(DB.ConfigurationAutoload.NAME_EXACT_SCEDULING, isExactScheduling());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		hour = bundle.getInt(DB.ConfigurationAutoload.NAME_HOUR);
		minute = bundle.getInt(DB.ConfigurationAutoload.NAME_MINUTE);
		setWeekday(bundle.getInt(DB.ConfigurationAutoload.NAME_WEEKDAY));
		configuration = bundle.getString(DB.ConfigurationAutoload.NAME_CONFIGURATION);
		nextExecution = bundle.getLong(DB.ConfigurationAutoload.NAME_NEXT_EXEC);
		exactScheduling = bundle.getBoolean(DB.ConfigurationAutoload.NAME_EXACT_SCEDULING);

	}

	public void readFromJson(JSONBundle jsonBundle) {
		id = jsonBundle.getLong(DB.NAME_ID);
		hour = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_HOUR);
		minute = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_MINUTE);
		setWeekday(jsonBundle.getInt(DB.ConfigurationAutoload.NAME_WEEKDAY));
		configuration = jsonBundle.getString(DB.ConfigurationAutoload.NAME_CONFIGURATION);
		nextExecution = jsonBundle.getLong(DB.ConfigurationAutoload.NAME_NEXT_EXEC);
		setExactScheduling(jsonBundle.getInt(DB.ConfigurationAutoload.NAME_EXACT_SCEDULING));
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}
		values.put(DB.ConfigurationAutoload.NAME_HOUR, getHour());
		values.put(DB.ConfigurationAutoload.NAME_MINUTE, getMinute());
		values.put(DB.ConfigurationAutoload.NAME_WEEKDAY, getWeekday());
		values.put(DB.ConfigurationAutoload.NAME_CONFIGURATION, getConfiguration());
		values.put(DB.ConfigurationAutoload.NAME_NEXT_EXEC, getNextExecution());
		values.put(DB.ConfigurationAutoload.NAME_EXACT_SCEDULING, getExactScheduling());
		return values;
	}

	public long getDbId() {
		return id;
	}

	public void setDbId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		nextExecution = -1;
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		nextExecution = -1;
		this.minute = minute;
	}

	public int getWeekday() {
		return weekday;
	}

	private void printWeekday() {
		String out = "";
		for (int i = 0; i < 7; i++) {
			out = out + (isWeekdayInt(i) ? "1" : "0");
		}
		Logger.d("Weekday: " + out);
	}

	public boolean isWeekday(int weekdayBit) {
		boolean b = isWeekdayInt(weekdayBit);
		Logger.d("Weekday bit " + weekdayBit + " is set " + b);
		printWeekday();
		return b;
	}

	private boolean isWeekdayInt(int weekdayBit) {
		boolean b = ((weekday >>> weekdayBit) & 1) != 0;
		return b;
	}

	public void setWeekdayBit(int weekdayBit, boolean b) {
		int weekdayBitMask = (int) Math.pow(2, weekdayBit);
		Logger.d("Weekday bit " + weekdayBit + " set to " + b);
		if (b) {
			weekday |= weekdayBitMask;
		} else {
			weekday &= ~weekdayBitMask;
		}
		printWeekday();
	}

	public void setWeekday(int weekday) {
		this.weekday = weekday;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getConfiguration() {
		return configuration;
	}

	public long getNextExecution() {
		// if (nextExecution <= System.currentTimeMillis()) {
		calcNextExecution();
		// }
		return nextExecution;
	}

	public void calcNextExecution() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		long delta = -1;
		while (delta < 0) {
			delta = cal.getTimeInMillis() - System.currentTimeMillis();
			if (delta < 0) {
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		if (weekday > 0) {
			boolean loop = true;
			for (int i = 0; loop && i < 8; i++) {
				if (isWeekday(cal.get(Calendar.DAY_OF_WEEK))) {
					loop = false;
				} else {
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
		}
		nextExecution = cal.getTimeInMillis();
		Logger.d("Next execution: " + cal.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + (exactScheduling ? 1231 : 1237);
		result = prime * result + hour;
		result = prime * result + minute;
		result = prime * result + (int) (nextExecution ^ (nextExecution >>> 32));
		result = prime * result + weekday;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationAutoloadModel other = (ConfigurationAutoloadModel) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (exactScheduling != other.exactScheduling)
			return false;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		if (nextExecution != other.nextExecution)
			return false;
		if (weekday != other.weekday)
			return false;
		return true;
	}

	public void setExactScheduling(boolean useExactScheduling) {
		this.exactScheduling = useExactScheduling;
	}

	public boolean isExactScheduling() {
		return exactScheduling;
	}

	private void setExactScheduling(int i) {
		this.exactScheduling = i != 0;
	}

	private int getExactScheduling() {
		return exactScheduling ? 1 : 0;
	}

}
