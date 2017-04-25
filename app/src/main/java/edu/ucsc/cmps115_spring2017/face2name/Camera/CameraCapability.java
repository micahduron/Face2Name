package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public abstract class CameraCapability {
    abstract protected void onAttach(Camera camera);

    abstract protected Bitmap onPreFrame(Camera camera);

    abstract protected void onFrame(Bitmap bitmap, Camera camera);

    abstract protected void onRelease(Camera camera);
}
