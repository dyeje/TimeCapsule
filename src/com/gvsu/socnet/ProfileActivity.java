package com.gvsu.socnet;


import soc.net.R;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/****************************************************************
 * com.gvsusocnet.treasurehunt.ProfileActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class ProfileActivity extends NavigationMenu implements
    OnClickListener {

	/** String GETUSER */
	private final String GETUSER = "getUser.php?id=";

	/****************************************************************
	 * @see com.gvsusocnet.NavigationMenu#onCreate(android.os.Bundle)
	 * @param savedInstanceState
	 ***************************************************************/
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		ViewGroup.inflate(this, R.layout.profile, vg);

		LinearLayout btnInfo = (LinearLayout) findViewById(R.id.player_info);
		btnInfo.setOnClickListener(this);
		TextView btnStat = (TextView) findViewById(R.id.text_stats);
		btnStat.setOnClickListener(this);
		TextView btnClan = (TextView) findViewById(R.id.text_clan);
		btnClan.setOnClickListener(this);
		RelativeLayout btnAchieve = (RelativeLayout) findViewById(R.id.button_achieve);
		btnAchieve.setOnClickListener(this);

		// for testing add_capsule
		Button addCapsule = (Button) findViewById(R.id.button1);
		addCapsule.setOnClickListener(this);
		addCapsule.setText("Capture a moment");

		updateBasicInfo();
	}

	/**************************************************************** void
	 ***************************************************************/
	public void updateBasicInfo() {
		String s = Server.getUser(getPlayerId());
		String[] userInfo = s.split("\t");

		for (String str : userInfo)
			Log.v("debug", str);

		TextView name = (TextView) findViewById(R.id.text_player);
		name.setText(userInfo[0]);
		TextView stats = (TextView) findViewById(R.id.text_stats);
		stats.setText("Level " + userInfo[1]);
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

		String str = "Level: " + userInfo[1] + "\nAbility: "
		    + userInfo[2] + "\nPermissions: " + userInfo[3]
		    + "\n\nTreasures Found: " + "xxx"
		    + "\nTreasures Placed: " + "xxx";
		// for (int i = 0; i < 10; i++)
		// str += "\nMore Stats: XXX";

		return str;
	}

	/****************************************************************
	 * @return String
	 ***************************************************************/
	public String getPlayerId() {
		return "2";
	}

	/****************************************************************
	 * @see com.gvsusocnet.NavigationMenu#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	public void onClick(View v) {
		/*********LOG**********LOG*************/
		Log.println(2, "debug", "Click Switch");
		/*********LOG**********LOG*************/
		switch (v.getId()) {
		case R.id.player_info:
			showDialog(getStats(), "Detailed Stats");
			break;
		case R.id.text_stats:
			showDialog(getStats(), "Detailed Stats");
			break;
		case R.id.text_clan:
			showDialog(getStats(), "Detailed Stats");
			// show(getTreasure("4"), "Treasure Info");
			break;
		case R.id.button_achieve:
			// show("you have no trophies", "Trophies");
			gotoTreasure();
			break;
		case R.id.button1:
			Intent myIntent = new Intent().setClassName(
			    "com.gvsusocnet", "com.gvsusocnet.AddCapsule");
			startActivity(myIntent);
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
		Intent myIntent = new Intent().setClassName("com.gvsusocnet",
		    "com.gvsusocnet.TreasureActivity");
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
		updateBasicInfo();
		super.onResume();
	}
}