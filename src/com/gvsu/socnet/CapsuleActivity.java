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

	/** LinearLayout commentList */
	LinearLayout commentList;

	/** ArrayList<TextView> comments */
	ArrayList<TextView> comments;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		ViewGroup vg = (ViewGroup) findViewById(R.id.lldata);
		View.inflate(this, R.layout.treasure, vg);
		// setContentView(R.layout.treasure);

		btnCapture.setVisibility(View.INVISIBLE);
		btnMap.setVisibility(View.INVISIBLE);
		btnProfile.setVisibility(View.INVISIBLE);

		ImageView playButton = (ImageView) findViewById(R.id.play_button);
		playButton.setOnClickListener(this);
		TextView title = (TextView) findViewById(R.id.capsule_title);
		TextView description = (TextView) findViewById(R.id.description);

		commentList = (LinearLayout) findViewById(R.id.comment_layout);
		createFakeComments();

		// Get Treasure Info From Server
		// String id = PreferenceManager.getDefaultSharedPreferences(
		// getApplicationContext()).getString("capsule", "0");
		Intent intent = this.getIntent();
		int id = intent.getIntExtra("cID", 0);
		String[] treasureInfo = Server.getCapsule(
		    Integer.toString(id)).split("\t");
		// String[] treasureInfo = Server.getCapsule(id).split("\t");
		String debug = "";
		for (String str : treasureInfo) {
			debug += str;
			debug += " - ";
		}

		Log.d("debug", debug);
//
//		title.setText(treasureInfo[2]);
//		description.setText(treasureInfo[3]);
//
//		TextView leftOn = (TextView) findViewById(R.id.leftOn);
//		leftOn.setText(treasureInfo[5]/* .substring(0, 10) */);
//
//		TextView timesFound = (TextView) findViewById(R.id.timesFound);
//		timesFound.setText("(Read " + treasureInfo[0] + " times)");

		// TextView tv = (TextView) findViewById(R.id.)
		thisClass = getClass();
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
	public String getTreasure(String id) {
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

	private void createFakeComments() {
		comments = new ArrayList<TextView>();

		TextView t = new TextView(this);
		t.setText(new Comment("Caleb", "This is fun").toString());
		t.setPadding(0, 10, 0, 0);
		// commentList.addView(t);
		TextView t1 = new TextView(this);
		t1.setPadding(0, 10, 0, 0);
		t1.setText(new Comment("Joe", "I like youtube").toString());
		// commentList.addView(t1);

		comments.add(t);
		comments.add(t1);
		int i = 0;
		while (i <= 5) {
			TextView T = new TextView(this);
			T.setText(new Comment("Person" + i,
			    "Random Gibberish to show scrolling effect " + i)
			    .toString());
			T.setPadding(0, 10, 0, 0);
			comments.add(T);
			i++;
		}
		for (TextView c : comments) {
			commentList.addView(c);
		}
	}
}