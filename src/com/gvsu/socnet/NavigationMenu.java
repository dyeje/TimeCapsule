/** NavigationMenu.java */
package com.gvsu.socnet;

import soc.net.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/****************************************************************
 * com.gvsusocnet.NavigationMenu
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class NavigationMenu extends Activity implements
    OnClickListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.headerfooter);

		Button btnPirate = (Button) findViewById(R.id.pirate_button);
		btnPirate.setOnClickListener(this);
		Button btnMap = (Button) findViewById(R.id.map_button);
		btnMap.setOnClickListener(this);
	}

	/****************************************************************
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * @param menu
	 * @return
	 ***************************************************************/
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nav_menu, menu);
		return true;
	}

	/****************************************************************
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * @param item
	 * @return
	 ***************************************************************/
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		switch (item.getItemId()) {
		case R.id.goto_profile:
			if (this.getClass() != ProfileActivity.class)
				result = gotoProfile();
			break;
		case R.id.goto_map:
//			if (getBaseContext().getClass() != MyMapActivity.class)
				result = gotoMap();
			break;
		case R.id.goto_friends:
			if (this.getClass() != FriendsActivity.class)
				result = gotoFriends();
			break;
		case R.id.goto_journal:
			if (this.getClass() != JournalActivity.class)
				result = gotoJournal();
			break;
		case R.id.goto_settings:
			// if (this.getClass() != SettingsActivity.class)
			result = gotoSettings();
			break;
		default:
			result = super.onOptionsItemSelected(item);
		}
		Log.println(3, "debug", "Page change "
		    + ((result) ? "successful" : "unsuccessful"));
		return result;
	}

	private boolean gotoProfile() {
		Intent myIntent = new Intent(getBaseContext(), ProfileActivity.class);
		startActivity(myIntent);
		return true;
	}

	private boolean gotoMap() {
		Intent myIntent = new Intent(getBaseContext(), CapsuleMapActivity.class);
		startActivity(myIntent);
		return true;
	}

	private boolean gotoFriends() {
		Intent myIntent = new Intent(getBaseContext(), FriendsActivity.class);
		startActivity(myIntent);
		return true;
	}

	private boolean gotoJournal() {
		Intent myIntent = new Intent(getBaseContext(), JournalActivity.class);
		startActivity(myIntent);
		return true;
	}

	private boolean gotoSettings() {
		Intent myIntent = new Intent(getBaseContext(), SettingsActivity.class);
		startActivity(myIntent);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (this.getClass() == ProfileActivity.class) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
			    this);
			builder
			    .setMessage("Are you sure you want to exit?")
			    .setCancelable(false)
			    .setPositiveButton("Yes",
			        new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog,
				            int id) {
					        Log.println(3, "debug", "App Closing");
					        finish();
				        }
			        })
			    .setNegativeButton("No",
			        new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog,
				            int id) {
					        Log.println(3, "debug",
					            "App Close Canceled");
					        dialog.cancel();
				        }
			        });
			AlertDialog closeAlert = builder.create();
			closeAlert.show();
		} else
			super.onBackPressed();
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	public void onClick(View v) {
		Log.v("debug", "Button Clicked");
		switch (v.getId()) {
		case R.id.pirate_button:
			Toast.makeText(getApplicationContext(), "Pirate",
			    Toast.LENGTH_SHORT).show();
			break;
		case R.id.map_button:
			Toast.makeText(getApplicationContext(), "Map",
			    Toast.LENGTH_SHORT).show();
			gotoMap();
			break;
		default:
			break;
		}

	}
}
