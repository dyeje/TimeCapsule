/** SettingsActivity.java */
package com.gvsu.socnet.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import soc.net.R;

/**
 * *************************************************************
 * com.gvsusocnet.SettingsActivity
 *
 * @author Caleb Gomer
 * @version 1.0
 *          *************************************************************
 */
public class SettingsActivity extends PreferenceActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    Preference logout = (Preference) findPreference("logout");
    logout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

      @Override
      public boolean onPreferenceClick(Preference preference) {
        getSharedPreferences(ProfileActivity.PROFILE, 0).edit().clear().putString(ProfileActivity.PLAYER_ID, "-1").commit();
        finish();
        return false;
      }
    });
  }
}
