package edu.ucsc.cmps115_spring2017.face2name.Utils;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by micah on 5/27/17.
 */

/**
 * A unified rectangle class. This class enable the seamless conversion between all of the different
 * rectangle types while also providing some useful transformation methods.
 */
public class Rectangle extends RectF {
    public Rectangle() {
        super();
    }

    public Rectangle(RectF other) {
        super(other);
    }

    public Rectangle(android.graphics.Rect rect) {
        super(rect);
    }

    public Rectangle(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }

    public Rectangle(org.opencv.core.Rect openCVRect) {
        this.left = openCVRect.x;
        this.top = openCVRect.y;
        this.bottom = openCVRect.y + openCVRect.height;
        this.right = openCVRect.x + openCVRect.width;
    }

    public void scale(float scaleFactor) {
        scale(scaleFactor, scaleFactor);
    }

    public void scale(float scaleFactorX, float scaleFactorY) {
        float deltaY = (scaleFactorY - 1) * (this.bottom - this.top) / 2;

        this.top -= deltaY;
        this.bottom += deltaY;

        float deltaX = (scaleFactorX - 1) * (this.right - this.left) / 2;

        this.left -= deltaX;
        this.right += deltaX;
    }

    public PointF getCenter() {
        return new PointF(centerX(), centerY());
    }

    public android.graphics.Rect toRect() {
        return toRect(new android.graphics.Rect());
    }

    public android.graphics.Rect toRect(android.graphics.Rect rect) {
        rect.left = (int) this.left;
        rect.top = (int) this.top;
        rect.right = (int) this.right;
        rect.bottom = (int) this.bottom;

        return rect;
    }

    public org.opencv.core.Rect toOpenCVRect() {
        return toOpenCVRect(new org.opencv.core.Rect());
    }

    public org.opencv.core.Rect toOpenCVRect(org.opencv.core.Rect rect) {
        rect.x = (int) this.left;
        rect.y = (int) this.top;
        rect.width = (int) width();
        rect.height = (int) height();

        return rect;
    }
}
