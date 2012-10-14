package com.gvsu.socnet.map;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class CapsuleOverlays extends ItemizedOverlay<CapsuleOverlayItem> {

  private ArrayList<CapsuleOverlayItem> mOverlays = new ArrayList<CapsuleOverlayItem>();
  private Context mContext;

  public CapsuleOverlays(Drawable defaultMarker, Context context) {
    super(boundCenterBottom(defaultMarker));
    mContext = context;
    populate();
  }

  @Override
  protected CapsuleOverlayItem createItem(int i) {
    return mOverlays.get(i);
  }

  @Override
  public int size() {
    return mOverlays.size();
  }

  @Override
  protected boolean onTap(int index) {
    CapsuleOverlayItem item = mOverlays.get(index);
    if (item.getCID() == -1) {
      CharSequence text = "You don't appear to be in range to open this capsule.";
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(mContext, text, duration);
      toast.show();
      return true;
    }
    Intent intent = new Intent(mContext, CapsuleActivity.class);
    intent.putExtra("cID", Integer.toString(item.getCID()));
    mContext.startActivity(intent);
    return true;
  }

  public boolean onTouchEvent(MotionEvent event, MapView map) {
    // Disable following the user when the map is dragged
    // It will be enabled when they press the 'search' button
    boolean following = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("follow_user", true);
    if (following && event.getAction() == MotionEvent.ACTION_MOVE) {
      PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("follow_user", false).commit();
//      Log.d("debug", "map dragged, stop following");
    }
    return super.onTouchEvent(event, map);
  }

  public void addOverlay(CapsuleOverlayItem overlay) {
    mOverlays.add(overlay);
    populate();
  }

  public void clear() {
    mOverlays.clear();
  }

}
