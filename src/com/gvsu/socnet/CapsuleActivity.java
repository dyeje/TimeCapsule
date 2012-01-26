package com.gvsu.socnet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CapsuleActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("You opened a treasure.");
        setContentView(tv);
	}
}
