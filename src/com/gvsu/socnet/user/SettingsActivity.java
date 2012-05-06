/** SettingsActivity.java */
package com.gvsu.socnet.user;

import soc.net.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/****************************************************************
 * com.gvsusocnet.SettingsActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference logout = (Preference) findPreference("logout");
		logout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				getSharedPreferences("profile", 0).edit().putString("player_id", "-1").commit();
				finish();
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "Show current settings");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// Toast!!
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			StringBuilder builder = new StringBuilder();
			builder.append(sharedPrefs.getBoolean("perform_updates", false));
			builder.append(" " + sharedPrefs.getString("updates_interval", "-1"));
			builder.append(" " + sharedPrefs.getString("welcome_message", "NULL"));

			Toast.makeText(getApplicationContext(), builder.toString(), Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
}
