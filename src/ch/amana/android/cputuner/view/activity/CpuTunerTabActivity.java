package ch.amana.android.cputuner.view.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import ch.amana.android.cputuner.view.preference.CpuProfilePreferenceActivity;
import ch.amana.android.cputuner.view.preference.SettingsPreferenceActivity;

public class CpuTunerTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tabCurrent").setIndicator("Current").setContent(new Intent(this, TuneCpu.class)));
		tabHost.addTab(tabHost.newTabSpec("tabProfiles").setIndicator("Profiles").setContent(new Intent(this, CpuProfilePreferenceActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tabSettings").setIndicator("Settings").setContent(new Intent(this, SettingsPreferenceActivity.class)));
	}

}