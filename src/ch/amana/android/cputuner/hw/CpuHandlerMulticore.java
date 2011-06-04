package ch.amana.android.cputuner.hw;

import ch.amana.android.cputuner.helper.Logger;

public class CpuHandlerMulticore extends CpuHandler {

	private String[] cpus;

	public CpuHandlerMulticore(String[] cpus) {
		super();
		this.cpus = cpus;
	}

	private boolean writeFile(String file, String value) {
		return writeFile(null, file, value);
	}

	private boolean writeFile(String subDir, String file, String value) {
		for (int i = 0; i < cpus.length; i++) {
			StringBuilder path = new StringBuilder(CPU_BASE_DIR);
			path.append("/").append(cpus[i]);
			if (subDir != null) {
				path.append("/").append(subDir);
			}
			RootHandler.writeFile(getFile(path.toString(), file), value);
		}
		return false;
	}

	public boolean setCurGov(String gov) {
		Logger.i("Setting multicore governor to " + gov);
		return writeFile(SCALING_GOVERNOR, gov);
	}


	public boolean setUserCpuFreq(int val) {
		Logger.i("Setting  multicore user frequency to " + val);
		return writeFile(SCALING_SETSPEED, val + "");
	}

	public boolean setMaxCpuFreq(int val) {
		Logger.i("Setting multicore max frequency to " + val);
		return writeFile(SCALING_MAX_FREQ, Integer.toString(val));
	}

	public boolean setMinCpuFreq(int i) {
		Logger.i("Setting multicore min frequency to " + i);
		return writeFile(SCALING_MIN_FREQ, Integer.toString(i));
	}

	public boolean setGovSamplingRate(int i) {
		return writeFile(getCurCpuGov(), GOV_SAMPLING_RATE, i + "");
	}

	public boolean setPowersaveBias(int i) {
		if (i < 0) {
			return false;
		}
		return writeFile(getCurCpuGov(), POWERSAVE_BIAS, i + "");
	}

	public boolean setGovThresholdUp(int i) {
		if (i < 1) {
			return false;
		}
		if (i > 100) {
			i = 98;
		}
		Logger.i("Setting multicore threshold up to " + i);
		return writeFile(getCurCpuGov(), GOV_TRESHOLD_UP, i + "");
	}

	public boolean setGovThresholdDown(int i) {
		if (i < 1) {
			return false;
		}
		if (i > 100) {
			i = 95;
		}
		Logger.i("Setting multicore threshold down to " + i);
		return writeFile(getCurCpuGov(), GOV_TRESHOLD_DOWN, i + "");
	}


}
