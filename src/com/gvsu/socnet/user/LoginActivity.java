/** LoginActivity.java */
package com.gvsu.socnet.user;

import soc.net.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/****************************************************************
 * com.gvsu.socnet.LoginActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class LoginActivity extends Activity implements OnClickListener {

	public static final String PLAYER_ID = "player_id";
	public TextView loginResult, username, password, newUser;
	public Button loginButton = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		SharedPreferences prefs = getSharedPreferences("profile", 0);

		if (!prefs.getString(PLAYER_ID, "").equals("")) {
			gotoProfile();
		}

		setContentView(R.layout.login);
		loginButton = (Button) findViewById(R.id.button_start);
		loginButton.setOnClickListener(this);
		username = (EditText) findViewById(R.id.username);
		username.requestFocus();
		password = (EditText) findViewById(R.id.password);
		loginResult = (TextView) findViewById(R.id.loginresult);
		newUser = (TextView) findViewById(R.id.button_new_user);
		newUser.setOnClickListener(this);

		// this is very temporary
		username.setHint("Enter Player ID");
		password.setHint("Enter ID Above");
		password.setEnabled(false);
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_start:

			// if (username.getText().toString().equals("")
			// || password.getText().toString().equals("")) {
			// showDialog("Missing Information",
			// "Please enter a username and password", this);
			// return;
			// }

			// temporarily sets user ID to the 'username' box for
			// debugging
			getSharedPreferences("profile", 0).edit().putString(PLAYER_ID, username.getText().toString()).commit();
			gotoProfile();
			break;
		case R.id.button_new_user:
			Intent i = new Intent(getApplicationContext(), NewUserActivity.class);
			startActivity(i);
			finish();
		}

	}

	/****************************************************************
	* @param info
	* @param title
	* void
	***************************************************************/
	public void showDialog(String title, String info, Context context) {

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.text_dialog, (ViewGroup) findViewById(R.id.layout_root));

		TextView text = (TextView) layout.findViewById(R.id.text_level);
		text.setText(info);

		builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		alertDialog = builder.create();
		alertDialog.setTitle(title);
		alertDialog.show();
	}

	/****************************************************************
	 * void
	 ***************************************************************/
	public void gotoProfile() {

		Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
		startActivity(intent);
		finish();
	}
}
