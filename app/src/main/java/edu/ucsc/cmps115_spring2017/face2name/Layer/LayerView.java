package edu.ucsc.cmps115_spring2017.face2name.Layer;

/**
 * Created by Chris Myau on 4/25/2017.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.IllegalFormatCodePointException;

public final class LayerView extends View
{
    private Paint mPaint;
    private int mColor;

    public LayerView(Context context) {
        super(context);
        init();
    }
    public LayerView(Context context, AttributeSet set) {
        super(context, set);
        init();
    }

    public LayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

        //initialize draw variables.
        mColor =  Color.parseColor("#e60000");
        mPaint = new Paint();
        mPaint.setColor(mColor); // set mColor
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10); // set stroke width
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draws a rectangle in the middle rectangle of a 3 x 3 grid
        canvas.drawRect(getLeft()+(getRight()-getLeft())/3,
                getTop()+(getBottom()-getTop())/3,
                getRight()-(getRight()-getLeft())/3,
                getBottom()-(getBottom()-getTop())/3, mPaint);
    }

    public int getRectPoint(coordinate_sections bound) throws IllegalArgumentException{
        //returns the coordinates of a given bound that we are looking for
        switch(bound){
            case LEFT_X:
                return getLeft()+(getRight()-getLeft())/3;
            case RIGHT_X:
                return getRight()-(getRight()-getLeft())/3;
            case UPPER_Y:
                return getTop()+(getBottom()-getTop())/3;
            case LOWER_Y:
                return getBottom()-(getBottom()-getTop())/3;
        }
        throw new IllegalArgumentException("Unexpected enum");
    }

    public enum coordinate_sections {
        LEFT_X, RIGHT_X, UPPER_Y, LOWER_Y
    }

}
