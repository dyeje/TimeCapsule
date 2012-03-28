package com.gvsu.socnet.map;

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
import com.gvsu.socnet.data.Server;
import com.gvsu.socnet.user.AddCapsuleActivity;
import com.gvsu.socnet.user.LoginActivity;
import com.gvsu.socnet.user.ProfileActivity;
import com.gvsu.socnet.user.SettingsActivity;

/**
 * Map to display and retrieve capsules.
 * 
 * @author Jeremy Dye
 *
 */
public class CapsuleMapActivity extends MapActivity implements LocationListener, OnClickListener
{

	List<Overlay> mapOverlays;
	Drawable capsuleDrawable;
	Drawable userDrawable;
	CapsuleOverlays itemizedoverlays;
	UserOverlay user;
	public static Location userLocation;
	MapController mapController;
	MapView mapView;
	String lastRetrieve;
	LocationManager locationManager;
	Criteria crit;
	long lastTimeMapCentered, lastTimeRedrawn, timeBetweenUpdates = 5000, distBetweenUpdates = 5;
	boolean warnedAboutDriving, forceRedraw, forceRecenter;
	MyLocationOverlay myLocationOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		/**************************/
		// Debug.startMethodTracing("map_create");
		/**************************/

		// makes sure user is logged in otherwise kicks them to login
		// screen
		if (getSharedPreferences("profile", 0).getString("player_id", "-1").equals("-1"))
		{
			finish();
			Intent i = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(i);
		}

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(true);

		mapController = mapView.getController();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		mapOverlays = mapView.getOverlays();
		capsuleDrawable = this.getResources().getDrawable(R.drawable.ic_capsule);
		itemizedoverlays = new CapsuleOverlays(capsuleDrawable, this);
		lastRetrieve = null;
		lastTimeMapCentered = 0L;

		// start map at previously known location
		crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = locationManager.getBestProvider(crit, true);
		userLocation = locationManager.getLastKnownLocation(provider);
		if (userLocation == null)
			userLocation = new Location(provider);

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

		/** setup follow user button **/
		if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("follow_user", false))
		{
			((ImageView) findViewById(R.id.map_center_map_button)).setImageResource(R.drawable.center_on_user);
		} else
		{
			((ImageView) findViewById(R.id.map_center_map_button)).setImageResource(R.drawable.dont_center);
		}

		/**************************/
		// Debug.stopMethodTracing();
		/**************************/
	}

	@Override
	public void onResume()
	{
		/**************************/
		// Debug.startMethodTracing("map_resume");
		/**************************/
		super.onResume();
		requestLocationUpdates();
		warnedAboutDriving = false;
		forceRedraw = true;
		forceRecenter = true;
		/**************************/
		// Debug.stopMethodTracing();
		/**************************/
	}

	@Override
	public void onPause()
	{
		super.onPause();
		stopLocationUpdates();
	}

