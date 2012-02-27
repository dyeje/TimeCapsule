package com.gvsu.socnet;

import soc.net.R;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

/****************************************************************
 * com.gvsusocnet.ProfileActivity
 * @version 1.0
 ***************************************************************/
public class ProfileActivity extends NavigationMenu implements
    OnClickListener {

	private SharedPreferences prefs;
	public static String BASIC_INFO = "basic_info";
	public static String TAB = "\t";

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

		btnBack.setVisibility(View.INVISIBLE);
		btnInfo.setOnClickListener(this);
		TextView btnStat = (TextView) findViewById(R.id.text_name);
		btnStat.setOnClickListener(this);
		TextView btnClan = (TextView) findViewById(R.id.text_age);
		btnClan.setOnClickListener(this);

		thisClass = getClass();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		updateBasicInfo(false);
	}

	private void setBasicInfo() {
		TextView username = (TextView) findViewById(R.id.text_username);
		TextView name = (TextView) findViewById(R.id.text_name);
		TextView location = (TextView) findViewById(R.id.text_location);
		TextView gender = (TextView) findViewById(R.id.text_gender);
		TextView age = (TextView) findViewById(R.id.text_age);
		TextView interests = (TextView) findViewById(R.id.text_interests);
		TextView about = (TextView) findViewById(R.id.text_about);

		String info = prefs.getString(BASIC_INFO, "");
		Log.d("debug", info);
		if (!info.equals("")
		    && !info.contains("Connection Failed (I/O)")) {
			String[] userInfo = info.split(TAB);

			username.setText(userInfo[8]);
			name.setText(userInfo[0]);
			location.setText(userInfo[1] + ", " + userInfo[2]);
			String strGender;
			if (userInfo[3].equalsIgnoreCase("m")) {
				strGender = "Male";
			} else if (userInfo[3].equalsIgnoreCase("f")) {
				strGender = "Female";
			} else {
				strGender = "Other";
			}
			gender.setText(strGender);
			age.setText(userInfo[4] + " years old");
			interests.setText("Interests:\n" + userInfo[5]);
			about.setText("About me:\n" + userInfo[6]);
		} else {
			name.setText(info);
		}
	}

	/**************************************************************** void
	 ***************************************************************/
	private void updateBasicInfo(boolean updateFirst) {
		if (updateFirst) {
			String s = Server.getUser(getPlayerId());
			if (!s.equals("")) {
				prefs.edit().putString(BASIC_INFO, s).commit();
			}
		}
		setBasicInfo();
	}

	/****************************************************************
	 * @return String
	 ***************************************************************/
	public String getStats() {
		String s = Server.getUser(getPlayerId());
		String[] userInfo = s.split("\t");

		/*********LOG**********LOG*************/
		Log.println(3, "debug", s);
		/*********LOG**********LOG*************/

		// String str = "Level: " + userInfo[1] + "\nAbility: "
		// + userInfo[2] + "\nPermissions: " + userInfo[3]
		// + "\n\nTreasures Found: " + "xxx"
		// + "\nTreasures Placed: " + "xxx";
		// for (int i = 0; i < 10; i++)
		// str += "\nMore Stats: XXX";

		// return str;
		return "";
	}

	/****************************************************************
	 * @return String
	 ***************************************************************/
	public String getPlayerId() {
		return prefs.getString("player_id", "01");
	}

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
	 * @param info
	 * @param title void
	 ***************************************************************/
	private void showDialog(String info, String title) {
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

	/****************************************************************
	 * @see android.app.Activity#onResume()
	 ***************************************************************/
	@Override
	public void onResume() {
		updateBasicInfo(true);
		super.onResume();
	}
}