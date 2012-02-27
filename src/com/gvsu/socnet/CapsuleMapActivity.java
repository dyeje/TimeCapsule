package com.gvsu.socnet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.List;

import soc.net.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

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
	CapsuleOverlayItem userOverlay;
	String lastRetrieve;
	LocationManager locationManager;
	Uri fileUri;
	FileInputStream in;
	BufferedInputStream buf;
	Bitmap bMap;
	long lastTimeMapCentered;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// I am trying to get the header and footer around the map...
		// ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		// ViewGroup.inflate(this, R.layout.map, vg);

		setContentView(R.layout.map);
		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		mapController = mapView.getController();

		locationManager = (LocationManager) this
		    .getSystemService(Context.LOCATION_SERVICE);

		mapOverlays = mapView.getOverlays();

		capsuleDrawable = this.getResources().getDrawable(
		    R.drawable.androidmarker);
		itemizedoverlays = new DefaultOverlays(capsuleDrawable, this);

		userDrawable = this.getResources().getDrawable(
		    R.drawable.marker);
		itemizeduseroverlay = new DefaultOverlays(userDrawable, this);

		lastRetrieve = null;

		lastTimeMapCentered = 0L;

		// Set up header-footer buttons
		Button btnBack = (Button) findViewById(R.id.map_back_button);
		btnBack.setOnClickListener(this);
		Button btnCapture = (Button) findViewById(R.id.map_capture_button);
		btnCapture.setOnClickListener(this);
		Button btnProfile = (Button) findViewById(R.id.map_profile_button);
		btnProfile.setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

	@Override
	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	/**
	 * Retrieve string from the server of all treasures relevant
	 * to the users current location.
	 */
	protected void retrieveCapsules(GeoPoint userLoc) {
		String lat = Double
		    .toString(userLoc.getLatitudeE6() / 1000000.0);
		String lng = Double
		    .toString(userLoc.getLongitudeE6() / 1000000.0);
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
						    .parseDouble(capsuleData[2]) * 1000000.0;
						double longitude = Double
						    .parseDouble(capsuleData[3]) * 1000000.0;

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
		if (location != null) {
			itemizeduseroverlay.clear();

			double lat = location.getLatitude() * 1000000.0;
			double lng = location.getLongitude() * 1000000.0;

			userLocation = new GeoPoint((int) lat, (int) lng);
			userOverlay = new CapsuleOverlayItem(userLocation,
			    "User", "User", 0);
			itemizeduseroverlay.addOverlay(userOverlay);
			mapOverlays.add(itemizeduseroverlay);

			retrieveCapsules(userLocation);
			mapOverlays.add(itemizeduseroverlay);
		} else {
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = locationManager.getBestProvider(crit,
			    true);
			Location lastLocation = locationManager
			    .getLastKnownLocation(provider);
			itemizeduseroverlay.clear();

			double lat = lastLocation.getLatitude() * 1000000.0;
			double lng = lastLocation.getLongitude() * 1000000.0;

			userLocation = new GeoPoint((int) lat, (int) lng);
			userOverlay = new CapsuleOverlayItem(userLocation,
			    "User", "User", 0);
			itemizeduseroverlay.addOverlay(userOverlay);

			retrieveCapsules(userLocation);
		}
		centerMap(false);
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
		case R.id.create_capsule:
			Intent i = new Intent(getBaseContext(), AddCapsule.class);
			startActivity(i);
			return true;
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
		long now = Calendar.getInstance().getTimeInMillis();
		if (now > lastTimeMapCentered + 5000 || forceRefresh) {
			lastTimeMapCentered = now;
			mapController.animateTo(userLocation);
			mapController.setZoom(20);
		}
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_back_button:
			finish();
			break;
		case R.id.map_capture_button:
			break;
		case R.id.map_profile_button:
			break;
		case R.id.map_map_button:
			break;
		}

	}
}