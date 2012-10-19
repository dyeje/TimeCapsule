package com.gvsu.socnet.map;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import com.gvsu.socnet.data.AsyncCallbackListener;
import com.gvsu.socnet.data.AsyncDownloader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.google.android.maps.Overlay;
import com.gvsu.socnet.user.AddCapsuleActivity;
import com.gvsu.socnet.user.LoginActivity;
import com.gvsu.socnet.user.ProfileActivity;
import com.gvsu.socnet.user.SettingsActivity;
import soc.net.R;

/**
 * Map to display and retrieve capsules.
 *
 * @author Jeremy Dye
 */
public class CapsuleMapActivity extends MapActivity implements LocationListener, OnClickListener, SensorEventListener, AsyncCallbackListener {

  List<Overlay> mapOverlays;
  Drawable capsuleInnerDrawable;
  Drawable capsuleOuterDrawable;
  CapsuleOverlays innerCapsules;
  CapsuleOverlays outerCapsules;
  UserOverlay user;
  public static Location userLocation;
  MapController mapController;
  MapView mapView;
  String lastRetrieve;
  LocationManager locationManager;
  Criteria crit;
  long lastTimeMapCentered, lastTimeRedrawn, timeBetweenUpdates = 5000, distBetweenUpdates = 5;
  boolean warnedAboutDriving, forceRedraw, forceRecenter;
  SensorManager sensorManager;
  Sensor accelerometerSensor;
  Sensor magneticSensor;
  float[] accelerometerData = new float[3];
  float[] magneticData = new float[3];
  float[] rotation = new float[16];
  float[] inclination = new float[16];
  float[] orientation = new float[3];
  float bearing = 0;
  boolean rotatableUser = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    /**************************/
    // Debug.startMethodTracing("map_create");
    /**************************/

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map);

    mapView = (MapView) findViewById(R.id.mapview_thingy);
    mapView.setBuiltInZoomControls(false);
    mapView.setSatellite(true);

    mapController = mapView.getController();
    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    mapOverlays = mapView.getOverlays();
    capsuleInnerDrawable = this.getResources().getDrawable(R.drawable.ic_capsule_inner);
    capsuleOuterDrawable = this.getResources().getDrawable(R.drawable.ic_capsule_outer);
    innerCapsules = new CapsuleOverlays(capsuleInnerDrawable, this);
    outerCapsules = new CapsuleOverlays(capsuleOuterDrawable, this);
    lastRetrieve = null;
    lastTimeMapCentered = 0L;

    // start map at previously known location
    crit = new Criteria();
    crit.setAccuracy(Criteria.ACCURACY_FINE);
    String provider = locationManager.getBestProvider(crit, true);
    userLocation = locationManager.getLastKnownLocation(provider);
    if (userLocation == null)
      userLocation = new Location(provider);

    /** Register header-footer buttons for clicks*/
    ((Button) findViewById(R.id.map_settings_button)).setOnClickListener(this);
    ((Button) findViewById(R.id.map_filter_button)).setOnClickListener(this);
    ((Button) findViewById(R.id.map_capture_button)).setOnClickListener(this);
    ((Button) findViewById(R.id.map_profile_button)).setOnClickListener(this);
    ((Button) findViewById(R.id.map_map_button)).setOnClickListener(this);
    // ((ImageView) findViewById(R.id.map_center_map_button))
    // .setOnClickListener(this);
    ((ImageView) findViewById(R.id.map_zoom_in_button)).setOnClickListener(this);
    ((ImageView) findViewById(R.id.map_zoom_out_button)).setOnClickListener(this);

    /**************************/
    // Debug.stopMethodTracing();
    /**************************/
  }

  @Override
  public void onResume() {
    /**************************/
    // Debug.startMethodTracing("map_resume");
    /**************************/
    // makes sure user is logged in otherwise makes them login
    if (getSharedPreferences("profile", 0).getString("player_id", "-1").equals("-1")) {
      Intent i = new Intent(getApplicationContext(), LoginActivity.class);
      startActivity(i);
    }

    super.onResume();
    requestLocationUpdates();
    warnedAboutDriving = false;
    forceRedraw = true;

    /**************************/
    // Debug.stopMethodTracing();
    /**************************/
  }

  @Override
  public void onPause() {
    super.onPause();
    stopLocationUpdates();
  }

  protected void requestLocationUpdates() {
    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("rotate_user", false)) {
      sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotatableUser = true;
      }
      sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
      sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
    } else {
      rotatableUser = false;
    }
  }

  protected void stopLocationUpdates() {
    locationManager.removeUpdates(this);
    if (sensorManager != null) {
      sensorManager.unregisterListener(this);
    }
  }

  /**
   * ************************************************************
   * Retrieve string from the server of all treasures relevant
   * to the users current location.
   * *************************************************************
   */
  protected void retrieveCapsules() {
    Log.d("MAP", "RETRIEVING");
    /**************************/
    // Debug.startMethodTracing("map_retrieve");
    /**************************/

    String lat = Double.toString(userLocation.getLatitude());
    String lng = Double.toString(userLocation.getLongitude());

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(prefs.getLong(FilterActivity.START_RANGE, 0L));
    String from = "";

    if (c.getTimeInMillis() != 0L) {
      from = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
    }

    c.setTimeInMillis(prefs.getLong(FilterActivity.END_RANGE, 0L));
    String to = "";
    if (c.getTimeInMillis() != 0L) {
      to = c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
    }

    String minRating = (prefs.getFloat(FilterActivity.MIN_RATING, 0) + " ").substring(0, 1);


    HashMap<String, String> requestParams = new HashMap<String, String>();
    requestParams.put(AsyncDownloader.LATITUDE, Double.toString(userLocation.getLatitude()));
    requestParams.put(AsyncDownloader.LONGITUDE, Double.toString(userLocation.getLongitude()));
    requestParams.put(AsyncDownloader.FROM, from);
    requestParams.put(AsyncDownloader.TO, to);
    requestParams.put(AsyncDownloader.RATING, minRating);

    AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.RETRIEVECAPSULES, requestParams, this, getApplicationContext());

    AsyncDownloader.perform(request);
  }

  /**
   * Takes a JSON Array of time capsules from the server and displays them on the map
   *
   * @param strCapsules
   */
  protected void parseAndDrawCapsules(String strCapsules, boolean inner) {
    // TODO clean this up when we combine the inner/outer calls
    if (inner) {
      Log.i("debug", "drawing nearby capsules:");// LOG
      innerCapsules.clear();
      outerCapsules.clear();
    } else
      Log.i("debug", "drawing perimeter capsules:");// LOG

    if (!strCapsules.equals("error")) {
      JSONArray capsules = null;
      try {
        capsules = new JSONArray(strCapsules);
        // capsules.getJSONObject(0)
      } catch (JSONException e) {
        Log.e("debug", "Improper JSON (Capsules from server):\n" + strCapsules);
      }
      if (capsules != null) {
        for (int i = 0; i < capsules.length(); i++) {
          try {
            JSONObject capsule = capsules.getJSONObject(i);
            int capID;
            if (inner)
              capID = Integer.parseInt(capsule.getString("id"));
            else
              capID = -1;
            double latitude = Double.parseDouble(capsule.getString("locLat")) * 1e6;
            double longitude = Double.parseDouble(capsule.getString("locLong")) * 1e6;

            int lat = (int) latitude;
            int lng = (int) longitude;

            GeoPoint point = new GeoPoint(lat, lng);
            CapsuleOverlayItem item = new CapsuleOverlayItem(point, null, null, capID);

            if (inner) {
              Log.i("debug", "capsule " + capsule.getString("title"));// LOG
              innerCapsules.addOverlay(item);
            } else {
              Log.i("debug", "outercapsule " + capsule.getString("title"));// LOG
              outerCapsules.addOverlay(item);
            }

          } catch (JSONException e) {
            Log.e("debug", "error: " + capsules.toString() + " is not valid JSON");
            e.printStackTrace();
          } catch (NumberFormatException e) {
            Log.e("debug", e.getMessage());
          }
        }
      }
    } else {
      Toast.makeText(this, "There was an error finding the time capsules", Toast.LENGTH_LONG).show();
    }
    if (inner)
      mapOverlays.add(innerCapsules);
    else {
      mapOverlays.add(outerCapsules);
    }
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  @Override
  public void onLocationChanged(Location location) {
    /**************************/
    // Debug.startMethodTracing("map_location_changed");
    /**************************/

    if (location == null) {
      return;
    }

    if (location.getSpeed() > 25 && !warnedAboutDriving) {
      warnedAboutDriving = true;
      Toast.makeText(this, "Caution: do not use this application while driving", Toast.LENGTH_LONG).show();
    }

    Log.i("debug", "accuracy:" + location.getAccuracy());
    // will not accept location without a good accuracy
    if (location.getAccuracy() <= 1000) {
      /**
       * Conditions for redrawing capsules and user layer
       * 1) onResume() forces redraw
       * 2) the user has moved more than 5
       * 3) meters, assuming a good level of accuracy haven't redrawn in the last 5 seconds
       */
      if (forceRedraw || (userLocation.distanceTo(location) > 5 && location.getAccuracy() <= 500 && Calendar.getInstance().getTimeInMillis() - lastTimeRedrawn > timeBetweenUpdates)) {
        if (forceRedraw) {
          Log.i("debug", "redraw forced");
          innerCapsules.clear();
          outerCapsules.clear();
          forceRedraw = false;
        }
        userLocation = location;
        drawUser();
        /** new way to update map **/
        retrieveCapsules();
      }

      /**
       * centers map on user's location as they move if they checked
       * the box in preferences
       * also centers on user's location if their location
       * was just found and a recenter is forced
       */
      if (forceRecenter || PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("follow_user", false)) {
        forceRecenter = false;
        centerMap(false);
      }
    }
    /**************************/
    // Debug.stopMethodTracing();
    /**************************/
  }

  @Override
  public void onProviderDisabled(String provider) {
    Log.i("map", "oops, " + provider + " provider was disabled...");
  }

  @Override
  public void onProviderEnabled(String provider) {
    Log.i("map", "yay, " + provider + " provider was enabled!");
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    Log.i("map", provider + " provider status changed\nnew status=" + status);
  }

  /**
   * *************************************************************
   * Centers the map on user's location as it changes, but
   * at most every 1 seconds to avoid excessive 'jittering'
   *
   * @returns void
   * *************************************************************
   */
  private void centerMap(boolean forceRefresh) {
    if (userLocation == null)
      return; // can't center on a null location, get out!
    Log.i("debug", "centering map");
    Log.v("map", "0");
    if (forceRefresh) {
      Log.v("map", "1");
      mapController.animateTo(toGeoPoint(userLocation));
      PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("follow_user", true).commit();
      Log.i("debug", "search pressed, following user");
    } else {
      long now = Calendar.getInstance().getTimeInMillis();
      if ((now - lastTimeMapCentered) > timeBetweenUpdates) {
        mapController.animateTo(toGeoPoint(userLocation));
        lastTimeMapCentered = now;
      }
    }
    Log.v("map", "2");
  }

  /**
   * *************************************************************
   * Centers the map on user location when search button pressed
   * *************************************************************
   */
  @Override
  public boolean onSearchRequested() {
    Log.v("map", "-1");
    centerMap(true);
    super.onSearchRequested();
    return false;
  }

  /**
   * *************************************************************
   *
   * @param v *************************************************************
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
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
        } else {
          mapView.setSatellite(true);
        }
        break;
      case R.id.map_zoom_in_button:
        mapController.zoomIn();
        break;
      case R.id.map_zoom_out_button:
        mapController.zoomOut();
        break;
    }

  }

  public void drawUser() {
    int index = mapOverlays.indexOf(user);
    if (index != -1)
      mapOverlays.remove(mapOverlays.indexOf(user));
    user = new UserOverlay(toGeoPoint(userLocation), this, rotatableUser, bearing);
    mapOverlays.add(0, user);
  }

  /**
   * *************************************************************
   *
   * @param location user's location
   * @return GeoPoint the lat/lng GeoPoint of user
   * @WhyHaveThisMethod This method is just a handy way to quickly convert
   * from the userLocation member field to a GeoPoint
   * which is helpful when communicating with the server
   * *************************************************************
   */
  private GeoPoint toGeoPoint(Location location) {
    if (location != null) {
      return new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
    } else {
      return new GeoPoint(0, 0);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor arg0, int arg1) {
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (rotatableUser) {
      int type = event.sensor.getType();

      if (type == Sensor.TYPE_ACCELEROMETER) {
        accelerometerData = event.values;
      } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
        magneticData = event.values;
      }

      SensorManager.getRotationMatrix(rotation, null, accelerometerData, magneticData);
      SensorManager.getOrientation(rotation, orientation);

      float tempBearing = (float) Math.toDegrees(orientation[0]);

      if (Math.abs(tempBearing - bearing) > 5) {
        bearing = tempBearing;
        drawUser();
      }
    }
  }


  public void asyncDone(AsyncDownloader.Payload payload) {
    if (payload.exception == null) {
      switch (payload.taskType) {
        case AsyncDownloader.RETRIEVECAPSULES:
          String[] result = payload.result.split(AsyncDownloader.INNEROUTERSPLIT);
          mapView.invalidate();
          parseAndDrawCapsules(result[0], true);
          parseAndDrawCapsules(result[1], false);
          break;
      }
    } else {
      new AlertDialog.Builder(this)
          .setTitle(payload.exception.getMessage() + " [" + payload.taskType + "](" + payload.result + "){ID-10-T}")
          .setMessage("Sorry, we're having trouble talking to the internet. Please try that again...")
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          })
          .show();
    }
  }
}