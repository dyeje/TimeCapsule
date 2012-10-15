package com.gvsu.socnet.user;

import android.app.Activity;
import android.widget.Button;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gvsu.socnet.data.Server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import soc.net.R;

/****************************************************************
 * com.gvsusocnet.ProfileActivity
 * @version 1.0
 ***************************************************************/
public class ProfileActivity extends Activity implements OnClickListener {

  // private SharedPreferences prefs;
  public final String PROFILE = "profile", PLAYER_ID = "player_id";
  private final String TAB = "\t";
  private final String NO_CONN = "No Network Connection";
  private final String NO_CONN_INFO = "Many features of this app will not work without an internet connection";
  private final String PROFILE_NOT_RETRIEVED = "Sorry, this profile could not be retrieved :(";
  private TextView username, name, location, gender, age, interests, aboutme;
  private OnSharedPreferenceChangeListener listener;
  private boolean viewing;
    protected Button btnMenu, btnCapture, btnProfile, btnMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // makes sure user is logged in, otherwise kicks them out to
    // the login screen
    if (getSharedPreferences(PROFILE, 0).getString(PLAYER_ID, "-1").equals("-1")) {
      logout();
    }
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile);

    username = (TextView) findViewById(R.id.text_username);
    name = (TextView) findViewById(R.id.text_name);
    location = (TextView) findViewById(R.id.text_location);
    gender = (TextView) findViewById(R.id.text_gender);
    age = (TextView) findViewById(R.id.text_age);
    interests = (TextView) findViewById(R.id.text_interests);
    aboutme = (TextView) findViewById(R.id.text_about);

        btnMenu = (Button) findViewById(R.id.menu_button);
        btnMenu.setOnClickListener(this);
        btnCapture = (Button) findViewById(R.id.capture_button);
        btnCapture.setOnClickListener(this);
        btnProfile = (Button) findViewById(R.id.profile_button);
        btnProfile.setOnClickListener(this);
        btnMap = (Button) findViewById(R.id.map_button);
        btnMap.setOnClickListener(this);

    btnProfile.setBackgroundResource(R.drawable.user_pic_edit);

    SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
    if (viewing) {
      setInfo(getIntent().getStringExtra("viewing_id"));
    } else {
      setInfo(prefs);
      listener = new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key) {

          Log.d("debug", "prefs updated:key=" + key);

          if (key.equals("username")) {
            username.setText(sPrefs.getString(key, ""));
          } else if (key.equals("name")) {
            name.setText(sPrefs.getString(key, ""));
          } else if (key.equals("location")) {
            location.setText(sPrefs.getString(key, ""));
          } else if (key.equals("age")) {
            age.setText(sPrefs.getString(key, ""));
          } else if (key.equals("gender")) {
            gender.setText(sPrefs.getString(key, ""));
          } else if (key.equals("interests")) {
            interests.setText(sPrefs.getString(key, ""));
          } else if (key.equals("aboutme")) {
            aboutme.setText(sPrefs.getString(key, ""));
          } else {
            Log.d("debug", "I don't know what was changed, updating all");
            setInfo(sPrefs);
          }
        }
      };
    }
  }

  /****************************************************************
   * @see android.app.Activity#onResume()
   ***************************************************************/
  @Override
  public void onResume() {
    if (getSharedPreferences(PROFILE, 0).getString(PLAYER_ID, "-1").equals("-1")) {
      logout();
    }
    super.onResume();
  }

  @Override
  public void onStart() {
    super.onStart();
    SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
    prefs.registerOnSharedPreferenceChangeListener(listener);

    if (isOnline()) {
      refresh();
    } else {
      showDialog(NO_CONN, NO_CONN_INFO);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    getSharedPreferences(PROFILE, 0).unregisterOnSharedPreferenceChangeListener(listener);
  }

  private void setInfo(SharedPreferences prefs) {
    username.setText(prefs.getString("username", "-----"));
    name.setText(prefs.getString("name", "----"));
    location.setText(prefs.getString("location", "---, --"));
    gender.setText(prefs.getString("gender", "--"));
    age.setText(prefs.getString("age", "--") + " years old");
    interests.setText(prefs.getString("interests", "-----"));
    aboutme.setText(prefs.getString("aboutme", "-----"));
  }

  private void setInfo(String playerId) {

    boolean online = isOnline();

    // make sure we have internet connection before talking to
    // server
    if (online) {
      Log.d("debug", "updating from network");
      // String playerId = prefs.getString("player_id", "");
      String s = Server.getUser(playerId);
      if (!s.equals("error")) {
        // SocNetData:[{"name":"Caleb","location":"Allendale","state":"MI","gender":"m","age":"19","interest":"anything CS!","about":"CS major at GVSU","password":"pass","userName":"calebgomer"}]
        JSONArray profileInfos;
        try {
          profileInfos = new JSONArray(s);
          if (profileInfos.length() == 1) {
            JSONObject info = profileInfos.getJSONObject(0);
            username.setText(info.getString("userName"));
            String strGender = info.getString("gender");
            if (strGender.equalsIgnoreCase("m")) {
              strGender = "Male";
            } else if (strGender.equalsIgnoreCase("f")) {
              strGender = "Female";
            } else {
              strGender = "Other";
            }
            name.setText(info.getString("name"));
            location.setText(info.getString("location"));
            gender.setText(strGender);
            age.setText(info.getString("age"));
            interests.setText(info.getString("interest"));
            aboutme.setText(info.getString("about"));
          }
        } catch (JSONException e) {
          Log.e("profile", "error with JSON");
          e.printStackTrace();
        }
      }
    } else {
      Log.d("debug", "couldn't update - no network connection");
      showDialog(NO_CONN, PROFILE_NOT_RETRIEVED);
    }

  }

  /****************************************************************
   * Checks server for user information and updates any changes 
   * void
   ***************************************************************/
  protected void refresh() {
    SharedPreferences prefs = getSharedPreferences(PROFILE, 0);
    boolean online = isOnline();

    // make sure we have internet connection before talking to
    // server
    if (online) {
      Log.d("debug", "updating from network");
      String playerId = prefs.getString("player_id", "");
      String s = Server.getUser(playerId);
      if (!s.equals("error")) {
        // SocNetData:[{"name":"Caleb","location":"Allendale","state":"MI","gender":"m","age":"19","interest":"anything CS!","about":"CS major at GVSU","password":"pass","userName":"calebgomer"}]
        JSONArray profileInfos;
        try {
          profileInfos = new JSONArray(s);
          if (profileInfos.length() == 1) {
            JSONObject info = profileInfos.getJSONObject(0);

            // String[] userinfo = s.split(TAB);
            SharedPreferences.Editor editor = prefs.edit();
            if (!prefs.getString("username", "").equals(info.getString("userName")))
              // if (!prefs.getString("username", "").equals(userinfo[8]))
              editor.putString("username", info.getString("userName"));
            if (!prefs.getString("name", "").equals(info.getString("name")))
              // if (!prefs.getString("name", "").equals(userinfo[0]))
              editor.putString("name", info.getString("name"));
            String strLocation = info.getString("location") + ", " + info.getString("state");
            if (!prefs.getString("location", "").equals(strLocation))
              editor.putString("location", strLocation);
            String strGender = info.getString("gender");
            if (strGender.equalsIgnoreCase("m")) {
              strGender = "Male";
            } else if (strGender.equalsIgnoreCase("f")) {
              strGender = "Female";
            } else {
              strGender = "Other";
            }
            if (!prefs.getString("gender", "").equals(strGender))
              editor.putString("gender", strGender);
            if (!prefs.getString("age", "").equals(info.getString("age")))
              editor.putString("age", info.getString("age"));
            if (!prefs.getString("interests", "").equals(info.getString("interest")))
              editor.putString("interests", info.getString("interest"));
            if (!prefs.getString("aboutme", "").equals(info.getString("about")))
              editor.putString("aboutme", info.getString("about"));
            editor.commit();
          }
        } catch (JSONException e) {
          Log.e("profile", "error with JSON");
          e.printStackTrace();
        }
      }
    } else {
      Log.d("debug", "couldn't update - no network connection");
      showDialog(NO_CONN, PROFILE_NOT_RETRIEVED);
    }
  }

  /****************************************************************
   * @param title
   * @param info void
   ***************************************************************/
  private void showDialog(String title, String info) {

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.detailed_stats, (ViewGroup) findViewById(R.id.layout_root));

    TextView text = (TextView) layout.findViewById(R.id.text_level);
    text.setText(info);

    builder = new AlertDialog.Builder(this);
    builder.setView(layout);
    alertDialog = builder.create();
    alertDialog.setTitle(title);
    alertDialog.show();
  }

  private void logout() {
    finish();
    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
    startActivity(i);
  }

  public boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = cm.getActiveNetworkInfo();
    if (info != null && info.isConnected()) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean gotoMenu() {
    Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
    startActivity(i);
    return true;
  }

  protected boolean gotoProfile() {
    Toast.makeText(this, "Edit your profile", Toast.LENGTH_SHORT).show();
    Intent i = new Intent(getApplicationContext(), NewUserActivity.class);
    startActivity(i);
    return false;
  }

  protected boolean gotoMap() {
    // Intent myIntent = new Intent(getBaseContext(),
    // CapsuleMapActivity.class);
    // startActivity(myIntent);
    finish();
    return true;
  }

  protected boolean newCapsule() {
    Intent myIntent = new Intent(this, AddCapsuleActivity.class);
    startActivity(myIntent);
    return true;
  }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_button:
                gotoMenu();
                break;
            case R.id.capture_button:
                Toast.makeText(getApplicationContext(), "Capture a Moment", Toast.LENGTH_SHORT).show();
                newCapsule();
                break;
            case R.id.profile_button:
                gotoProfile();
                break;
            case R.id.map_button:
                gotoMap();
                break;
            default:
                break;
        }
    }
}