package com.ivangusev.paint.draw;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface Figure {

	void draw(Canvas c, Paint p);

	void setStartXY(float x, float y);

	void setEndXY(float x, float y);
}
