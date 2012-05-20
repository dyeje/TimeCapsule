package com.gvsu.socnet.map;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import soc.net.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gvsu.socnet.data.Comment;
import com.gvsu.socnet.data.Server;
import com.gvsu.socnet.user.LoginActivity;
import com.gvsu.socnet.user.ProfileActivity;
import com.gvsu.socnet.user.SettingsActivity;
import com.gvsu.socnet.views.NavigationMenu;

public class CapsuleActivity extends NavigationMenu implements OnClickListener {

	/** String YOUTUBE */
	private final String YOUTUBE = "http://www.youtube.com/watch?v=";

	/** String TAB */
	private final String TAB = "\t";

	private String creatorID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		View.inflate(this, R.layout.capsule, vg);

		// Increment the number of views on the capsule
		final String cId = getIntent().getStringExtra("cID");

		String userId = getSharedPreferences(LoginActivity.PROFILE, 0).getString(LoginActivity.PLAYER_ID, "-1");
		if (userId.equals("-1"))// just in case user is not logged in somehow
			startActivity(new Intent(this, LoginActivity.class));
		else
			Server.addAView(userId, cId);

		refresh();

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/****************************************************************
	 * Get capsule info from the server and display it
	 **************************************************************
	 */
	@Override
	protected void refresh() {
		Intent intent = this.getIntent();
		final String cId = intent.getStringExtra("cID");
		Log.i("debug", "capid=" + cId);
		setCapsuleInfo(cId);
		setComments(cId);
		setupAddComments(cId);
		setupAddRating(cId);
	}

