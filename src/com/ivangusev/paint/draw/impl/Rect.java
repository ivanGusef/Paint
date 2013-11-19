package com.ivangusev.paint.draw.impl;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.ivangusev.paint.draw.Figure;

public class Rect implements Figure {

	private float startX, startY, endX, endY;

	@Override
	public void draw(Canvas c, Paint p) {
		if (startX < endX && startY < endY) {
			c.drawRect(startX, startY, endX, endY, p);
		} else if (startX > endX && startY < endY) {
			c.drawRect(endX, startY, startX, endY, p);
		} else if (startX < endX && startY > endY) {
			c.drawRect(startX, endY, endX, startY, p);
		} else if (startX > endX && startY > endY) {
			c.drawRect(endX, endY, startX, startY, p);
		}
	}

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

}
