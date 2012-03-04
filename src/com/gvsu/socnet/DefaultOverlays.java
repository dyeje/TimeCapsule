package com.gvsu.socnet;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class DefaultOverlays extends ItemizedOverlay {

	private ArrayList<CapsuleOverlayItem> mOverlays = new ArrayList<CapsuleOverlayItem>();
	private Context mContext;

	public DefaultOverlays(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		CapsuleOverlayItem item = mOverlays.get(index);
		if (item.getTitle() == "User")
			return true;
		Intent intent = new Intent(mContext, CapsuleActivity.class);
		intent.putExtra("cID", Integer.toString(item.getCID()));
		mContext.startActivity(intent);
		return true;
	}

	public void addOverlay(CapsuleOverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	public void clear() {
		mOverlays.clear();
	}

}
