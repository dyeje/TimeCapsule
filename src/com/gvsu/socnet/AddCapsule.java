package com.gvsu.socnet;

import java.util.Calendar;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

import soc.net.R;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/****************************************************************
 * com.gvsusocnet.AddCapsule
 * 
 * @version 1.0
 ***************************************************************/
public class AddCapsule extends Activity implements OnClickListener,
    LocationListener {

	private Button capture, cancel;
	private EditText name, content, description;
	private LocationManager locationManager;
	private GeoPoint userLocation;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_capsule);

		capture = (Button) findViewById(R.id.btn_capture);
		capture.setOnClickListener(this);
		cancel = (Button) findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(this);

		name = (EditText) findViewById(R.id.text_new_capsule_name);
		// content = (EditText)
		// findViewById(R.id.text_new_capsule_content);
		description = (EditText) findViewById(R.id.text_new_capsule_description);

		locationManager = (LocationManager) this
		    .getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_capture:

			String lat = Double
			    .toString(userLocation.getLatitudeE6() / 1000000.0);
			String lng = Double.toString(userLocation
			    .getLongitudeE6() / 1000000.0);

			String name = "";
			char[] nam = this.name.getText().toString().toCharArray();
			for (int i = 0; i < nam.length; i++) {
				if (nam[i] == ' ')
					name += "_";
				else
					name += nam[i];
			}

			String description = "";
			char[] descript = this.description.getText().toString()
			    .toCharArray();
			for (int i = 0; i < descript.length; i++) {
				if (descript[i] == ' ')
					description += "_";
				else
					description += descript[i];
			}

			Server.newCapsule(lat, lng, name.toString(), description,
			    0, Calendar.getInstance().getTimeInMillis());
			finish();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {

			double lat = location.getLatitude() * 1000000.0;
			double lng = location.getLongitude() * 1000000.0;

			userLocation = new GeoPoint((int) lat, (int) lng);

		} else {
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = locationManager.getBestProvider(crit,
			    true);
			Location lastLocation = locationManager
			    .getLastKnownLocation(provider);
			if (lastLocation != null) {
				double lat = lastLocation.getLatitude() * 1000000.0;
				double lng = lastLocation.getLongitude() * 1000000.0;

				userLocation = new GeoPoint((int) lat, (int) lng);
			}
		}
	}

	/****************************************************************
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 * @param arg0
	 ***************************************************************/
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	/****************************************************************
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 * @param arg0
	 ***************************************************************/
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	/****************************************************************
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 ***************************************************************/
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

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

	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
	}

	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
	}

}
