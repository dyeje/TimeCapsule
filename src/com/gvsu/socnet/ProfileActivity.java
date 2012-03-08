package com.gvsu.socnet;

import soc.net.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/****************************************************************
 * com.gvsusocnet.ProfileActivity
 * @version 1.0
 ***************************************************************/
public class ProfileActivity extends NavigationMenu implements
    OnClickListener {

	// private SharedPreferences prefs;
	public final String PROFILE = "profile";
	private final String TAB = "\t";
	private final String NO_CONN = "No Network Connection";
	private final String NO_CONN_INFO = "Many features of this app will not work without an internet connection";
	private TextView username, name, location, gender, age,
	    interests, aboutme;
	private OnSharedPreferenceChangeListener listener;

	/****************************************************************
	 * @see com.gvsusocnet.NavigationMenu#onCreate(android.os.Bundle)
	 * @param savedInstanceState
	 ***************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
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

		btnProfile.setEnabled(false);
		btnInfo.setOnClickListener(this);
		TextView btnStat = (TextView) findViewById(R.id.text_name);
		btnStat.setOnClickListener(this);
		TextView btnClan = (TextView) findViewById(R.id.text_age);
		btnClan.setOnClickListener(this);

		SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
		setInfo(prefs);
		listener = new OnSharedPreferenceChangeListener() {

			public void onSharedPreferenceChanged(
			    SharedPreferences sPrefs, String key) {

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
					Log.d("debug",
					    "I don't know what was changed, updating all");
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
		getSharedPreferences(PROFILE, 0)
		    .unregisterOnSharedPreferenceChangeListener(listener);
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
			if (playerId.equals("")) {
				Intent i = new Intent(getApplicationContext(),
				    LoginActivity.class);
				startActivity(i);
				finish();
			} else {
				String s = Server.getUser(playerId);
				if (!s.equals("")) {
					String[] userinfo = s.split(TAB);
					SharedPreferences.Editor editor = prefs.edit();
					if (!prefs.getString("username", "").equals(
					    userinfo[8]))
						editor.putString("username", userinfo[8]);
					if (!prefs.getString("name", "").equals(
					    userinfo[0]))
						editor.putString("name", userinfo[0]);
					String strLocation = userinfo[1] + ", "
					    + userinfo[2];
					if (!prefs.getString("location", "").equals(
					    strLocation))
						editor.putString("location", userinfo[1]
						    + ", " + userinfo[2]);
					String strGender = userinfo[3];
					if (strGender.equalsIgnoreCase("m")) {
						strGender = "Male";
					} else if (strGender.equalsIgnoreCase("f")) {
						strGender = "Female";
					} else {
						strGender = "Other";
					}
					if (!prefs.getString("gender", "").equals(
					    strGender))
						editor.putString("gender", strGender);
					if (!prefs.getString("age", "").equals(
					    userinfo[4]))
						editor.putString("age", userinfo[4]);
					if (!prefs.getString("interests", "").equals(
					    userinfo[5]))
						editor.putString("interests", userinfo[5]);
					if (!prefs.getString("aboutme", "").equals(
					    userinfo[6]))
						editor.putString("aboutme", userinfo[6]);
					editor.commit();
				}
			}
		} else {
			Log.d("debug", "couldn't update - no network connection");
		}
	}

	/****************************************************************
	 * @return String
	 ***************************************************************/
	public String getStats(SharedPreferences prefs) {
		String strId = prefs.getString("user_id", "");
		if (!strId.equals("")) {
			// String s = Server.getUser(strId);
			// String[] userInfo = s.split("\t");
			//
			// /*********LOG**********LOG*************/
			// Log.println(3, "debug", s);
			// /*********LOG**********LOG*************/

			// String str = "Level: " + userInfo[1] + "\nAbility: "
			// + userInfo[2] + "\nPermissions: " + userInfo[3]
			// + "\n\nTreasures Found: " + "xxx"
			// + "\nTreasures Placed: " + "xxx";
			// for (int i = 0; i < 10; i++)
			// str += "\nMore Stats: XXX";

			// return str;
			return "";
		} else {
			return "";
		}
	}

	// /****************************************************************
	// * @return String
	// ***************************************************************/
	// public String getPlayerId() {
	// return prefs.getString("player_id", "");
	// }

	/****************************************************************
	 * @see com.gvsusocnet.NavigationMenu#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v) {
		/*********LOG**********LOG*************/
		Log.println(2, "debug", "Click Switch");
		/*********LOG**********LOG*************/
		switch (v.getId()) {
		case R.id.player_info:
			// showDialog(getStats(), "Detailed Stats");
			break;
		case R.id.text_name:
			// showDialog(getStats(), "Detailed Stats");
			break;
		case R.id.text_age:
			// showDialog(getStats(), "Detailed Stats");
			break;
		case R.id.btn_capture:
			Intent myIntent = new Intent(this, AddCapsule.class);
			startActivity(myIntent);
			break;
		case -123:
			Intent i = new Intent(getApplicationContext(),
			    CapsuleActivity.class);
			startActivity(i);
			break;
		default:
			super.onClick(v);
			break;
		}
	}

	/****************************************************************
	 * @return boolean
	 ***************************************************************/
	private boolean gotoTreasure() {
		Intent myIntent = new Intent(this, CapsuleActivity.class);
		startActivity(myIntent);
		return true;
	}

	/****************************************************************
	 * @param title
	 * @param info void
	 ***************************************************************/
	private void showDialog(String title, String info) {
		/*********LOG**********LOG*************/
		// Log.println(3, "debug", "showing: " + info);
		/*********LOG**********LOG*************/

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) this
		    .getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.detailed_stats,
		    (ViewGroup) findViewById(R.id.layout_root));

		TextView text = (TextView) layout
		    .findViewById(R.id.text_level);
		text.setText(info);

		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		alertDialog = builder.create();
		alertDialog.setTitle(title);
		alertDialog.show();
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
	
	protected boolean gotoProfile() {
		
		return false;
	}
	protected boolean gotoMap() {
		Intent myIntent = new Intent(getBaseContext(),
		    CapsuleMapActivity.class);
		startActivity(myIntent);
		return true;
	}
}