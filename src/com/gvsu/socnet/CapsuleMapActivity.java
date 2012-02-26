package com.gvsu.socnet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import soc.net.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Map to display and retrieve capsules.
 * 
 * @author Jeremy Dye
 *
 */
public class CapsuleMapActivity extends MapActivity implements
    LocationListener {

	List<Overlay> mapOverlays;
	Drawable capsuleDrawable;
	Drawable userDrawable;
	DefaultOverlays itemizedoverlays;
	DefaultOverlays itemizeduseroverlay;
	GeoPoint userLocation;
	MapController mapController;
	OverlayItem userOverlay;
	String lastRetrieve;
	LocationManager locationManager;
	Uri fileUri;
	FileInputStream in;
	BufferedInputStream buf;
	Bitmap bMap;

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
	}

	public void onStart() {
		super.onStart();
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
	}

	public void onRestart() {
		super.onRestart();
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

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
						int tID = Integer.parseInt(capsuleData[0]);
						double latitude = Double
						    .parseDouble(capsuleData[1]) * 1000000.0;
						double longitude = Double
						    .parseDouble(capsuleData[2]) * 1000000.0;

						int lat = (int) latitude;
						int lng = (int) longitude;

						GeoPoint point = new GeoPoint(lat, lng);

						CapsuleOverlayItem item = new CapsuleOverlayItem(
						    point, null, null, tID);

						itemizedoverlays.addOverlay(item);
					} catch (NumberFormatException ex) {
						System.out.println
							("Improper treasure format, encountered Number Format Exception.");
					} catch (ArrayIndexOutOfBoundsException ex) {
						System.out.println
							("Array Index out of Bounds, problem traversing array.");
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
			userOverlay = new OverlayItem(userLocation, "User", "User");
			itemizeduseroverlay.addOverlay(userOverlay);
			mapOverlays.add(itemizeduseroverlay);

			retrieveCapsules(userLocation);
			mapOverlays.add(itemizeduseroverlay);
		} else {
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = locationManager.getBestProvider(crit, true);
			Location lastLocation = locationManager
			    .getLastKnownLocation(provider);
			itemizeduseroverlay.clear();

			double lat = lastLocation.getLatitude() * 1000000.0;
			double lng = lastLocation.getLongitude() * 1000000.0;

			userLocation = new GeoPoint((int) lat, (int) lng);
			userOverlay = new OverlayItem(userLocation, "User", "User");
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
		case R.id.create_capsule:
			Intent i = new Intent(getBaseContext(), AddCapsule.class);
			startActivity(i);
			return true;
		case R.id.center_map:
			mapController.animateTo(userLocation);
			mapController.setZoom(20);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}