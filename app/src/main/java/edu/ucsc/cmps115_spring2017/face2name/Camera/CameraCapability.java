package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public interface CameraCapability {
    void onAttach(Camera camera);

    Bitmap onPreFrame(Camera camera);

    void onFrame(Bitmap bitmap, Camera camera);

    void onRelease(Camera camera);
}