//	public void onBackPressed()
//	{
//		return;
//	}

	/****************************************************************
	 * You called this a few times so I moved it into one method  
	 * @return void
	 ***************************************************************/
	protected void requestLocationUpdates()
	{
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeBetweenUpdates, distBetweenUpdates, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeBetweenUpdates, distBetweenUpdates, this);
	}

	protected void stopLocationUpdates()
	{
		locationManager.removeUpdates(this);
	}

	/***************************************************************
	 * Retrieve string from the server of all treasures relevant
	 * to the users current location.
	 ***************************************************************/
	protected void retrieveCapsules()
	{
		/**************************/
		// Debug.startMethodTracing("map_retrieve");
		/**************************/

		String lat = Double.toString(userLocation.getLatitude());
		String lng = Double.toString(userLocation.getLongitude());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(prefs.getLong(FilterActivity.START_RANGE, 0L));
		String from = "";

		if (c.getTimeInMillis() != 0L)
		{
			from = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
		}

		c.setTimeInMillis(prefs.getLong(FilterActivity.END_RANGE, 0L));
		String to = "";
		if (c.getTimeInMillis() != 0L)
		{
			to = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
		}

		String minRating = (prefs.getFloat(FilterActivity.MIN_RATING, 0) + " ").substring(0, 1);

		// TODO MERGE BOTH OF THESE INTO ONE
		final String retrieveInner = Server.getCapsules(lat, lng, "1", from, to, minRating);
		final String retrieveOuter = Server.getCapsules(lat, lng, "2", from, to, minRating);
		String retrieve = retrieveInner + retrieveOuter;

		if (lastRetrieve != retrieve || lastRetrieve == null)
		{
			lastRetrieve = retrieve;
			// parseAndDrawCapsules(retrieveInner, true);
			// parseAndDrawCapsules(retrieveOuter, false);
			/** new way to update map **/
			mapView.post(new Runnable()
			{
				public void run()
				{
					parseAndDrawCapsules(retrieveInner, true);
					parseAndDrawCapsules(retrieveOuter, false);
				}
			});
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
	protected void parseAndDrawCapsules(String capsules, boolean inner)
	{
		if (inner)
			itemizedoverlays.clear();

		String[] splitCapsules = capsules.split("\\n");

		for (int i = 0; i < splitCapsules.length; i++)
		{
			String[] capsuleData = splitCapsules[i].split("\\t");

			if (splitCapsules[i] != "")
			{

				try
				{
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

				} catch (NumberFormatException ex)
				{
					System.out.println("Improper treasure format, encountered Number Format Exception.");
				} catch (ArrayIndexOutOfBoundsException ex)
				{
					System.out.println("Array Index out of Bounds, problem traversing array.");
				}
			}
		}
		mapOverlays.add(itemizedoverlays);
		mapView.invalidate();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	@Override
	public void onLocationChanged(Location location)
	{
		/**************************/
		// Debug.startMethodTracing("map_location_changed");
		/**************************/

		if (location.getSpeed() > 25 && !warnedAboutDriving)
		{
			warnedAboutDriving = true;
			Toast.makeText(this, "Caution: do not use this application while driving", Toast.LENGTH_LONG).show();
		}

		// will not accept location without a good accuracy
		if (location != null && location.getAccuracy() <= 500)
		{
			/**
			 * Conditions for redrawing capsules and user layer
			 * 1) onResume() forces redraw
			 * 2) the user has moved more than 5
			 * 3) meters, assuming a good level of accuracy haven't redrawn in the last 5 seconds
			 */
			if (forceRedraw || (userLocation.distanceTo(location) > 5 && location.getAccuracy() <= 500 && Calendar.getInstance().getTimeInMillis() - lastTimeRedrawn > timeBetweenUpdates))
			{
				forceRedraw = false;
				userLocation = location;
				mapOverlays.remove(mapOverlays.indexOf(user));
				user = new UserOverlay(toGeoPoint(userLocation));
				mapOverlays.add(user);
				// retrieveCapsules();
				/** new way to update map **/
				new Thread(new Runnable()
				{
					public void run()
					{
						retrieveCapsules();
					}
				}).start();

			}

			/**
			 * updates user's location as they move if they checked
			 * the optional box in preferences of if their location
			 * was just found and a recenter is forced
			 */
			if (forceRecenter || PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("follow_user", false))
			{
				forceRecenter = false;
				centerMap(false);
			}
		}
		/**************************/
		// Debug.stopMethodTracing();
		/**************************/
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	/****************************************************************
	 * Centers the map on user's location as it changes, but
	 * at most every 1 seconds to avoid excessive 'jittering'
	 * @returns void
	 ***************************************************************/
	private void centerMap(boolean forceRefresh)
	{
		Log.d("debug", "centering map");
		if (forceRefresh && userLocation != null)
		{
			mapController.animateTo(toGeoPoint(userLocation));
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("follow_user", true).commit();
		}
	}

	
	/****************************************************************
	 * Centers the map on user location when search button pressed
	 ***************************************************************/
	@Override
	public boolean onSearchRequested()
	{
		centerMap(true);
		return false;
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
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
			if (mapView.isSatellite())
			{
				mapView.setSatellite(false);
				((Button) findViewById(R.id.map_map_button)).setBackgroundResource(R.drawable.ic_tab_map_grey);
			} else
			{
				mapView.setSatellite(true);
				((Button) findViewById(R.id.map_map_button)).setBackgroundResource(R.drawable.ic_tab_map_color);
			}
			break;
		case R.id.map_center_map_button:
			if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("follow_user", false))
			{
				centerMap(true);
				int zoomLvl = mapView.getZoomLevel();
				if (zoomLvl <= 10)
				{
					while (zoomLvl <= 15)
					{
						zoomLvl++;
						mapController.zoomIn();
					}
				}
				Toast.makeText(getApplicationContext(), "Following", Toast.LENGTH_SHORT).show();
				((ImageView) findViewById(R.id.map_center_map_button)).setImageResource(R.drawable.center_on_user);

			} else
			{
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("follow_user", false).commit();
				Toast.makeText(getApplicationContext(), "Not Following", Toast.LENGTH_SHORT).show();
				((ImageView) findViewById(R.id.map_center_map_button)).setImageResource(R.drawable.dont_center);
			}
			if (userLocation == null)
			{
				Toast.makeText(this, "Waiting for GPS Location...", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.map_zoom_in_button:
			mapController.zoomIn();
			break;
		case R.id.map_zoom_out_button:
			mapController.zoomOut();
			break;
		case R.id.map_notsurewhattouseitfor_button:
			centerMap(true);
			break;
		}

	}

	/****************************************************************
	 * @param location user's location
	 * @return GeoPoint the lat/lng GeoPoint of user
	 * @WhyHaveThisMethod
	 * This method is just a handy way to quickly convert
	 * from the userLocation member field to a GeoPoint
	 * which is helpful when communicating with the server
	 ***************************************************************/
	private GeoPoint toGeoPoint(Location location)
	{
		if (location != null)
		{
			return new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
		} else
		{
			return new GeoPoint(0, 0);
		}
	}
}