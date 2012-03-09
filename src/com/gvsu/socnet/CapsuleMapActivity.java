package com.gvsu.socnet;

/**
 * Note about filtering activity
 * I was unable to figure out a way to
 * have a popout box contain a slider, so
 * I made a separate activity to do the
 * filtering. (this is the way the geo-
 * caching app was doing it as well)
 * Api 11+ has a much better way to do
 * a popout thing, but we can't exactly
 * use that high of an api level...
 * 
 * to get the values that I stored,
 * create a SharedPreferences object
 * from the default shared preferences
 * and say
 * 
 * prefs.getLong(FilterActivity.START_RANGE, 0L);
 * 
 * to get the start date/time (in milliseconds)
 * and say
 * 
 * prefs.getLong(FilterActivity.END_RANGE, 0L);
 * 
 * to get the end date/time (in milliseconds)
 * I also set up a minimum rating filter too.
 * say
 * 
 * prefs.getFloat(FilterActivity.MIN_RATING, 1);
 * 
 * to get the minimum rating (it is a float value
 * because my rating bar increments by .25 stars)
 * 
 * let me know if you have any questions!
 * (or just delete this when you read it)
 */

import java.io.FileInputStream;
import java.util.Calendar;
import java.util.List;

import soc.net.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.gvsu.socnet.map.FilterActivity;

/**
 * Map to display and retrieve capsules.
 * 
 * @author Jeremy Dye
 *
 */
