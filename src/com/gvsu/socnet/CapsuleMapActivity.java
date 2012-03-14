package com.gvsu.socnet;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.gvsu.socnet.map.FilterActivity;

/**
 * Map to display and retrieve capsules.
 * 
 * @author Jeremy Dye
 *
 */
public class CapsuleMapActivity extends MapActivity implements LocationListener, OnClickListener {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

	List<Overlay> mapOverlays;
	Drawable capsuleDrawable;
	Drawable userDrawable;
	CapsuleOverlays itemizedoverlays;
	UserOverlay user;
	// GeoPoint userLocation;
	Location userLocation;
	MapController mapController;
	MapView mapView;
	// CapsuleOverlayItem userOverlay;
	String lastRetrieve;
	LocationManager locationManager;
	FileInputStream in;
	Criteria crit;
	long lastTimeMapCentered, lastTimeRedrawn, timeBetweenUpdates = 5000;
	int numNotifiedAboutPoorLocation;
	boolean warnedAboutDriving, forceRedrawCapsules;
	MyLocationOverlay myLocationOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		/**************************/
		// Debug.startMethodTracing("map_create");
		/**************************/

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		// ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		// View.inflate(this, R.layout.map, vg);
		setContentView(R.layout.map);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(true);
		mapView.setDrawingCacheEnabled(true);
		mapView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
		// mapView.setStreetView(true);

		mapController = mapView.getController();

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		mapOverlays = mapView.getOverlays();

		capsuleDrawable = this.getResources().getDrawable(R.drawable.ic_capsule);
		itemizedoverlays = new CapsuleOverlays(capsuleDrawable, this);

		userDrawable = this.getResources().getDrawable(R.drawable.marker);
		// itemizeduseroverlay = new DefaultOverlays(userDrawable,
		// this);

		lastRetrieve = null;

		lastTimeMapCentered = 0L;

		crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);

		String provider = locationManager.getBestProvider(crit, true);
		userLocation = locationManager.getLastKnownLocation(provider);
		/*
		 * if we keep the user's location in Location form we can keep
		 * more information about them like distanceTo, bearing, etc
		 */

		// Location lastLocation =
		// locationManager.getLastKnownLocation(provider);

		// double lat = lastLocation.getLatitude() * 1000000.0;
		// double lng = lastLocation.getLongitude() * 1000000.0;

		// userLocation = new GeoPoint((int) lat, (int) lng);
		// user = new UserOverlay(userLocation);
		user = new UserOverlay(toGeoPoint(userLocation));
		mapOverlays.add(user);

		/** Register header-footer buttons for clicks*/
		((Button) findViewById(R.id.map_settings_button)).setOnClickListener(this);
		((Button) findViewById(R.id.map_filter_button)).setOnClickListener(this);
		((Button) findViewById(R.id.map_capture_button)).setOnClickListener(this);
		((Button) findViewById(R.id.map_profile_button)).setOnClickListener(this);
		((Button) findViewById(R.id.map_map_button)).setOnClickListener(this);
		((ImageView) findViewById(R.id.map_center_map_button)).setOnClickListener(this);
		((ImageView) findViewById(R.id.map_zoom_in_button)).setOnClickListener(this);
		((ImageView) findViewById(R.id.map_zoom_out_button)).setOnClickListener(this);
		((ImageView) findViewById(R.id.map_notsurewhattouseitfor_button)).setOnClickListener(this);

		/**************************/
		// Debug.stopMethodTracing();
		/**************************/
	}

	@Override
	public void onStart() {
		super.onStart();
		// requestLocationUpdates();
	}

	@Override
	public void onStop() {
		super.onStop();
		// locationManager.removeUpdates(this);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// requestLocationUpdates();
	}

	@Override
	public void onResume() {
		/**************************/
		// Debug.startMethodTracing("map_resume");
		/**************************/
		super.onResume();
		String info = "";
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		info += "startDate: " + prefs.getLong(FilterActivity.START_RANGE, 0L);
		info += " endDate: " + prefs.getLong(FilterActivity.END_RANGE, 0L);
		info += " minRating: " + prefs.getFloat(FilterActivity.MIN_RATING, -1);
		Log.d("debug", "filters: " + info);
		requestLocationUpdates();
		warnedAboutDriving = false;
		forceRedrawCapsules = true;
		numNotifiedAboutPoorLocation = 0;
		/**************************/
		// Debug.stopMethodTracing();
		/**************************/
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
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeBetweenUpdates, 5, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeBetweenUpdates, 5, this);
	}

	/**
	 * Retrieve string from the server of all treasures relevant
	 * to the users current location.
	 */
	protected void retrieveCapsules(GeoPoint userLoc) {
		/**************************/
		// Debug.startMethodTracing("map_retrieve");
		/**************************/

		String lat = Double.toString(userLoc.getLatitudeE6() / 1e6);
		String lng = Double.toString(userLoc.getLongitudeE6() / 1e6);
		// String retrieve = Server.getTreasure(lat, lng);

		// String info = "";
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		// info += "startDate: "
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(prefs.getLong(FilterActivity.START_RANGE, 0L));
		String from = "";
		if (c.getTimeInMillis() != 0L) {
			from = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
			// from = dateFormat.format(new
			// Date(c.getTimeInMillis()));
		}

		c.setTimeInMillis(prefs.getLong(FilterActivity.END_RANGE, 0L));
		String to = "";
		if (c.getTimeInMillis() != 0L) {
			to = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
			// to = dateFormat.format(new Date(c.getTimeInMillis()));
		}

		String minRating = (prefs.getFloat(FilterActivity.MIN_RATING, 0) + " ").substring(0, 1);

		String retrieveInner = Server.getCapsules(lat, lng, "1", from, to, minRating);
		String retrieveOuter = Server.getCapsules(lat, lng, "2", from, to, minRating);
		String retrieve = retrieveInner + retrieveOuter;

		if (lastRetrieve != retrieve || lastRetrieve == null) {
			lastRetrieve = retrieve;
			parseAndDrawCapsules(retrieveInner, true);
			parseAndDrawCapsules(retrieveOuter, false);
		}

		/**************************/
		// Debug.stopMethodTracing();
		/**************************/
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
	protected void parseAndDrawCapsules(String capsules, boolean inner) {
		// if (capsules != "") {
		if (inner)
			itemizedoverlays.clear();

		String[] splitCapsules = capsules.split("\\n");

		for (int i = 0; i < splitCapsules.length; i++) {
			String[] capsuleData = splitCapsules[i].split("\\t");

			if (splitCapsules[i] != "") {

				try {
					int cID;
					if (inner)
						cID = Integer.parseInt(capsuleData[0]);
					else
						cID = -1;
					double latitude = Double.parseDouble(capsuleData[2]) * 1e6;
					double longitude = Double.parseDouble(capsuleData[3]) * 1e6;

					int lat = (int) latitude;
					int lng = (int) longitude;

					GeoPoint point = new GeoPoint(lat, lng);

					CapsuleOverlayItem item = new CapsuleOverlayItem(point, null, null, cID);

					itemizedoverlays.addOverlay(item);
				} catch (NumberFormatException ex) {
					System.out.println("Improper treasure format, encountered Number Format Exception.");
				} catch (ArrayIndexOutOfBoundsException ex) {
					System.out.println("Array Index out of Bounds, problem traversing array.");
				}
			}
		}
		mapOverlays.add(itemizedoverlays);
	}

	// }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d("debug", "**********************\nlocationchangecalled\n*****************");
		/**************************/
		// Debug.startMethodTracing("map_location_changed");
		/**************************/

		Log.d("debug", "accuracy = " + location.getAccuracy() + " speed = " + location.getSpeed());
		if (location.getSpeed() > 25 && !warnedAboutDriving) {
			warnedAboutDriving = true;
			Toast.makeText(this, "Caution: do not use this application while driving ", Toast.LENGTH_LONG).show();
		}
		// will not accept location without a good accuracy
		if (location != null && location.getAccuracy() <= 1000) {
			Log.d("debug", "has some good accuracy\n********************");
			// if (location != null) {
			// Log.d("debug", location.getAccuracy() +
			// " good enough accuracy");
			// itemizeduseroverlay.clear();

			// double lat = location.getLatitude() * 1e6;
			// double lng = location.getLongitude() * 1e6;

			// userLocation = new GeoPoint((int) lat, (int) lng);
			// long now = Calendar.getInstance().getTimeInMillis();
			// if (now - lastTimeLocationUpdated >= timeBetweenUpdates
			// || userLocation == null) {
			// userLocation = location;
			// }
			if (userLocation == null) {
				userLocation = location;
			}
			// Conditions for redrawing capsules and user layer
			// *onResume() forces redraw* *the user has moved more
			// than 5 meters, assuming a good level of accuracy*
			// *haven't redrawn in the last 5 seconds*
			if (forceRedrawCapsules || (userLocation.distanceTo(location) > 5 && location.getAccuracy() <= 500 && Calendar.getInstance().getTimeInMillis() - lastTimeRedrawn > timeBetweenUpdates)) {
				forceRedrawCapsules = false;
				userLocation = location;
				Toast.makeText(this, "Redrawing", Toast.LENGTH_SHORT).show();
				// userOverlay = new
				// CapsuleOverlayItem(toGeoPoint(userLocation),
				// "User", "User", 0);
				mapOverlays.remove(mapOverlays.indexOf(user));
				user = new UserOverlay(toGeoPoint(userLocation));
				mapOverlays.add(user);
				retrieveCapsules(toGeoPoint(userLocation));
			}
			// itemizeduseroverlay.addOverlay(userOverlay);
			// mapOverlays.add(itemizeduseroverlay);
			// mapOverlays.add(user);

			// mapOverlays.add(itemizeduseroverlay);

			// updates user's location as they move if they checked
			// the optional box in preferences
			if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("follow_user", false)) {
				centerMap(false);
			}
		} else {
			Log.d("debug", "accuracy is bad\n********************");
			// if gps has no accuracy, will resposition on last known
			// location
			// Toast.makeText(this, "Not sure where you are...",
			// Toast.LENGTH_LONG).show();
			numNotifiedAboutPoorLocation++;
			String provider = locationManager.getBestProvider(crit, true);
			userLocation = locationManager.getLastKnownLocation(provider);
			// Location lastLocation =
			// locationManager.getLastKnownLocation(provider);
			// itemizeduseroverlay.clear();

			// double lat = lastLocation.getLatitude() * 1e6;
			// double lng = lastLocation.getLongitude() * 1e6;

			// userLocation = new GeoPoint((int) lat, (int) lng);
			// userOverlay = new
			// CapsuleOverlayItem(toGeoPoint(userLocation), "User",
			// "User", 0);
			// itemizeduseroverlay.addOverlay(userOverlay);

			// retrieveCapsules(toGeoPoint(userLocation));

			/**************************/
			// Debug.stopMethodTracing();
			/**************************/
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(R.menu.capsule_add, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// // Handle item selection
	// switch (item.getItemId()) {
	// case R.id.center_map:
	// centerMap(true);
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }

	/****************************************************************
	 * Centers the map on user's location as it changes, but
	 * at most every 1 seconds to avoid excessive 'jittering'
	 * @returns void
	 ***************************************************************/
	private void centerMap(boolean forceRefresh) {
		Log.d("debug", "centering map");
		long now = Calendar.getInstance().getTimeInMillis();
		if ((now > lastTimeMapCentered + 1000 || forceRefresh) && userLocation != null) {
			lastTimeMapCentered = now;
			mapController.animateTo(toGeoPoint(userLocation));
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("follow_user", true).commit();
			Log.d("debug", userLocation.getLatitude() + " " + userLocation.getLongitude());
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
			Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(i);
			break;
		case R.id.map_filter_button:
			Intent i1 = new Intent(getApplicationContext(), FilterActivity.class);
			startActivity(i1);
			break;
		case R.id.map_capture_button:
			Intent i2 = new Intent(getApplicationContext(), AddCapsuleActivity.class);
			startActivity(i2);
			break;
		case R.id.map_profile_button:
			Intent i3 = new Intent(getApplicationContext(), ProfileActivity.class);
			startActivity(i3);
			break;
		case R.id.map_map_button:
			if (mapView.isSatellite()) {
				mapView.setSatellite(false);
				((Button) findViewById(R.id.map_map_button)).setBackgroundResource(R.drawable.ic_tab_map_grey);
			} else {
				mapView.setSatellite(true);
				((Button) findViewById(R.id.map_map_button)).setBackgroundResource(R.drawable.ic_tab_map_color);
			}
			break;
		case R.id.map_center_map_button:
			if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("follow_user", false)) {
				centerMap(true);
				Toast.makeText(getApplicationContext(), "Following", Toast.LENGTH_SHORT).show();
				((ImageView) findViewById(R.id.map_center_map_button)).setImageResource(R.drawable.center_on_user);
				
			} else {
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("follow_user", false).commit();
				Toast.makeText(getApplicationContext(), "Not Following", Toast.LENGTH_SHORT).show();				
				((ImageView) findViewById(R.id.map_center_map_button)).setImageResource(R.drawable.dont_center);
			}
			break;
		case R.id.map_zoom_in_button:
			mapController.setZoom(mapView.getZoomLevel() + 1);
			break;
		case R.id.map_zoom_out_button:
			mapController.setZoom(mapView.getZoomLevel() - 1);
			break;
		case R.id.map_notsurewhattouseitfor_button:
			Toast.makeText(getApplicationContext(), "Make me do something!", Toast.LENGTH_SHORT).show();
			break;
		}

	}

	private GeoPoint toGeoPoint(Location location) {
		return new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
	}
}