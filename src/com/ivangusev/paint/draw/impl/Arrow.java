package com.ivangusev.paint.draw.impl;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.ivangusev.paint.draw.Figure;

public class Arrow implements Figure {

	public static final int arrowSize = 10;
	private float startX, startY, endX, endY;

	@Override
	public void setStartXY(float x, float y) {
		startX = x;
		startY = y;
	}

	@Override
	public void setEndXY(float x, float y) {
		endX = x;
		endY = y;
	}

	@Override
	public void draw(Canvas c, Paint p) {
		float dx = endX - startX;
		float dy = endY - startY;
		double length = Math.sqrt((startX - endX) * (startX - endX)
                + ((startY - endY) * (startY - endY)));
		double angle = Math.acos(dx / length);
		if (dy >= 0) {
			angle = (Math.PI * 2) - angle;
		}

		double arrow1X = endX - Math.sin(angle + Math.PI / 3) * arrowSize;
		double arrow1Y = endY - Math.cos(angle + Math.PI / 3) * arrowSize;
		double arrow2X = endX - Math.sin(angle + Math.PI - Math.PI / 3)
				* arrowSize;
		double arrow2Y = endY - Math.cos(angle + Math.PI - Math.PI / 3)
				* arrowSize;
		c.drawLine(endX, endY, (float) arrow1X, (float) arrow1Y, p);
		c.drawLine(endX, endY, (float) arrow2X, (float) arrow2Y, p);
		c.drawLine(startX, startY, endX, endY, p);
	}
}
