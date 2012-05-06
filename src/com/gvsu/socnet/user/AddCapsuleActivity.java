package com.gvsu.socnet.user;

import soc.net.R;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gvsu.socnet.data.Server;
import com.gvsu.socnet.map.CapsuleMapActivity;

/****************************************************************
 * com.gvsusocnet.AddCapsule
 * 
 * @version 1.0
 ***************************************************************/
public class AddCapsuleActivity extends Activity implements OnClickListener, LocationListener {

	private Button capture, cancel, addpicture;
	private EditText name, content, description;
	private LocationManager locationManager;
	private Location userLocation = CapsuleMapActivity.userLocation;;

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

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_capture:

			if (userLocation != null && userLocation.getAccuracy() < 500) {

				String name = "";
				char[] nam = this.name.getText().toString().toCharArray();
				for (int i = 0; i < nam.length; i++) {
					if (nam[i] == ' ')
						name += "+";
					else
						name += nam[i];
				}

				String description = "";
				char[] descript = this.description.getText().toString().toCharArray();
				for (int i = 0; i < descript.length; i++) {
					if (descript[i] == ' ')
						description += "+";
					else
						description += descript[i];
				}

				Server.newCapsule(Double.toString(userLocation.getLatitude()), Double.toString(userLocation.getLongitude()), name.toString(), description);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "Unable to determine location", Toast.LENGTH_SHORT).show();
			}
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
			userLocation = location;
		}
	}

	/****************************************************************
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 * @param arg0
	 ***************************************************************/
	@Override
	public void onProviderDisabled(String arg0) {

	}

	/****************************************************************
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 * @param arg0
	 ***************************************************************/
	@Override
	public void onProviderEnabled(String arg0) {

	}

	/****************************************************************
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 ***************************************************************/
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

	}

	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 2f, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 2f, this);
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
