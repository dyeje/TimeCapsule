package com.gvsu.socnet.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.gvsu.socnet.data.AsyncCallbackListener;
import com.gvsu.socnet.data.AsyncDownloader;
import com.gvsu.socnet.map.CapsuleMapActivity;
import soc.net.R;

import java.io.File;
import java.net.URISyntaxException;

/**
 * *************************************************************
 * com.gvsusocnet.AddCapsule
 *
 * @version 1.0
 *          *************************************************************
 */
public class AddCapsuleActivity extends Activity implements OnClickListener, LocationListener, AsyncCallbackListener {

  private String TAG = "AddCapActivity";

  private Button capture, cancel, addPicture, addAudio, addVideo, addDocument;
  private final int PICK_PIC = 0, PICK_AUD = 1, PICK_VID = 2, PICK_DOC = 3;
  private EditText name, content, description;
  private LocationManager locationManager;
  private Location userLocation = CapsuleMapActivity.userLocation;
  private static Context mContext;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.add_capsule);

    mContext = getApplicationContext();

    capture = (Button) findViewById(R.id.btn_capture);
    capture.setOnClickListener(this);
    cancel = (Button) findViewById(R.id.btn_cancel);
    cancel.setOnClickListener(this);
    addPicture = (Button) findViewById(R.id.btn_add_picture);
    addPicture.setOnClickListener(this);
    addAudio = (Button) findViewById(R.id.btn_add_audio);
    addAudio.setOnClickListener(this);
    addVideo = (Button) findViewById(R.id.btn_add_video);
    addVideo.setOnClickListener(this);
    addDocument = (Button) findViewById(R.id.btn_add_document);
    addDocument.setOnClickListener(this);

    name = (EditText) findViewById(R.id.text_new_capsule_name);
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
          String userId = getSharedPreferences(LoginActivity.PROFILE, 0).getString("player_id", "");

          new AsyncDownloader().execute(
              new AsyncDownloader.Payload(
                  AsyncDownloader.NEWCAPSULE, new Object[]{
                  AddCapsuleActivity.this, new Object[]{
                  userId,
                  Double.toString(userLocation.getLatitude()),
                  Double.toString(userLocation.getLongitude()),
                  name.toString(),
                  description
              }
              }
              )
          );
        } else {
          Toast.makeText(getApplicationContext(), "Unable to determine location", Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.btn_cancel:
        finish();
        break;
      case R.id.btn_add_picture:
        Log.i(TAG, "choosing a picture");
        Intent choosePicIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(choosePicIntent, PICK_PIC);
        break;
      case R.id.btn_add_audio:
        Log.i(TAG, "choosing audio");
//      Intent chooseAudIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        Intent chooseAudIntent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        chooseAudIntent.setType("audio/*");
        startActivityForResult(chooseAudIntent, PICK_AUD);
        break;
      case R.id.btn_add_video:
        Log.i(TAG, "choosing a video");
        Intent chooseVidIntent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        chooseVidIntent.setType("video/*");
        startActivityForResult(chooseVidIntent, PICK_VID);
        break;
      case R.id.btn_add_document:
        Log.i(TAG, "choosing a document");
        Intent chooseDocIntent = new Intent(Intent.ACTION_GET_CONTENT, Uri.fromFile(new File("/sdcard")));
        chooseDocIntent.setType("*/*");
        chooseDocIntent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
          startActivityForResult(/*Intent.createChooser(*/chooseDocIntent/*), "Select a Document")*/, PICK_DOC);
        } catch (android.content.ActivityNotFoundException e) {
          Toast.makeText(this, "Please install a File Manager", Toast.LENGTH_LONG).show();
        }
        break;
    }
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    boolean shouldUpload = false;
    String userId = getSharedPreferences(LoginActivity.PROFILE, 0).getString("player_id", "");
    String filePath = "";

    switch (requestCode) {
      case PICK_PIC:
        if (resultCode == RESULT_OK) {
          Uri uri = data.getData();
          Log.i(TAG, "Picture Uri: " + uri);
          try {
            String path = getFilePath(this, uri);
            Log.i(TAG, "Picture path: " + path);
            if (path == null) {
              Toast.makeText(this, "Choose a different file!", Toast.LENGTH_SHORT).show();
            } else {

              shouldUpload = true;
              filePath = path;
            }
          } catch (URISyntaxException e) {
            e.printStackTrace();
          }
        }
        break;
      case PICK_AUD:
        if (resultCode == RESULT_OK) {
          Uri uri = data.getData();
          Log.d(TAG, "Audio Uri: " + uri.toString());
          try {
            String path = getFilePath(this, uri);
            Log.d(TAG, "Audio Path: " + path);
            if (path == null) {
              Toast.makeText(this, "Choose a different file!", Toast.LENGTH_SHORT).show();
            } else {
              shouldUpload = true;
              filePath = path;
            }
          } catch (URISyntaxException e) {
            e.printStackTrace();
          }
        }
        break;
      case PICK_VID:
        if (resultCode == RESULT_OK) {

          Uri uri = data.getData();
          Log.d(TAG, "Video Uri: " + uri.toString());
          try {
            String path = getFilePath(this, uri);
            Log.d(TAG, "Video Path: " + path);
            if (path == null) {
              Toast.makeText(this, "Choose a different file!", Toast.LENGTH_SHORT).show();
            } else {
              shouldUpload = true;
              filePath = path;
            }
          } catch (URISyntaxException e) {
            e.printStackTrace();
          }
        }
        break;
      case PICK_DOC:
        if (resultCode == RESULT_OK) {
          Uri uri = data.getData();
          Log.d(TAG, "Doc Uri: " + uri.toString());
          try {
            String path = getFilePath(this, uri);
            Log.d(TAG, "Doc Path: " + path);
            if (path == null) {
              Toast.makeText(this, "Choose a different file!", Toast.LENGTH_SHORT).show();
            } else {
              shouldUpload = true;
              filePath = path;
            }
          } catch (URISyntaxException e) {
            e.printStackTrace();
          }
        }
        break;
    }

    new AsyncDownloader().execute(
        new AsyncDownloader.Payload(
            AsyncDownloader.UPLOADFILE, new Object[]{
            AddCapsuleActivity.this, new Object[]{
            userId,
            filePath
        }
        }
        )
    );
  }

  public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
    if ("content".equalsIgnoreCase(uri.getScheme())) {
      String[] projection = {"_data"};
      Cursor cursor = null;

      try {
        cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
            .getColumnIndexOrThrow("_data");
        if (cursor.moveToFirst()) {
          return cursor.getString(column_index);
        }
      } catch (Exception e) {
        // Eat it
      }
    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }

  @Override
  public void onLocationChanged(Location location) {
    if (location != null) {
      userLocation = location;
    }
  }

  /**
   * *************************************************************
   *
   * @param arg0 *************************************************************
   * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
   */
  @Override
  public void onProviderDisabled(String arg0) {

  }

  /**
   * *************************************************************
   *
   * @param arg0 *************************************************************
   * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
   */
  @Override
  public void onProviderEnabled(String arg0) {

  }

  /**
   * *************************************************************
   *
   * @param arg0
   * @param arg1
   * @param arg2 *************************************************************
   * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
   */
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

  public static Context stealContext() {
    return mContext;
  }

  public void asyncSuccess(String[] results) {
    int request = Integer.parseInt(results[0]);
    if (request == AsyncDownloader.NEWCAPSULE) {
      Toast.makeText(this, "Time Capsule Saved Successfully", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  public void asyncFailure(String[] results) {
    new AlertDialog.Builder(this)
        .setTitle("Internet Error (" + results[1] + ")[" + results[0] + "]{ID-10-T}")
        .setMessage("Sorry, we couldn't save your Time Capsule. Please try that again...")
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        })
        .show();
  }
}