	/****************************************************************
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 * @param v
	 ***************************************************************/
	@Override
	public void onClick(View v) {
		Log.println(3, "debug", "buttonClicked");
		switch (v.getId()) {
		case R.id.play_button:
			Log.i("debug", "playButtonClicked");
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE + "RCUBxgdKZ_Y")));
			break;
		default:
			super.onClick(v);
			break;
		}

	}

	private void setCapsuleInfo(String capsuleId) {

		TextView title = (TextView) findViewById(R.id.capsule_title);
		TextView description = (TextView) findViewById(R.id.description);
		TextView creator = (TextView) findViewById(R.id.capsule_creator);
		TextView leftOn = (TextView) findViewById(R.id.left_on);
		RatingBar rating = (RatingBar) findViewById(R.id.capsule_rating_bar);

		// Get Capsule Information from Server
		String strCapInfo = Server.getCapsule(capsuleId);
		if (strCapInfo.equals("error"))
			return;// return instead
		// Log.i("debug", "capsuleinfo: " + strCapInfo);
		// TODO show and link to the user who left capsule when the server returns this
		// information

		try {
			JSONArray capsuleStuff = new JSONArray(strCapInfo);
			JSONObject capsule = capsuleStuff.getJSONObject(0);
			String strTitle = capsule.getString("title");
			String strDescription = capsule.getString("description");
			String strCreateDate = capsule.getString("createDate");
			creatorID = capsule.getString("creatorId");
			String strCreatorUName = capsule.getString("userName");
			String strRating = capsule.getString("avgRate");

			title.setText(strTitle);
			description.setText(strDescription);
			leftOn.setText(leftOnToString(strCreateDate));
			// leftOn.setText("Left on " + strCreateDate.split(" ")[0] + " at " +
			// strCreateDate.split(" ")[1]);
			if (strRating == null || strRating.equals("null"))
				rating.setRating(0);
			else
				rating.setRating(Float.parseFloat(strRating));
			if (strCreatorUName == null || strCapInfo.equals("") || strCreatorUName.equals(""))
				creator.setText("Anonymous");
			else
				creator.setText("Left by " + strCreatorUName);
		} catch (JSONException e) {
			Log.e("debug", "Error parsing capsule id=" + capsuleId + ": " + e.getMessage());
		}

		/** old non-JSON method **/
		// String[] capsuleInfo = strCapInfo.split(TAB);
		// title.setText(capsuleInfo[0]);
		// description.setText(capsuleInfo[3]);
		//
		// // gets the date capsule was left
		// leftOn.setText("Left on " + capsuleInfo[4].split(" ")[0] + " at " +
		// capsuleInfo[4].split(" ")[1]);
		//
		// String strRating = Server.getRating(capsuleId);
		// if (!strRating.equals("")) {
		// Log.i("debug", "rating: " + strRating);
		// rating.setRating(Float.parseFloat(strRating));
		// } else {
		// rating.setRating(0);
		// }
		// creatorID = capsuleInfo[5];
		// creator.setText(capsuleInfo[6]);
		creator.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
				i.putExtra("viewing_id", creatorID);
				startActivity(i);
			}
		});
	}

	private void setComments(final String capsuleId) {
		final LinearLayout commentList = (LinearLayout) findViewById(R.id.comment_layout);
		commentList.removeAllViews();
		String commentsFromServer = Server.getComments(capsuleId);
		if (commentsFromServer.equals("error"))
			return;// return instead

		// Log.i("debug", "Comments on capsule number " + capsuleId + "\n" + commentsFromServer);

		try {
			JSONArray comments = new JSONArray(commentsFromServer);
			int len = comments.length();
			int numComments = 0;
			int numViews = 0;
			ArrayList<String> viewers = new ArrayList<String>();
			int numDistinctViewers = 0;
			for (int i = 0; i < len; i++) {
				JSONObject comment = comments.getJSONObject(i);

				String strComment = comment.getString("comments");
				if (strComment.equals("")) {
					numViews++;
					if (!viewers.contains(comment.getString("userId"))) {
						viewers.add(comment.getString("userId"));
						numDistinctViewers++;
					}
					continue; // this is just a view, not a comment so don't do anything else
				} else
					numComments++;
				String strVisitTime = comment.getString("visitTime");
				String strUserId = comment.getString("userId");

				JSONArray userStuff = new JSONArray(Server.getUser(strUserId));
				JSONObject user = userStuff.getJSONObject(0);

				Log.i("debug", "user=" + user.toString());

				TextView t = new TextView(this);
				t.setId(Integer.parseInt(strUserId));
				t.setText(new Comment(user.getString("name"), strComment, strVisitTime).toString());
				t.setPadding(0, 10, 0, 0);
				commentList.addView(t);
			}
			if (numComments == 0) {
				TextView noComments = new TextView(this);
				noComments.setText("Be the first to comment!");
				commentList.addView(noComments);
			}
			((TextView) findViewById(R.id.timesFound)).setText("Read " + numViews + " time" + ((numViews != 1) ? "s" : "") + " by " + numDistinctViewers
			    + ((numDistinctViewers != 1) ? " different users" : " user"));

		} catch (JSONException e) {
			Log.e("debug", "error parsing comments on capsule (id=" + capsuleId + ")\n" + e.getMessage());
		}
	}

	private void setupAddComments(final String capsuleId) {
		((Button) findViewById(R.id.button_add_comment)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText newComment = (EditText) findViewById(R.id.edit_text_new_comment);
				Server.addComment(getSharedPreferences("profile", 0).getString("player_id", "0"), capsuleId, fixSpaces(newComment.getText().toString()));
				newComment.setText("");
				refresh();
			}
		});
	}

	private void setupAddRating(final String capsuleId) {

		final RatingBar ratingBar = ((RatingBar) findViewById(R.id.capsule_rating_bar));
		final LinearLayout submitLayout = ((LinearLayout) findViewById(R.id.submit_rating_layout));
		final Button submitButton = ((Button) findViewById(R.id.capsule_rating_submit));
		final Button cancelButton = ((Button) findViewById(R.id.capsule_rating_cancel));

		final float ratingBeforeUserMessedWithIt = ratingBar.getRating();

		// listens for ratingbar to be changed
		ratingBar.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				submitLayout.setVisibility(View.VISIBLE);
				return false;
			}
		});

		// listens for ratingbar submit button
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submitLayout.setVisibility(View.GONE);
				Server.addRating(getSharedPreferences("profile", 0).getString("player_id", "0"), capsuleId, Float.toString(ratingBar.getRating()));
				refresh();
				Toast.makeText(getApplicationContext(), "Rating Submitted", Toast.LENGTH_SHORT).show();
			}
		});
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submitLayout.setVisibility(View.GONE);
				ratingBar.setRating(ratingBeforeUserMessedWithIt);
			}
		});
	}

	@Override
	protected boolean gotoMenu() {
		Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(i);
		return true;
	}

	@Override
	protected boolean gotoProfile() {
		Intent myIntent = new Intent(getBaseContext(), ProfileActivity.class);
		// TODO profile button from a capsule takes you to the user
		// who left that capsule's profile
		// myIntent.putExtra("player_id",
		// "the player's id who left it");
		startActivity(myIntent);
		return false;
	}

	@Override
	protected boolean gotoMap() {
		Intent myIntent = new Intent(getBaseContext(), CapsuleMapActivity.class);
		startActivity(myIntent);
		finish();
		return true;
	}

	@Override
	protected boolean newCapsule() {
		return false;
	}

	/****************************************************************
	 * @see com.gvsu.socnet.views.NavigationMenu#onBackPressed()
	 ***************************************************************/
	@Override
	public void onBackPressed() {
		finish();
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

	private String leftOnToString(String strCreateDate) {
		GregorianCalendar c = Comment.makeSenseOf(strCreateDate);
		// String s = "Left on ";
		String s = "";
		switch (c.get(Calendar.DAY_OF_WEEK)) {
		case 1:
			s += "Sunday, ";
			break;
		case 2:
			s += "Monday, ";
			break;
		case 3:
			s += "Tuesday, ";
			break;
		case 4:
			s += "Wednesday, ";
			break;
		case 5:
			s += "Thursday, ";
			break;
		case 6:
			s += "Friday, ";
			break;
		case 7:
			s += "Saturday, ";
			break;
		}
		switch (c.get(Calendar.MONTH)) {
		case 0:
			s += "January ";
			break;
		case 1:
			s += "February ";
			break;
		case 2:
			s += "March ";
			break;
		case 3:
			s += "April ";
			break;
		case 4:
			s += "May ";
			break;
		case 5:
			s += "June ";
			break;
		case 6:
			s += "July ";
			break;
		case 7:
			s += "August, ";
			break;
		case 8:
			s += "Sunday, ";
			break;
		case 9:
			s += "Monday, ";
			break;
		case 10:
			s += "Tuesday, ";
			break;
		case 11:
			s += "Wednesday, ";
			break;
		}
		s += c.get(Calendar.DAY_OF_MONTH);
		s += " at ";
		int hour = c.get(Calendar.HOUR);
		if (hour == 0)
			hour = 12;
		s += hour;
		s += ":";
		int minute = c.get(Calendar.MINUTE);
		String sminute = "";
		if (minute < 10)
			sminute = "0" + minute;
		else
			sminute = "" + minute;
		s += sminute;
		s += (c.get(Calendar.AM_PM) == 0 ? "am" : "pm");
		// s += "\n" + strCreateDate;
		return s;
	}
}