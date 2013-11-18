package com.ivangusev.paint;

import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.ivangusev.paint.draw.Figure;
import com.ivangusev.paint.draw.impl.Arrow;
import com.ivangusev.paint.draw.impl.Circle;
import com.ivangusev.paint.draw.impl.Rect;
import com.ivangusev.paint.draw.impl.Text;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Aleksey_Medvedev
 */
public class PainterView extends View implements View.OnTouchListener {

    public static final int MODE_ARROW = 0;
    public static final int MODE_CIRCLE = 1;
    public static final int MODE_RECT = 2;
    public static final int MODE_TEXT = 3;

    public String mPaintText = "";
    public float mTextSize = 14;

    private int mEventMode;

    private Paint mPaint;

    private Bitmap mStateBitmap;
    private Bitmap mTempBitmap;

    private Canvas mCanvas;
    private Figure mFigure;
    private State mState;

    private String fileName;

    public PainterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();

        mPaint.setColor(Color.YELLOW);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);

        mFigure = new Arrow();
        mState = new State();

        if (mStateBitmap == null) {
            mStateBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        }

        setOnTouchListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mEventMode = MotionEvent.ACTION_DOWN;
                mFigure.setStartXY(event.getX(), event.getY());
                mCanvas.drawBitmap(getStateBitmap(), 0, 0, null);
                break;
            case MotionEvent.ACTION_UP:
                mEventMode = MotionEvent.ACTION_UP;
                mFigure.setEndXY(event.getX(), event.getY());
                mCanvas.drawBitmap(getNewBitmap(), 0, 0, null);
                mFigure.draw(mCanvas, mPaint);
                mCanvas.drawBitmap(getStateBitmap(), 0, 0, null);
                break;
            case MotionEvent.ACTION_MOVE:
                mEventMode = MotionEvent.ACTION_MOVE;
                mFigure.setEndXY(event.getX(), event.getY());
                mCanvas.drawBitmap(getTempBitmap(), 0, 0, null);
                mFigure.draw(mCanvas, mPaint);
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == this.getId()) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
        }
        return false;
    }

    private Bitmap getStateBitmap() {
        mCanvas = new Canvas(mStateBitmap);
        mTempBitmap = mStateBitmap;
        return mStateBitmap;
    }

    private Bitmap getTempBitmap() {
        mTempBitmap = mStateBitmap;
        return mTempBitmap;
    }

    private Bitmap getNewBitmap() {
        toNextState();
        mStateBitmap = mTempBitmap;
        return mStateBitmap;
    }

    public void switchMode(int mode) {
        switch (mode) {
            case MODE_ARROW:
                mPaint.setStyle(Paint.Style.STROKE);
                mFigure = new Arrow();
                break;
            case MODE_CIRCLE:
                mPaint.setStyle(Paint.Style.STROKE);
                mFigure = new Circle();
                break;
            case MODE_RECT:
                mPaint.setStyle(Paint.Style.STROKE);
                mFigure = new Rect();
                break;
            case MODE_TEXT:
                mPaint.setStyle(Paint.Style.FILL);
                mFigure = new Text(mPaintText, mTextSize);
                break;
        }
    }

    public void back() {
        mEventMode = 3;
        if (mState.prevBuffer == null) {
            restoreBuffer(mState.startBuffer);
        } else {
            restoreBuffer(mState.prevBuffer);
        }
        toPreviousState();
        invalidate();
    }

    public void clear() {
        mEventMode = 4;
        restoreBuffer(mState.startBuffer);
        toStartState();
        invalidate();
    }

    public void saveImage() throws FileNotFoundException {
        if (fileName == null || "".equals(fileName)) return;
        FileOutputStream fos = new FileOutputStream(fileName);
        getStateBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
    }

    private void toStartState() {
        mState.prevBuffer = saveBuffer();
        mState.startBuffer = saveBuffer();
        mState.isPrev = false;
    }

    private void toPreviousState() {
        mState.prevBuffer = saveBuffer();
        mState.isPrev = false;
    }

    private void toNextState() {
        mState.prevBuffer = saveBuffer();
        mState.isPrev = true;
    }

    private byte[] saveBuffer() {
        byte[] buffer = new byte[mStateBitmap.getRowBytes() * mStateBitmap.getHeight()];
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        mStateBitmap.copyPixelsToBuffer(byteBuffer);
        return buffer;
    }

    private void restoreBuffer(byte[] buffer) {
        if (buffer == null) return;
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        mStateBitmap.copyPixelsFromBuffer(byteBuffer);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        mStateBitmap = PainterView.createThumbnail(fileName, 800);
        mState.startBuffer = saveBuffer();
        invalidate();
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public int getPaintColor() {
        return mPaint.getColor();
    }

    public void setPaintWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    public static class State {
        public byte[] prevBuffer = null;
        public byte[] startBuffer = null;
        public boolean isPrev = false;
    }

    public static Bitmap createThumbnail(String file, int squareSize) {
        InputStream inputStream = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            inputStream = new FileInputStream(file);
            options.inSampleSize = PainterView.calculateInSampleSize(inputStream, squareSize, squareSize);
            inputStream.close();
            inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap != null) {
                return rotateBitmap(file, bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    public static int calculateInSampleSize(InputStream fileStream, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(fileStream, null, options);
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(src);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);

            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
}
