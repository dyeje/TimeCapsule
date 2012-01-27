package com.gvsu.socnet;

import java.util.List;

import soc.net.R;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

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
}