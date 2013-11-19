package com.ivangusev.paint.draw.impl;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.FloatMath;
import com.ivangusev.paint.draw.Figure;

public class Circle implements Figure {

	private float startX, startY, radius;
	
	@Override
	public void setStartXY(float x, float y) {
		startX = x;
		startY = y;
	}

	@Override
	public void setEndXY(float x, float y) {
		setRadius(x, y);
	}

	public void setRadius(float x, float y) {
		radius = FloatMath.sqrt((x - startX) * (x - startX) + (y - startY)
                * (y - startY));
	}

	@Override
	public void draw(Canvas c, Paint p) {
		c.drawCircle(startX, startY, radius, p);
	}
}
