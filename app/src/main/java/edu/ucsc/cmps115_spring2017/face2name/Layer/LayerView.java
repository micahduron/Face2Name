package edu.ucsc.cmps115_spring2017.face2name.Layer;

/**
 * Created by Chris Myau on 4/25/2017.
 */

import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public final class LayerView extends View
{
    private Rect rectangle;
    private Paint paint;
    //just example code
    public LayerView(Context context) {
        super(context);
        int x = 50;
        int y = 50;
        int sideLength = 200;
        rectangle = new Rect(x, y, sideLength, sideLength);

        paint = new Paint();
        paint.setColor(Color.GRAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        canvas.drawRect(rectangle, paint);
    }
}
