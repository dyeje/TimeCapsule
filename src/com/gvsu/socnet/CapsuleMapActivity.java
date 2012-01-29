package com.gvsu.socnet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import soc.net.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Map to display and retrieve capsules.
 * 
 * @author Jeremy Dye
 *
 */
public class CapsuleMapActivity extends MapActivity implements LocationListener{

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	List<Overlay> capsuleOverlays;
	List<Overlay> userOverlays;
	Drawable capsuleDrawable;
	Drawable userDrawable;
	DefaultOverlays itemizedoverlays;
	DefaultOverlays itemizeduseroverlay;
	GeoPoint userLocation;
	OverlayItem userOverlay;
	String lastRetrieve;
	LocationManager locationManager;
	Uri fileUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        capsuleOverlays = mapView.getOverlays();
        userOverlays = mapView.getOverlays();
        
        capsuleDrawable = this.getResources().getDrawable(R.drawable.androidmarker);
        itemizedoverlays = new DefaultOverlays(capsuleDrawable, this);
        
        userDrawable = this.getResources().getDrawable(R.drawable.marker);
        itemizeduseroverlay = new DefaultOverlays(userDrawable, this);
        
        lastRetrieve = null;
    }
    
    public void onStart() {
    	super.onStart();
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    
    public void onStop() {
    	super.onStop();
    	locationManager.removeUpdates(this);
    }
    
    public void onRestart() {
    	super.onRestart();
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    
    public void onResume() {
    	super.onResume();
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
    	String lat = Double.toString(userLoc.getLatitudeE6() / 1000000.0);
    	String lng = Double.toString(userLoc.getLongitudeE6() / 1000000.0);
    	String retrieve = Server.getTreasure(lat, lng);
    	if(lastRetrieve != retrieve || lastRetrieve == null) {
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
    	if(capsules != "") {
    		itemizedoverlays.clear();
    		capsuleOverlays.clear();

    		String[] splitCapsules = capsules.split("\\n");

    		for(int i = 0; i < splitCapsules.length; i++) {
    			String[] capsuleData = splitCapsules[i].split("\\t");
    			
    			if(capsuleData[i] != "") {

    				int tID = Integer.parseInt(capsuleData[0]);
    				double latitude = Double.parseDouble(capsuleData[1]) * 1000000.0;
    				double longitude = Double.parseDouble(capsuleData[2]) * 1000000.0;

    				int lat = (int) latitude;
    				int lng = (int) longitude;

    				GeoPoint point = new GeoPoint(lat, lng);

    				CapsuleOverlayItem item = new CapsuleOverlayItem(point, null, null, tID);

    				itemizedoverlays.addOverlay(item);
    			}
    		}
    		capsuleOverlays.add(itemizedoverlays);
    	}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		if(location != null) {
			itemizeduseroverlay.clear();
			userOverlays.clear();

			double lat = location.getLatitude() * 1000000.0;
			double lng = location.getLongitude() * 1000000.0;
			
			userLocation = new GeoPoint((int) lat, (int) lng);
			userOverlay = new OverlayItem(userLocation, "User", "User");
			itemizeduseroverlay.addOverlay(userOverlay);
			userOverlays.add(itemizeduseroverlay);
			
			retrieveCapsules(userLocation);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
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
	        //launch the camera
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void captureImage() {
	    // create Intent to take a picture and return control to the calling application
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

	    // start the image capture Intent
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
	            Toast.makeText(this, "Image saved to:\n" +
	                     data.getData(), Toast.LENGTH_LONG).show();
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