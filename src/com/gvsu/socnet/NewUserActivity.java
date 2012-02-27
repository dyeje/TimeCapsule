/** NewUserActivity.java */
package com.gvsu.socnet;

import soc.net.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

/****************************************************************
 * com.gvsu.socnet.NewUserActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class NewUserActivity extends Activity implements
    OnClickListener {

	EditText username, password, name, city, state, age, interests,
	    aboutme;
	RadioButton male, female, unsure;
	Button create, cancel;

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

		// for debug purposes
		username.setText("User1");
		password.setText("abcd1234");
		name.setText("User number 1");
		city.setText("Allendale");
		state.setText("MI");
		age.setText("21");
		interests.setText("Pizza and Apple Pie");
		aboutme.setText("I eat pizza and pie");
		female.setChecked(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_create_account:
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
			String username = fixSpaces(this.username.getText()
			    .toString());
			String password = fixSpaces(this.password.getText()
			    .toString());
			String city = fixSpaces(this.city.getText().toString());
			String state = fixSpaces(this.state.getText().toString());
			String age = fixSpaces(this.age.getText().toString());
			String interests = fixSpaces(this.interests.getText()
			    .toString());
			String aboutme = fixSpaces(this.aboutme.getText()
			    .toString());

			String userID = Server.newUser(name, city, state, gender,
			    age, interests, aboutme, password, username);

			PreferenceManager
			    .getDefaultSharedPreferences(getApplicationContext())
			    .edit().putString(LoginActivity.PLAYER_ID, userID)
			    .commit();
			Intent i = new Intent(getApplicationContext(),
			    ProfileActivity.class);
			startActivity(i);
			finish();
			break;
		case R.id.button_cancel:
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

	/****************************************************************
	 * @see android.app.Activity#onRestart()
	 ***************************************************************/
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/****************************************************************
	 * @see android.app.Activity#onResume()
	 ***************************************************************/
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/****************************************************************
	 * @see android.app.Activity#onStart()
	 ***************************************************************/
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/****************************************************************
	 * @see android.app.Activity#onStop()
	 ***************************************************************/
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
