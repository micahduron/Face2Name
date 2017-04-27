package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public abstract class CameraCapability {
    abstract protected void onAttach(CameraInstance cameraInst);

    abstract protected Bitmap onPreFrame(CameraInstance cameraInst);

    abstract protected void onFrame(Bitmap bitmap, CameraInstance cameraInst);

    abstract protected void onRelease(CameraInstance cameraInst);
}
