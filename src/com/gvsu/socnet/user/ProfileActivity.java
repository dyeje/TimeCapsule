package com.gvsu.socnet.user;

import com.gvsu.socnet.data.Server;
import com.gvsu.socnet.map.CapsuleMapActivity;
import com.gvsu.socnet.views.NavigationMenu;

import soc.net.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/****************************************************************
 * com.gvsusocnet.ProfileActivity
 * @version 1.0
 ***************************************************************/
public class ProfileActivity extends NavigationMenu implements OnClickListener {

	// private SharedPreferences prefs;
	public final String PROFILE = "profile", PLAYER_ID = "player_id";
	private final String TAB = "\t";
	private final String NO_CONN = "No Network Connection";
	private final String NO_CONN_INFO = "Many features of this app will not work without an internet connection";
	private final String PROFILE_NOT_RETRIEVED = "Sorry, your profile could not be retrieved :(";
	private TextView username, name, location, gender, age, interests, aboutme;
	private OnSharedPreferenceChangeListener listener;

	/****************************************************************
	 * @see com.gvsu.socnet.views.gvsusocnet.NavigationMenu#onCreate(android.os.Bundle)
	 * @param savedInstanceState
	 ***************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// makes sure user is logged in, otherwise kicks them out to
		// the login screen
		if (getSharedPreferences(PROFILE, 0).getString(PLAYER_ID, "").equals("")) {
			logout();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		View.inflate(this, R.layout.profile, vg);

		LinearLayout btnInfo = (LinearLayout) findViewById(R.id.player_info);

		username = (TextView) findViewById(R.id.text_username);
		name = (TextView) findViewById(R.id.text_name);
		location = (TextView) findViewById(R.id.text_location);
		gender = (TextView) findViewById(R.id.text_gender);
		age = (TextView) findViewById(R.id.text_age);
		interests = (TextView) findViewById(R.id.text_interests);
		aboutme = (TextView) findViewById(R.id.text_about);

		btnProfile.setBackgroundResource(R.drawable.user_pic_edit);
		btnInfo.setOnClickListener(this);
		TextView btnStat = (TextView) findViewById(R.id.text_name);
		btnStat.setOnClickListener(this);
		TextView btnClan = (TextView) findViewById(R.id.text_age);
		btnClan.setOnClickListener(this);

		SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
		setInfo(prefs);
		listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key) {

				Log.d("debug", "prefs updated:key=" + key);

				if (key.equals("username")) {
					username.setText(sPrefs.getString(key, ""));
				} else if (key.equals("name")) {
					name.setText(sPrefs.getString(key, ""));
				} else if (key.equals("location")) {
					location.setText(sPrefs.getString(key, ""));
				} else if (key.equals("age")) {
					age.setText(sPrefs.getString(key, ""));
				} else if (key.equals("gender")) {
					gender.setText(sPrefs.getString(key, ""));
				} else if (key.equals("interests")) {
					interests.setText(sPrefs.getString(key, ""));
				} else if (key.equals("aboutme")) {
					aboutme.setText(sPrefs.getString(key, ""));
				} else {
					Log.d("debug", "I don't know what was changed, updating all");
					setInfo(sPrefs);
				}
			}
		};
	}

	/****************************************************************
	 * @see android.app.Activity#onResume()
	 ***************************************************************/
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
		prefs.registerOnSharedPreferenceChangeListener(listener);

		if (isOnline()) {
			refresh();
		} else {
			showDialog(NO_CONN, NO_CONN_INFO);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		getSharedPreferences(PROFILE, 0).unregisterOnSharedPreferenceChangeListener(listener);
	}

	private void setInfo(SharedPreferences prefs) {
		username.setText(prefs.getString("username", "-----"));
		name.setText(prefs.getString("name", "----"));
		location.setText(prefs.getString("location", "---, --"));
		gender.setText(prefs.getString("gender", "--"));
		age.setText(prefs.getString("age", "--") + " years old");
		interests.setText(prefs.getString("interests", "-----"));
		aboutme.setText(prefs.getString("aboutme", "-----"));
	}

	/****************************************************************
	 * Checks server for user information and updates any changes 
	 * void
	 ***************************************************************/
	@Override
	protected void refresh() {
		SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
		boolean online = isOnline();

		// make sure we have internet connection before talking to
		// server
		if (online) {
			Log.d("debug", "updating from network");
			String playerId = prefs.getString("player_id", "");
			String s = Server.getUser(playerId);
			if (!s.equals("")) {
				String[] userinfo = s.split(TAB);
				SharedPreferences.Editor editor = prefs.edit();
				if (!prefs.getString("username", "").equals(userinfo[8]))
					editor.putString("username", userinfo[8]);
				if (!prefs.getString("name", "").equals(userinfo[0]))
					editor.putString("name", userinfo[0]);
				String strLocation = userinfo[1] + ", " + userinfo[2];
				if (!prefs.getString("location", "").equals(strLocation))
					editor.putString("location", userinfo[1] + ", " + userinfo[2]);
				String strGender = userinfo[3];
				if (strGender.equalsIgnoreCase("m")) {
					strGender = "Male";
				} else if (strGender.equalsIgnoreCase("f")) {
					strGender = "Female";
				} else {
					strGender = "Other";
				}
				if (!prefs.getString("gender", "").equals(strGender))
					editor.putString("gender", strGender);
				if (!prefs.getString("age", "").equals(userinfo[4]))
					editor.putString("age", userinfo[4]);
				if (!prefs.getString("interests", "").equals(userinfo[5]))
					editor.putString("interests", userinfo[5]);
				if (!prefs.getString("aboutme", "").equals(userinfo[6]))
					editor.putString("aboutme", userinfo[6]);
				editor.commit();
			}
		} else {
			Log.d("debug", "couldn't update - no network connection");
			showDialog(NO_CONN, PROFILE_NOT_RETRIEVED);
		}
	}

	/****************************************************************
	 * @param title
	 * @param info void
	 ***************************************************************/
	private void showDialog(String title, String info) {

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.detailed_stats, (ViewGroup) findViewById(R.id.layout_root));

		TextView text = (TextView) layout.findViewById(R.id.text_level);
		text.setText(info);

		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		alertDialog = builder.create();
		alertDialog.setTitle(title);
		alertDialog.show();
	}

	private void logout() {
		Intent i = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(i);
		finish();
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean gotoMenu() {
		Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(i);
		return true;
	}

	@Override
	protected boolean gotoProfile() {
		Toast.makeText(this, "Edit your profile", Toast.LENGTH_SHORT).show();
		Intent i = new Intent(getApplicationContext(), NewUserActivity.class);
		startActivity(i);
		return false;
	}

	@Override
	protected boolean gotoMap() {
		Intent myIntent = new Intent(getBaseContext(), CapsuleMapActivity.class);
		startActivity(myIntent);
		return true;
	}

	@Override
	protected boolean newCapsule() {
		Intent myIntent = new Intent(this, AddCapsuleActivity.class);
		startActivity(myIntent);
		return true;
	}
}