package edu.ucsc.cmps115_spring2017.face2name.Utils;

import android.graphics.Rect;

/**
 * Created by micah on 5/23/17.
 */

public final class GraphicsUtils {
    public static Rect openCVToAndroidRect(org.opencv.core.Rect openCVRect) {
        return new Rect(
                openCVRect.x,
                openCVRect.y,
                openCVRect.x + openCVRect.width,
                openCVRect.y + openCVRect.height
        );
    }

    public static android.graphics.Rect[] openCVToAndroidRects(org.opencv.core.Rect[] openCVRects) {
        android.graphics.Rect[] ret = new android.graphics.Rect[openCVRects.length];

        for (int i = 0; i < openCVRects.length; ++i) {
            ret[i] = openCVToAndroidRect(openCVRects[i]);
        }
        return ret;
    }

    public static org.opencv.core.Rect androidToOpenCVRect(Rect androidRect) {
        return new org.opencv.core.Rect(
                androidRect.left,
                androidRect.top,
                androidRect.width(),
                androidRect.height()
        );
    }

    public static org.opencv.core.Rect[] androidToOpenCVRects(android.graphics.Rect[] androidRects) {
        org.opencv.core.Rect[] ret = new org.opencv.core.Rect[androidRects.length];

        for (int i = 0; i < androidRects.length; ++i) {
            ret[i] = androidToOpenCVRect(androidRects[i]);
        }
        return ret;
    }
}
