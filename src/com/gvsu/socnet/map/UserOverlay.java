package com.gvsu.socnet.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class UserOverlay extends Overlay {

	GeoPoint position;
	int innerRadius;
	int outerRadius;

	public UserOverlay(GeoPoint point) {
		position = point;
	}

	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		innerRadius = 100;
		outerRadius = 100000;

		// Transfrom geoposition to Point on canvas
		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(position, point);

		Paint innerCircle = new Paint();
		innerCircle.setColor(Color.GREEN);
		innerCircle.setAlpha(40);
		int innerCircleRadius = metersToRadius(innerRadius, mapView, (double) position.getLatitudeE6() / 1000000);
		canvas.drawCircle(point.x, point.y, innerCircleRadius, innerCircle);

		Paint outerCircle = new Paint();
		outerCircle.setColor(Color.RED);
		outerCircle.setAlpha(10);
		int outerCircleRadius = metersToRadius(outerRadius, mapView, (double) position.getLatitudeE6() / 1000000);
		canvas.drawCircle(point.x, point.y, outerCircleRadius, outerCircle);
	}

	public static int metersToRadius(float meters, MapView map, double latitude) {
		return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math.cos(Math.toRadians(latitude))));
	}
}
