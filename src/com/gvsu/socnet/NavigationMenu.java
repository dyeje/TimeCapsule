/** NavigationMenu.java */
package com.gvsu.socnet;

import soc.net.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
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
public abstract class NavigationMenu extends Activity implements
    OnClickListener {

	/** Class<? extends Object> thisClass Differentiates each different activity that extends NavigationMenu */
	// protected Class<? extends Object> thisClass;

	protected Button btnMenu, btnCapture, btnProfile, btnMap;

	protected abstract boolean gotoProfile();
	protected abstract boolean gotoMap();
	protected abstract void refresh();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.headerfooter);

		btnMenu = (Button) findViewById(R.id.menu_button);
		btnMenu.setOnClickListener(this);
		btnCapture = (Button) findViewById(R.id.capture_button);
		btnCapture.setOnClickListener(this);
		btnProfile = (Button) findViewById(R.id.profile_button);
		btnProfile.setOnClickListener(this);
		btnMap = (Button) findViewById(R.id.map_button);
		btnMap.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/****************************************************************
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * @param menu
	 * @return
	 ***************************************************************/
	@Override
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh();
			break;
		case R.id.goto_capsule:
			Intent in = new Intent(getApplicationContext(),
			    CapsuleActivity.class);
			in.putExtra("cID", PreferenceManager
			    .getDefaultSharedPreferences(getApplicationContext())
			    .getString("capsule_id", "0"));
			startActivity(in);
			result = true;
			break;
		default:
			result = super.onOptionsItemSelected(item);
		}
		Log.println(3, "debug", "Page change "
		    + ((result) ? "successful" : "unsuccessful"));
		return result;
	}
//	private boolean gotoProfile() {
//		// Log.d("debug", thisClass.toString());
//		// if (thisClass != ProfileActivity.class) {
//		Intent myIntent = new Intent(getBaseContext(),
//		    ProfileActivity.class);
//		startActivity(myIntent);
//		// }
//		return true;
//	}

//	private boolean gotoMap() {
//		// if (thisClass != CapsuleMapActivity.class) {
//		Intent myIntent = new Intent(getBaseContext(),
//		    CapsuleMapActivity.class);
//		startActivity(myIntent);
//		// }
//		return true;
//	}

	private boolean gotoSettings() {
		Intent myIntent = new Intent(getBaseContext(),
		    SettingsActivity.class);
		startActivity(myIntent);
		return true;
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v) {
//		Log.v("debug", "Button Clicked");
		switch (v.getId()) {
		case R.id.menu_button:
			Intent intent = new Intent(getApplicationContext(),
			    SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.capture_button:
			Toast.makeText(getApplicationContext(),
			    "Capture a Moment", Toast.LENGTH_SHORT).show();
			Intent myIntent = new Intent(this, AddCapsule.class);
			startActivity(myIntent);
			break;
		case R.id.profile_button:
			gotoProfile();
			break;
		case R.id.map_button:
			gotoMap();
			break;
		default:
			break;
		}

	}
}
