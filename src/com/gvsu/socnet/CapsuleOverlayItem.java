package com.gvsu.socnet;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class CapsuleOverlayItem extends OverlayItem {
	int tID;
	
	public CapsuleOverlayItem(GeoPoint point, String title, String snippet, int tID) {
		super(point, title, snippet);
		this.tID = tID;
	}
}
