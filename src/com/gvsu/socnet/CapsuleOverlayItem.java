package com.gvsu.socnet;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class CapsuleOverlayItem extends OverlayItem {
	int cID;

	public CapsuleOverlayItem(GeoPoint point,
	    String title,
	    String snippet,
	    int cID) {
		super(point, title, snippet);
		this.cID = cID;
	}
	
	public int getCID() { return cID; }
}
