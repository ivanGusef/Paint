package com.ivangusev.paint;

import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
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

    private static final String DST_FOLDER_NAME = "MARM_PAINT";
    private static final File DST_FOLDER;

    private static final int CANVAS_WIDTH = 512;
    private static final int CANVAS_HEIGHT = 384;

    public static final int MODE_ARROW = 0;
    public static final int MODE_CIRCLE = 1;
    public static final int MODE_RECT = 2;
    public static final int MODE_TEXT = 3;

    static {
        DST_FOLDER = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DST_FOLDER_NAME);
        if (!DST_FOLDER.exists() && !DST_FOLDER.mkdirs()) {
            Log.e("PainterView", "Directory " + DST_FOLDER.getPath() + " can not be created");
        }
    }

    public String mPaintText = "";
    public float mTextSize = 14;

    private Paint mPaint;

    private Bitmap mStateBitmap;
    private Bitmap mTempBitmap;

    private Canvas mCanvas;
    private Figure mFigure;
    private State mState;

    private int mEventMode;

    private Handler mHandler;
    private String mBitmapSrc;

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
        mHandler = new Handler();

        mStateBitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);

        setOnTouchListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (mEventMode = event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFigure.setStartXY(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mFigure.setEndXY(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mFigure.setEndXY(event.getX(), event.getY());
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mEventMode) {
            case MotionEvent.ACTION_MOVE:
                canvas.drawBitmap(getTempBitmap(), 0, 0, null);
                mFigure.draw(canvas, mPaint);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawBitmap(getNewBitmap(), 0, 0, null);
                mFigure.draw(mCanvas, mPaint);
                canvas.drawBitmap(getStateBitmap(), 0, 0, null);
                break;
            default:
                canvas.drawBitmap(getStateBitmap(), 0, 0, null);
                break;
        }
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
        mEventMode = -100;
        if (mState.prevBuffer == null) {
            restoreBuffer(mState.startBuffer);
        } else {
            restoreBuffer(mState.prevBuffer);
        }
        toPreviousState();
        invalidate();
    }

    public void clear() {
        mEventMode = -100;
        restoreBuffer(mState.startBuffer);
        toStartState();
        invalidate();
    }

    public String saveImage() {
        ByteArrayOutputStream baos = null;
        FileOutputStream fos = null;
        try {
            baos = new ByteArrayOutputStream();
            getStateBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);

            final File dst = new File(DST_FOLDER, String.valueOf(System.currentTimeMillis() + ".jpg"));
            fos = new FileOutputStream(dst);
            fos.write(baos.toByteArray());
            return dst.getPath();
        } catch (IOException e) {
            Log.e("PainterView -> saveImage", e.getMessage(), e);
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    private void toStartState() {
        mState.prevBuffer = saveBuffer();
        mState.startBuffer = saveBuffer();
    }

    private void toPreviousState() {
        mState.prevBuffer = saveBuffer();
    }

    private void toNextState() {
        mState.prevBuffer = saveBuffer();
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

    public void setBitmapSrc(String fileName) {
        mBitmapSrc = fileName;
        post(new BitmapPreparer(fileName));
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

    private void recalcLayoutParams(int width, int height) {
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
    }

    public static class State {
        public byte[] prevBuffer = null;
        public byte[] startBuffer = null;
    }

    public static Bitmap createThumbnail(String file, int width, int height) {
        InputStream inputStream = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            inputStream = new FileInputStream(file);
            options.inSampleSize = PainterView.calculateInSampleSize(inputStream, width, height);
            inputStream.close();
            inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap != null) {
                return scaleBitmap(rotateBitmap(file, bitmap), width, height);
            }
            return bitmap;
        } catch (IOException e) {
            Log.e("PainterView -> createThumbnail", e.getMessage(), e);
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

    public static Bitmap decodeBitmap(String file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e("PainterView -> createThumbnail", e.getMessage(), e);
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

    public static Bitmap scaleBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        Matrix matrix = new Matrix();
        matrix.preScale((float) reqWidth / bitmap.getWidth(), (float) reqHeight / bitmap.getHeight());
        final Bitmap immutableBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        final Bitmap mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        immutableBitmap.recycle();
        return mutableBitmap;
    }

    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(src);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);

            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
                final Bitmap immutableBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                final Bitmap mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
                immutableBitmap.recycle();
                return mutableBitmap;
            }
        } catch (IOException e) {
            Log.e("PainterView -> rotateAndScaleBitmap", e.getMessage(), e);
        }
        return bitmap;
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

    private class BitmapPreparer implements Runnable {

        private final String fileName;

        private BitmapPreparer(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            final FrameLayout parent = (FrameLayout) getParent();
            final int maxWidth = parent.getMeasuredWidth();
            final int maxHeight = parent.getMeasuredHeight();
            if (fileName == null) {
                mStateBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mStateBitmap);
                mCanvas.drawColor(Color.WHITE);
                mState.startBuffer = saveBuffer();
            } else {
                Bitmap bitmap = PainterView.rotateBitmap(fileName, PainterView.decodeBitmap(fileName));
                final int bitmapMaxWidth = bitmap.getWidth();
                final int bitmapMaxHeight = bitmap.getHeight();
                final float factor = (float) bitmapMaxWidth / bitmapMaxHeight;
                bitmap.recycle();

                final int decodeWidth, decodeHeight;
                if (bitmapMaxWidth > maxWidth || bitmapMaxHeight > maxHeight) {
                    decodeWidth = Math.min(bitmapMaxWidth, maxWidth);
                    decodeHeight = Math.round(decodeWidth / factor);
                    mStateBitmap = PainterView.createThumbnail(fileName, decodeWidth, decodeHeight);
                } else {
                    mStateBitmap = PainterView.createThumbnail(fileName, bitmapMaxWidth, bitmapMaxHeight);
                }
                mState.startBuffer = saveBuffer();
            }
        }
    }
}
