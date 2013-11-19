package com.ivangusev.paint.draw.impl;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.ivangusev.paint.draw.Figure;

public class Text implements Figure {
	
	private float startX, startY, endX, endY;
	
	private String mText;
	private float mTextSize;
	
	public Text(String pText, float pTextSize){
		mText = pText;
		mTextSize = pTextSize;
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

	@Override
	public void draw(Canvas c, Paint p) {
		p.setTextSize(mTextSize);
		c.drawText(mText, startX, startY, p);	
	}

}
