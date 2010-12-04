package ch.amana.android.cputuner.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Context ctx = getApplicationContext();
		SettingsStorage.initInstance(ctx);
		PowerProfiles.initContext(ctx);
		InstallHelper.populateDb(ctx);
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			startService(new Intent(ctx, BatteryService.class));
			PowerProfiles.reapplyProfile(true);
		}
	}
}
