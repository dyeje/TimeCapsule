/** NewEditUserActivity.java */
package com.gvsu.socnet.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.gvsu.socnet.data.AsyncCallbackListener;
import com.gvsu.socnet.data.AsyncDownloader;
import soc.net.R;

import java.util.HashMap;

/**
 * *************************************************************
 * com.gvsu.socnet.NewEditUserActivity
 *
 * @author Caleb Gomer
 * @version 1.0
 *          *************************************************************
 */
public class NewEditUserActivity extends Activity implements OnClickListener, AsyncCallbackListener {

  EditText username, password, name, city, state, age, interests, aboutme;
  RadioButton male, female, unsure;
  Button create, cancel;
  boolean editing;//, worked, authenticated = false;

  /**
   * *************************************************************
   *
   * @param savedInstanceState *************************************************************
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_user);

    username = (EditText) findViewById(R.id.text_username);
    password = (EditText) findViewById(R.id.text_password);
    name = (EditText) findViewById(R.id.text_name);
    city = (EditText) findViewById(R.id.text_city);
    state = (EditText) findViewById(R.id.text_state);
    age = (EditText) findViewById(R.id.text_age);
    interests = (EditText) findViewById(R.id.text_interest);
    aboutme = (EditText) findViewById(R.id.text_about_me);

    male = (RadioButton) findViewById(R.id.radio_male);
    female = (RadioButton) findViewById(R.id.radio_female);
    unsure = (RadioButton) findViewById(R.id.radio_unsure);

    create = (Button) findViewById(R.id.button_create_account);
    create.setOnClickListener(this);
    cancel = (Button) findViewById(R.id.button_cancel);
    cancel.setOnClickListener(this);

    SharedPreferences prefs = getSharedPreferences("profile", 0);
    Log.d("debug", prefs.getString("player_id", ""));

    if (!prefs.getString("player_id", "-1").equals("-1")) {
      editing = true;
      username.setVisibility(View.GONE);
      password.setVisibility(View.GONE);
      ((TextView) findViewById(R.id.text_view_username)).setVisibility(View.GONE);
      ((TextView) findViewById(R.id.text_view_password)).setVisibility(View.GONE);

      name.setText(prefs.getString("name", ""));

      String location = prefs.getString("location", "");
      if(location.length() > 0) {
        String[] split_location = location.split(", ");
        if(0 < split_location.length) city.setText(split_location[0]);
        if(1 < split_location.length) state.setText(split_location[1]);
      }

      String strGender = prefs.getString("gender", "");
      if (strGender.equalsIgnoreCase("Male")) {
        male.setChecked(true);
      } else if (strGender.equalsIgnoreCase("Female")) {
        female.setChecked(true);
      } else {
        unsure.setChecked(true);
      }
      
      age.setText(prefs.getString("age", ""));
      interests.setText(prefs.getString("interests", ""));
      aboutme.setText(prefs.getString("aboutme", ""));
      create.setText("Save Changes");
    } else {
      editing = false;
    }
  }

  @Override
  public void onClick(View clickedView) {
    String gender;
    if (male.isChecked()) {
      gender = "m";
    } else if (female.isChecked()) {
      gender = "f";

    } else if (unsure.isChecked()) {
      gender = "u";
    } else {
      gender = "?";
    }

    String name = fixSpaces(this.name.getText().toString());
    String username = fixSpaces(this.username.getText().toString());
    String password = fixSpaces(this.password.getText().toString());
    String city = fixSpaces(this.city.getText().toString());
    String state = fixSpaces(this.state.getText().toString());
    String age = fixSpaces(this.age.getText().toString());
    String interests = fixSpaces(this.interests.getText().toString());
    String aboutme = fixSpaces(this.aboutme.getText().toString());

    switch (clickedView.getId()) {
      case R.id.button_create_account:
        if (editing) {
          showLoginFields();
        } else {
          HashMap<String, String> requestParams = new HashMap<String, String>();
          requestParams.put(AsyncDownloader.NAME, name);
          requestParams.put(AsyncDownloader.LOCATION, city);
          requestParams.put(AsyncDownloader.STATE, state);
          requestParams.put(AsyncDownloader.GENDER, gender);
          requestParams.put(AsyncDownloader.AGE, age);
          requestParams.put(AsyncDownloader.INTERESTS, interests);
          requestParams.put(AsyncDownloader.ABOUT, aboutme);
          requestParams.put(AsyncDownloader.PASSWORD, password);
          requestParams.put(AsyncDownloader.USERNAME, username);

          AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.NEWUSER, requestParams, this, getApplicationContext());

          AsyncDownloader.perform(request);
        }
        break;

      case R.id.button_cancel:
        finish();
        break;

      case R.id.btn_save:
        EditText pass = (EditText) findViewById(R.id.password_edit);
        String strPass = pass.getText().toString();
        if (!strPass.equals("")) {

          String strUserName = getSharedPreferences("profile", 0).getString("username", "");

          HashMap<String, String> requestParams = new HashMap<String, String>();
          requestParams.put(AsyncDownloader.USERNAME, strUserName);
          requestParams.put(AsyncDownloader.PASSWORD, strPass);

          AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.LOGIN, requestParams, this, getApplicationContext());

          AsyncDownloader.perform(request);

          Log.d("edit", "sent request");

          findViewById(R.id.btn_save).setEnabled(false);
        } else {
          ((TextView) findViewById(R.id.password_view)).setText("Enter a Password");
        }
        break;
    }
  }

  private String fixSpaces(String str) {
    String result = "";
    char[] stra = str.toCharArray();
    for (int i = 0; i < stra.length; i++) {
      if (stra[i] == ' ')
        result += "+";
      else
        result += stra[i];
    }
    return result;
  }

  private void showLoginFields() {
    findViewById(R.id.text_fields).setVisibility(View.GONE);
    findViewById(R.id.login_fields).setVisibility(View.VISIBLE);
    findViewById(R.id.btn_save).setOnClickListener(this);
  }

  public void asyncDone(AsyncDownloader.Payload payload) {
    if (payload.exception == null) {
      switch (payload.taskType) {
        case AsyncDownloader.NEWUSER:
          if (Integer.parseInt(payload.result) != -1) {
            getSharedPreferences("profile", 0).edit().putString(LoginActivity.PLAYER_ID, payload.result).commit();

            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(i);
            finish();
          }
          break;

        case AsyncDownloader.EDITUSER:
          new AlertDialog.Builder(this)
              .setTitle("Success EditUser...Server says:")
              .setMessage(payload.result)
              .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
              })
              .show();
          break;

        case AsyncDownloader.LOGIN:
          findViewById(R.id.btn_save).setEnabled(true);
          if (!payload.result.equals("-1")) {
            SharedPreferences prefs = getSharedPreferences("profile", 0);
            String id = prefs.getString("player_id", "-1");

            String gender;
            if (male.isChecked()) {
              gender = "m";
            } else if (female.isChecked()) {
              gender = "f";

            } else if (unsure.isChecked()) {
              gender = "u";
            } else {
              gender = "?";
            }

            String name = fixSpaces(this.name.getText().toString());
            String username = fixSpaces(this.username.getText().toString());
            String password = fixSpaces(this.password.getText().toString());
            String city = fixSpaces(this.city.getText().toString());
            String state = fixSpaces(this.state.getText().toString());
            String age = fixSpaces(this.age.getText().toString());
            String interests = fixSpaces(this.interests.getText().toString());
            String aboutme = fixSpaces(this.aboutme.getText().toString());

            HashMap<String, String> requestParams = new HashMap<String, String>();
            requestParams.put(AsyncDownloader.USERID, id);
            requestParams.put(AsyncDownloader.NAME, name);
            requestParams.put(AsyncDownloader.USERNAME, username);
            requestParams.put(AsyncDownloader.PASSWORD, password);
            requestParams.put(AsyncDownloader.LOCATION, city);
            requestParams.put(AsyncDownloader.STATE, state);
            requestParams.put(AsyncDownloader.GENDER, gender);
            requestParams.put(AsyncDownloader.AGE, age);
            requestParams.put(AsyncDownloader.INTERESTS, interests);
            requestParams.put(AsyncDownloader.ABOUT, aboutme);

            AsyncDownloader.Payload request = new AsyncDownloader.Payload(AsyncDownloader.EDITUSER, requestParams, this, getApplicationContext());

            AsyncDownloader.perform(request);

          } else {
            ((TextView) findViewById(R.id.password_view)).setText("Please Try Again");
          }
          break;
      }
    } else {
      new AlertDialog.Builder(this)
          .setTitle(payload.errorString())
          .setMessage("Sorry, we're having trouble editing your profile. Please try that again...")
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          })
          .show();
    }
  }
}
