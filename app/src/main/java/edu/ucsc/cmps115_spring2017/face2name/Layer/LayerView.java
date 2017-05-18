package edu.ucsc.cmps115_spring2017.face2name.Layer;

/**
 * Created by Chris Myau on 4/25/2017.
 */

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.TextureView;

public final class LayerView extends TextureView implements TextureView.SurfaceTextureListener
{
    private Paint mPaint;
    private int mColor;

    {
        //initialize draw variables.
        mColor =  Color.parseColor("#e60000");
        mPaint = new Paint();
        mPaint.setColor(mColor); // set mColor
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10); // set stroke width

        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    public LayerView(Context context) {
        super(context);
    }
    public LayerView(Context context, AttributeSet set) {
        super(context, set);
    }

    public LayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Drawer getDrawer() {
        return new Drawer();
    }

    public Rect getBoundingRect() {
        return new Rect(
                getLeft(),
                getTop(),
                getRight(),
                getBottom()
        );
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Drawer drawer = getDrawer();

        drawer.beginDrawing();
        drawer.clearScreen();
        drawer.endDrawing();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public class Drawer {
        public void beginDrawing() {
            mCanvas = lockCanvas();
        }

        public Canvas getCanvas() {
            if (mCanvas == null) {
                throw new RuntimeException("Must call beginDrawing() before drawing.");
            }
            return mCanvas;
        }

        public void clearScreen() {
            // The only argument that matters here in the call to drawColor is PorterDuff.Mode.CLEAR.
            // In Porter-Duff compositing, clear mode discards the pixels of all images to be
            // composited, leaving only a blank image behind.
            getCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        public void drawBox(float left, float top, float right, float bottom) {
            getCanvas().drawRect(left, top, right, bottom, mPaint);
        }

        public void drawBox(Rect box) {
            drawBox(box.left, box.top, box.right, box.bottom);
        }

        public void drawBox(RectF box) {
            drawBox(box.left, box.top, box.right, box.bottom);
        }

        public void endDrawing() {
            unlockCanvasAndPost(mCanvas);
            mCanvas = null;
        }

        private Canvas mCanvas;
    }
}
