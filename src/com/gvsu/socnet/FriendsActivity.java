package com.gvsu.socnet;

import android.os.Bundle;
import android.widget.TextView;

/****************************************************************
 * com.gvsusocnet.treasurehunt.FriendsActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class FriendsActivity extends NavigationMenu {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textview = new TextView(this);
		textview.setText("This is the Friends tab");
		setContentView(textview);
	}
}
