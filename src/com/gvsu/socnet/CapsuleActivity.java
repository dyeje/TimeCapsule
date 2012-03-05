package com.gvsu.socnet;

import java.util.ArrayList;

import soc.net.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gvsu.socnet.data.Comment;

public class CapsuleActivity extends NavigationMenu implements
    OnClickListener {

	/** String YOUTUBE */
	private final String YOUTUBE = "http://www.youtube.com/watch?v=";

	/** String TAB */
	private final String TAB = "\t";

	/** String capsuleId */
	private String capsuleId;

	/** LinearLayout commentList */
	LinearLayout commentList;

	/** ArrayList<TextView> comments */
	// ArrayList<TextView> comments;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		View.inflate(this, R.layout.capsule, vg);
		// setContentView(R.layout.treasure);

		// btnCapture.setVisibility(View.INVISIBLE);
		// btnMap.setVisibility(View.INVISIBLE);
		// btnProfile.setVisibility(View.INVISIBLE);

		ImageView playButton = (ImageView) findViewById(R.id.play_button);
		playButton.setOnClickListener(this);
		TextView title = (TextView) findViewById(R.id.capsule_title);
		TextView description = (TextView) findViewById(R.id.description);

		// Get Treasure Info From Server
		Intent intent = this.getIntent();
		final String cId = intent.getStringExtra("cID");
		capsuleId = cId;
		Log.d("debug", "id = " + capsuleId);
		String[] treasureInfo = Server.getCapsule(capsuleId).split(
		    TAB);
		String debug = "";
		for (String str : treasureInfo) {
			debug += str;
			debug += " - ";
		}

		commentList = (LinearLayout) findViewById(R.id.comment_layout);
		createComments();

		Log.d("debug", debug);
		//
		title.setText(treasureInfo[0]);
		description.setText(treasureInfo[3]);
		//
		// TextView leftOn = (TextView) findViewById(R.id.leftOn);
		// leftOn.setText(treasureInfo[5]/* .substring(0, 10) */);
		//
		// TextView timesFound = (TextView)
		// findViewById(R.id.timesFound);
		// timesFound.setText("(Read " + treasureInfo[0] + " times)");

		// TextView tv = (TextView) findViewById(R.id.)
	}

	@Override
	protected void refresh() {

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

	/****************************************************************
	 * @param id
	 * @return String
	 ***************************************************************/
	public String getCapsule(String id) {
		String s = Server.getCapsule(id);
		String[] treasureInfo = s.split("\t");

		/*********LOG**********LOG*************/
		Log.println(3, "debug", s);
		/*********LOG**********LOG*************/

		String str =
		// "Lat: " + treasureInfo[0] + "\tLong: " + treasureInfo[1] +
		"\n\nQuestion: " + treasureInfo[2] + "\nAnswer: "
		    + treasureInfo[3] + "\n\nPoints: " + treasureInfo[4];
		// + "\n\nCreated: " + treasureInfo[5] + "\nExpires: "
		// + treasureInfo[6];

		return str;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private void createComments() {
		String commentsFromServer = Server.getComments(capsuleId);
		Log.d("debug", capsuleId + ":" + commentsFromServer);
		String[] strArrayComments = commentsFromServer.split("\n");
		for (String s : strArrayComments) {
			if (!s.equals("")) {
				String[] strArrayComment = s.split("\t");
				// Log.d("debug", s);
				String[] strArrayUser = Server.getUser(
				    strArrayComment[0]).split("\t");
				TextView t = new TextView(this);
				String user = strArrayUser[8];
				t.setId(Integer.parseInt(strArrayComment[0]));
				t.setText(new Comment(user, strArrayComment[2])
				    .toString());
				t.setPadding(0, 10, 0, 0);
				commentList.addView(t);
			}
		}
	}

	protected boolean gotoProfile() {
		Intent myIntent = new Intent(getBaseContext(),
		    ProfileActivity.class);
		startActivity(myIntent);
		return false;
	}

	protected boolean gotoMap() {
		Intent myIntent = new Intent(getBaseContext(),
		    CapsuleMapActivity.class);
		startActivity(myIntent);
		return true;
	}
}