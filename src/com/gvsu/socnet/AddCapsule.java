package com.gvsu.socnet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import soc.net.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

/****************************************************************
 * com.gvsusocnet.AddCapsule
 * 
 * @version 1.0
 ***************************************************************/
public class AddCapsule extends Activity implements OnClickListener,
    LocationListener {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private Button capture, cancel, addpicture;
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
		addpicture = (Button)findViewById(R.id.btn_add_picture);
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
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
		case R.id.btn_add_picture:
			captureImage();
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
	
	
	public void captureImage() {
	    // create Intent to take a picture and return control to the calling application
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

	    // start the image capture Intent
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	    
	    /*try {
	    	//File file = new File(fileUri.toString())
	    	in = new FileInputStream(fileUri.toString());
            buf = new BufferedInputStream(in);
            bMap = BitmapFactory.decodeStream(buf);
            if (in != null)
             in.close();
            if (buf != null)
             buf.close();
        } catch (Exception e) {
            Log.e("Error reading file", e.toString());
        }
	    
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	    
	    bMap.compress(CompressFormat.JPEG, 0, bos);
	    
	    String data = null;
	    try {
			data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT));
		    data += "&" + URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode("key=30b2407b8988775ad0f9e9339cfb4ddd", "UTF-8");
		    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode("GVSUSOCNETHOLYBALLS", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    try {
	    URL url = new URL("http://api.imgur.com/2/upload");
	    URLConnection conn = url.openConnection();
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(data);
	    wr.flush();
	    } catch (MalformedURLException URLex) {
	    	
	    	
	    } catch (IOException ex) {
	    	
	    }*/
	    	
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "TimeCapsule");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	    	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	            // Image captured and saved to fileUri specified in the Intent
	            
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            // Image capture failed, advise user
	        }
	    }

	    if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	            // Video captured and saved to fileUri specified in the Intent
	            Toast.makeText(this, "Video saved to:\n" +
	                     data.getData(), Toast.LENGTH_LONG).show();
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        } else {
	            // Video capture failed, advise user
	        }
	    }
	}

}
