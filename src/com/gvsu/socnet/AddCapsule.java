package com.gvsu.socnet;

import java.util.Calendar;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.maps.GeoPoint;

/****************************************************************
 * com.gvsusocnet.AddCapsule
 * 
 * @version 1.0
 ***************************************************************/
public class AddCapsule extends Activity implements OnClickListener,
    LocationListener {

	private Button capture, cancel, addpicture;
	private EditText name, content, description;
	private LocationManager locationManager;
	private GeoPoint userLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_capsule);

		capture = (Button) findViewById(R.id.btn_capture);
		capture.setOnClickListener(this);
		cancel = (Button) findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(this);
		addpicture = (Button) findViewById(R.id.btn_add_picture);
		addpicture.setOnClickListener(this);

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

		this.getWindow()
		    .setSoftInputMode(
		        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
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
		case R.id.btn_add_picture:
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
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	/****************************************************************
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 * @param arg0
	 ***************************************************************/
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	/****************************************************************
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 ***************************************************************/
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

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

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
	}
}
