package edu.ucsc.cmps115_spring2017.face2name.Layer;

/**
 * Created by Chris Myau on 4/25/2017.
 */

import android.content.Context;

import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import 	android.view.MotionEvent;


public final class LayerView extends View
{
    private Paint paint;
    int color;
    int param1, param2, param3, param4;
    public LayerView(Context context) {
        super(context);
    }
    public LayerView(Context context, AttributeSet set) {
        super(context, set);
        paint = new Paint();
        color =  Color.parseColor("#e60000");
    }

    public LayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(color); // set color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10); // set stroke width
        canvas.drawRect(getLeft()+(getRight()-getLeft())/3,
                getTop()+(getBottom()-getTop())/3,
                getRight()-(getRight()-getLeft())/3,
                getBottom()-(getBottom()-getTop())/3, paint);
    }

    public int getRectPoint(String point){
        switch(point){
            case "Left X":
                return getLeft()+(getRight()-getLeft())/3;
            case "Right X":
                return getRight()-(getRight()-getLeft())/3;
            case "Upper Y":
                return getTop()+(getBottom()-getTop())/3;
            case "Lower Y":
                return getBottom()-(getBottom()-getTop())/3;
        }
        return 0;
    }

}
