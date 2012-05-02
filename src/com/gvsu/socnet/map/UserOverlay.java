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
	boolean rotate;
	float bearing;
	//these help the overlay look like it's in the correct position when zoomed out
	static final int X_OFFSET = 15;
	static final int Y_OFFSET = 30;

	public UserOverlay(GeoPoint pPosition, Context pContext, boolean pRotate, float pBearing) {
		position = pPosition;
		context = pContext;
		rotate = pRotate;
		bearing = pBearing;
	}

	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(position, point);
		
		if(rotate) {
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker);
			
			Matrix matrix = new Matrix();
			matrix.postRotate(bearing, point.x, point.y);
			Bitmap rbm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			
			canvas.drawBitmap(rbm, point.x - X_OFFSET, point.y - Y_OFFSET, null);
		} else {
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
			
			canvas.drawBitmap(bm, point.x - X_OFFSET, point.y - Y_OFFSET, null);
		}
	}
	
	public static int metersToRadius(float meters, MapView map, double latitude) {
		return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math.cos(Math.toRadians(latitude))));
	}
	
	
}