public class CapsuleMapActivity extends MapActivity implements
    LocationListener, OnClickListener {

	List<Overlay> mapOverlays;
	Drawable capsuleDrawable;
	Drawable userDrawable;
	DefaultOverlays itemizedoverlays;
	DefaultOverlays itemizeduseroverlay;
	GeoPoint userLocation;
	MapController mapController;
	MapView mapView;
	CapsuleOverlayItem userOverlay;
	String lastRetrieve;
	LocationManager locationManager;
	FileInputStream in;
	Criteria crit;
	long lastTimeMapCentered;
	int numNotifiedAboutPoorLocation;
	boolean warnedAboutDriving;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
//		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
//		View.inflate(this, R.layout.map, vg);
		 setContentView(R.layout.map);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
		mapView.setDrawingCacheEnabled(true);
		mapView
		    .setDrawingCacheQuality(MapView.DRAWING_CACHE_QUALITY_AUTO);
		// mapView.setStreetView(true);

		mapController = mapView.getController();

		locationManager = (LocationManager) this
		    .getSystemService(Context.LOCATION_SERVICE);

		mapOverlays = mapView.getOverlays();

		capsuleDrawable = this.getResources().getDrawable(
		    R.drawable.ic_capsule);
		itemizedoverlays = new DefaultOverlays(capsuleDrawable, this);

		userDrawable = this.getResources().getDrawable(
		    R.drawable.marker);
		itemizeduseroverlay = new DefaultOverlays(userDrawable, this);

		lastRetrieve = null;

		lastTimeMapCentered = 0L;

		// crit = new Criteria();
		// crit.setAccuracy(Criteria.ACCURACY_FINE);
		//
		// String provider = locationManager.getBestProvider(crit,
		// true);
		// Location lastLocation =
		// locationManager.getLastKnownLocation(provider);
		//
		// double lat = lastLocation.getLatitude() * 1000000.0;
		// double lng = lastLocation.getLongitude() * 1000000.0;
		//
		// userLocation = new GeoPoint((int) lat, (int) lng);
		//
		// retrieveCapsules(userLocation);

		/** Register header-footer buttons for clicks*/
		((Button) findViewById(R.id.map_settings_button))
		    .setOnClickListener(this);
		((Button) findViewById(R.id.map_filter_button))
		    .setOnClickListener(this);
		((Button) findViewById(R.id.map_capture_button))
		    .setOnClickListener(this);
		((Button) findViewById(R.id.map_profile_button))
		    .setOnClickListener(this);
		((Button) findViewById(R.id.map_map_button))
		    .setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		// requestLocationUpdates();
	}

	@Override
	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// requestLocationUpdates();
	}

	@Override
	public void onResume() {
		super.onResume();
		String info = "";
		SharedPreferences prefs = PreferenceManager
		    .getDefaultSharedPreferences(getApplicationContext());
		info += "startDate: "
		    + prefs.getLong(FilterActivity.START_RANGE, 0L);
		info += " endDate: "
		    + prefs.getLong(FilterActivity.END_RANGE, 0L);
		info += " minRating: "
		    + prefs.getFloat(FilterActivity.MIN_RATING, -1);
		Log.d("debug", "filters: " + info);
		requestLocationUpdates();
		warnedAboutDriving = false;
		numNotifiedAboutPoorLocation = 0;
	}

	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	/****************************************************************
	 * You called this a few times so I moved it into one method  
	 * @return void
	 ***************************************************************/
	protected void requestLocationUpdates() {
		// locationManager.requestLocationUpdates(
		// LocationManager.NETWORK_PROVIDER, 5 * 100, 2f, this);
		// locationManager.requestLocationUpdates(
		// LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 0, 0, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	/**
	 * Retrieve string from the server of all treasures relevant
	 * to the users current location.
	 */
	protected void retrieveCapsules(GeoPoint userLoc) {
		String lat = Double.toString(userLoc.getLatitudeE6() / 1e6);
		String lng = Double.toString(userLoc.getLongitudeE6() / 1e6);
		String retrieve = Server.getTreasure(lat, lng);
		if (lastRetrieve != retrieve || lastRetrieve == null) {
			lastRetrieve = retrieve;
			parseAndDrawCapsules(retrieve);
		}
	}

	/**
	 * Takes a long string taken from the server.  Splits
	 * it into an array of a string per capsule.  Splits
	 * each string for the capsules by the separate variables
	 * which are then used to generate capsule object.  If
	 * capsules is identical to the previously retrieved string
	 * then the method stops.
	 * @param capsules
	 */
	protected void parseAndDrawCapsules(String capsules) {
		if (capsules != "") {
			itemizedoverlays.clear();

			String[] splitCapsules = capsules.split("\\n");

			for (int i = 0; i < splitCapsules.length; i++) {
				String[] capsuleData = splitCapsules[i].split("\\t");

				if (splitCapsules[i] != "") {

					try {
						int cID = Integer.parseInt(capsuleData[0]);
						double latitude = Double
						    .parseDouble(capsuleData[2]) * 1e6;
						double longitude = Double
						    .parseDouble(capsuleData[3]) * 1e6;

						int lat = (int) latitude;
						int lng = (int) longitude;

						GeoPoint point = new GeoPoint(lat, lng);

						CapsuleOverlayItem item = new CapsuleOverlayItem(
						    point, null, null, cID);

						itemizedoverlays.addOverlay(item);
					} catch (NumberFormatException ex) {
						System.out
						    .println("Improper treasure format, encountered Number Format Exception.");
					} catch (ArrayIndexOutOfBoundsException ex) {
						System.out
						    .println("Array Index out of Bounds, problem traversing array.");
					}
				}
			}
			mapOverlays.add(itemizedoverlays);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d("debug", "accuracy = " + location.getAccuracy()
		    + " speed = " + location.getSpeed());
		if (location.getSpeed() > 25 && !warnedAboutDriving) {
			warnedAboutDriving = true;
			Toast
			    .makeText(
			        this,
			        "Caution: do not use this application while driving ",
			        Toast.LENGTH_LONG).show();
		}
		// will not accept location without a good accuracy
		if (location != null && location.getAccuracy() <= 500) {
			Log.d("debug", location.getAccuracy()
			    + " good enough accuracy");
			itemizeduseroverlay.clear();

			double lat = location.getLatitude() * 1e6;
			double lng = location.getLongitude() * 1e6;

			userLocation = new GeoPoint((int) lat, (int) lng);
			userOverlay = new CapsuleOverlayItem(userLocation,
			    "User", "User", 0);
			itemizeduseroverlay.addOverlay(userOverlay);
			mapOverlays.add(itemizeduseroverlay);

			retrieveCapsules(userLocation);
			mapOverlays.add(itemizeduseroverlay);

			// updates user's location as they move if they checked
			// the optional box in preferences
			if (PreferenceManager.getDefaultSharedPreferences(
			    getApplicationContext()).getBoolean("follow_user",
			    false)) {
				centerMap(false);
			}
			// numNotifiedAboutPoorLocation = 0;
		} else if (location != null && location.hasAccuracy()) {
			// if accuracy is bad, will let user know it is still
			// looking
			switch (numNotifiedAboutPoorLocation) {
			case 0:
				Toast.makeText(CapsuleMapActivity.this,
				    "Waiting for a better GPS position...",
				    Toast.LENGTH_LONG).show();
				break;
			case 1:
				Toast.makeText(CapsuleMapActivity.this,
				    "Still waiting for a better GPS position...",
				    Toast.LENGTH_LONG).show();
				break;
			default:
				if (numNotifiedAboutPoorLocation % 10 == 0)
					Toast.makeText(CapsuleMapActivity.this,
					    "Still waiting...", Toast.LENGTH_LONG).show();
				break;
			}
			numNotifiedAboutPoorLocation++;
		} else {
			// if gps has no accuracy, will resposition on last known
			// location
			Toast.makeText(this, "Not sure where you are...",
			    Toast.LENGTH_LONG).show();
			numNotifiedAboutPoorLocation++;
			String provider = locationManager.getBestProvider(crit,
			    true);
			Location lastLocation = locationManager
			    .getLastKnownLocation(provider);
			itemizeduseroverlay.clear();

			double lat = lastLocation.getLatitude() * 1e6;
			double lng = lastLocation.getLongitude() * 1e6;

			userLocation = new GeoPoint((int) lat, (int) lng);
			userOverlay = new CapsuleOverlayItem(userLocation,
			    "User", "User", 0);
			itemizeduseroverlay.addOverlay(userOverlay);

			retrieveCapsules(userLocation);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status,
	    Bundle extras) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capsule_add, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.center_map:
			centerMap(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/****************************************************************
	 * Centers the map on user's location as it changes, but
	 * at most every 5 seconds to avoid excessive 'jittering'
	 * @returns void
	 ***************************************************************/
	private void centerMap(boolean forceRefresh) {
		Log.d("debug", "centering map");
		long now = Calendar.getInstance().getTimeInMillis();
		if ((now > lastTimeMapCentered + 1000 || forceRefresh)
		    && userLocation != null) {
			lastTimeMapCentered = now;
			mapController.animateTo(userLocation);
			PreferenceManager
			    .getDefaultSharedPreferences(getApplicationContext())
			    .edit().putBoolean("follow_user", true);
			// mapController.setZoom(20);
		}
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_settings_button:
			Intent i = new Intent(getApplicationContext(),
			    SettingsActivity.class);
			startActivity(i);
			break;
		case R.id.map_filter_button:
			Intent i1 = new Intent(getApplicationContext(),
			    FilterActivity.class);
			startActivity(i1);
			break;
		case R.id.map_capture_button:
			Intent i2 = new Intent(getApplicationContext(),
			    AddCapsuleActivity.class);
			startActivity(i2);
			break;
		case R.id.map_profile_button:
			Intent i3 = new Intent(getApplicationContext(),
			    ProfileActivity.class);
			startActivity(i3);
			break;
		case R.id.map_map_button:
			if (mapView.isSatellite()) {
				mapView.setSatellite(false);
				((Button) findViewById(R.id.map_map_button))
				    .setBackgroundResource(R.drawable.ic_tab_map_grey);
			} else {
				mapView.setSatellite(true);
				((Button) findViewById(R.id.map_map_button))
				    .setBackgroundResource(R.drawable.ic_tab_map_color);
			}

			break;
		}

	}
}