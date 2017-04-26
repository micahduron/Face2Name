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
import android.view.SurfaceView;

public final class LayerView extends SurfaceView
{
    //just example code
    public LayerView(Context context) {
        super(context);
    }
    public LayerView(Context context, AttributeSet set) {
        super(context, set);
    }

    public LayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
