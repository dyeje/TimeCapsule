/** NewEditUserActivity.java */
package com.gvsu.socnet.user;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.gvsu.socnet.data.Server;
import soc.net.R;

/****************************************************************
 * com.gvsu.socnet.NewEditUserActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class NewEditUserActivity extends Activity implements OnClickListener {

  EditText username, password, name, city, state, age, interests, aboutme;
  RadioButton male, female, unsure;
  Button create, cancel;
  boolean editing, worked, authenticated = false;

  /****************************************************************
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * @param savedInstanceState
   ***************************************************************/
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

    if (!prefs.getString("player_id", "").equals("-1")) {
      editing = true;
      username.setVisibility(View.GONE);
      password.setVisibility(View.GONE);
      ((TextView) findViewById(R.id.text_view_username)).setVisibility(View.GONE);
      ((TextView) findViewById(R.id.text_view_password)).setVisibility(View.GONE);

      name.setText(prefs.getString("name", ""));
      city.setText(prefs.getString("location", ", ").split(", ")[0]);
      state.setText(prefs.getString("location", ", ").split(", ")[1]);
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

    // for debug purposes
    // username.setText("User1");
    // password.setText("abcd1234");
    // name.setText("User number 1");
    // city.setText("Allendale");
    // state.setText("MI");
    // age.setText("21");
    // interests.setText("Pizza and Apple Pie");
    // aboutme.setText("I eat pizza and pie");
    // female.setChecked(true);
  }

  @Override
  public void onClick(View v) {
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

    switch (v.getId()) {
    case R.id.button_create_account:

      /*******************************
       * Editing an existing profile
       ******************************/
      if (editing) {
        makeUserLogin();
        /*******************************
         * Creating a new profile
         ******************************/
      } else {
        String userID = Server.newUser(name, city, state, gender, age, interests, aboutme, password, username);
        if (Integer.parseInt(userID) != -1) {

          getSharedPreferences("profile", 0).edit().putString(LoginActivity.PLAYER_ID, userID).commit();
          worked = true;
        }
      }

      /****************************************
       * Leaves activity if everything's done
       ***************************************/
      if (worked) {
        Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(i);
        finish();
      } else {

      }
      break;
    case R.id.button_cancel:
      finish();
      break;
    case R.id.btn_login:
      EditText pass = (EditText) findViewById(R.id.password_edit);
      String strPass = pass.getText().toString();
      if (!strPass.equals("")) {

        String strUserName = getSharedPreferences("profile", 0).getString("username", "");

        Log.d("debug", "userName: " + strUserName + " ps: " + strPass);
        String auth = Server.authenticate(strUserName, strPass);
        Log.d("debug", "auth: " + auth);
        if (!auth.equals("-1")) {
          Log.d("debug", "auth worked");
          SharedPreferences prefs = getSharedPreferences("profile", 0);
          String id = prefs.getString("player_id", "-1");
          String strWorked = Server.editUser(id, name, city, state, gender, age, interests, aboutme, strPass, getSharedPreferences("profile", 0).getString("username", ""));
          finish();
        } else {
          Log.d("debug", "auth didn't work");
          ((TextView) findViewById(R.id.password_view)).setText("Please Try Again");
        }
      } else {
        Log.d("debug", "auth didn't work");
        ((TextView) findViewById(R.id.password_view)).setText("Please Try Again");
      }
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

  private void makeUserLogin() {
    ((LinearLayout) findViewById(R.id.text_fields)).setVisibility(View.GONE);
    ((LinearLayout) findViewById(R.id.login_fields)).setVisibility(View.VISIBLE);
    ((Button) findViewById(R.id.btn_login)).setOnClickListener(this);
  }

  /****************************************************************
   * @see android.app.Activity#onRestart()
   ***************************************************************/
  @Override
  protected void onRestart() {
    super.onRestart();
  }

  /****************************************************************
   * @see android.app.Activity#onResume()
   ***************************************************************/
  @Override
  protected void onResume() {
    super.onResume();
  }

  /****************************************************************
   * @see android.app.Activity#onStart()
   ***************************************************************/
  @Override
  protected void onStart() {
    super.onStart();
  }

  /****************************************************************
   * @see android.app.Activity#onStop()
   ***************************************************************/
  @Override
  protected void onStop() {
    super.onStop();
  }

}
