package com.gvsu.socnet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/****************************************************************
 * com.gvsusocnet.treasurehunt.JournalActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class JournalActivity extends NavigationMenu {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textview = new TextView(this);
		textview
		    .setText("This is the Journal tab\n\nIt will include information about active treasure hunts, missions, and other relevant information for the user/player (information not visible to anyone but the individual user).");
		setContentView(textview);
	}
}
