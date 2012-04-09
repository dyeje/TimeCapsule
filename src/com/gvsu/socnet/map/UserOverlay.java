package com.gvsu.socnet.map;

import soc.net.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class UserOverlay extends Overlay {

	GeoPoint position;
	Context context;
	//these help the overlay look like it's in the correct position when zoomed out
	static final int X_OFFSET = 15;
	static final int Y_OFFSET = 30;

	public UserOverlay(GeoPoint point, Context pContext) {
		position = point;
		context = pContext;
	}

	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// Transfrom geoposition to Point on canvas
		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(position, point);
		
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
		
		canvas.drawBitmap(bm, point.x - X_OFFSET, point.y - Y_OFFSET, null);
	}

	public static int metersToRadius(float meters, MapView map, double latitude) {
		return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math.cos(Math.toRadians(latitude))));
	}
}
