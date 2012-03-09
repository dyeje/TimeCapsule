package com.gvsu.socnet;

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

public class CapsuleActivity extends NavigationMenu implements
    OnClickListener {

	/** String YOUTUBE */
	private final String YOUTUBE = "http://www.youtube.com/watch?v=";

	/** String TAB */
	private final String TAB = "\t";

	/** String capsuleId */
	// private String capsuleId;

	/** LinearLayout commentList */
	// LinearLayout commentList;

	/** ArrayList<TextView> comments */
	// ArrayList<TextView> comments;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		View.inflate(this, R.layout.capsule, vg);

		refresh();

		this.getWindow()
		    .setSoftInputMode(
		        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/****************************************************************
	 * Get capsule info from the server and display it
	 **************************************************************
	 */
	@Override
	protected void refresh() {
		Intent intent = this.getIntent();
		final String cId = intent.getStringExtra("cID");
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
			Log.println(3, "debug", "playButtonClicked");
			startActivity(new Intent(Intent.ACTION_VIEW,
			    Uri.parse(YOUTUBE + "RCUBxgdKZ_Y")));
			break;
		default:
			super.onClick(v);
			break;
		}

	}

	private void setCapsuleInfo(String capsuleId) {
		TextView title = (TextView) findViewById(R.id.capsule_title);
		TextView description = (TextView) findViewById(R.id.description);
		TextView leftOn = (TextView) findViewById(R.id.left_on);
		RatingBar rating = (RatingBar) findViewById(R.id.capsule_rating_bar);

		String[] treasureInfo = Server.getCapsule(capsuleId).split(
		    TAB);
		title.setText(treasureInfo[0]);
		description.setText(treasureInfo[3]);
		leftOn.setText("Left on " + treasureInfo[4]);
		rating
		    .setRating(Float.parseFloat(Server.getRating(capsuleId)));
	}

	private void setComments(final String capsuleId) {
		final LinearLayout commentList = (LinearLayout) findViewById(R.id.comment_layout);
		commentList.removeAllViews();
		String commentsFromServer = Server.getComments(capsuleId);

		Log.d("debug", "Comments on capsule number " + capsuleId
		    + "\n" + commentsFromServer);
		String[] strArrayComments = commentsFromServer.split("\n");
		if (!strArrayComments[0].equals("")) {
			for (String s : strArrayComments) {
				if (!s.equals("")) {
					String[] strArrayComment = s.split("\t");
					String[] strArrayUser = Server.getUser(
					    strArrayComment[0]).split("\t");
					TextView t = new TextView(this);
					String user = strArrayUser[8];
					t.setId(Integer.parseInt(strArrayComment[0]));
					t.setText(new Comment(user, strArrayComment[2],
					    strArrayComment[1]).toString());
					t.setPadding(0, 10, 0, 0);
					commentList.addView(t);
				}
			}
		} else {
			TextView noComments = new TextView(this);
			noComments.setText("Be the first to comment!");
			commentList.addView(noComments);
		}
	}

	//
	private void setupAddComments(final String capsuleId) {
		((Button) findViewById(R.id.button_add_comment))
		    .setOnClickListener(new OnClickListener() {

			    @Override
			    public void onClick(View v) {
				    EditText newComment = (EditText) findViewById(R.id.edit_text_new_comment);
				    Server.addComment(
				        getSharedPreferences("profile", 0).getString(
				            "player_id", "0"), capsuleId,
				        fixSpaces(newComment.getText().toString()));
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

		final float ratingBeforeUserMessedWithIt = ratingBar
		    .getRating();

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
				Server.addRating(getSharedPreferences("profile", 0)
				    .getString("player_id", "0"), capsuleId, Float
				    .toString(ratingBar.getRating()));
				refresh();
				Toast.makeText(getApplicationContext(),
				    "Rating Submitted", Toast.LENGTH_SHORT).show();
				submitLayout.setVisibility(View.GONE);
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
		Intent i = new Intent(getApplicationContext(),
		    SettingsActivity.class);
		startActivity(i);
		return true;
	}

	@Override
	protected boolean gotoProfile() {
		Intent myIntent = new Intent(getBaseContext(),
		    ProfileActivity.class);
		startActivity(myIntent);
		return false;
	}

	@Override
	protected boolean gotoMap() {
		Intent myIntent = new Intent(getBaseContext(),
		    CapsuleMapActivity.class);
		startActivity(myIntent);
		return true;
	}

	@Override
	protected boolean newCapsule() {
		return false;
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
}