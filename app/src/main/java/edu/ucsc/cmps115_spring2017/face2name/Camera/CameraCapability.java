package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public abstract class CameraCapability {
    abstract protected void onAttach(CameraInstance cameraInst);

    protected Bitmap onPreFrame(CameraInstance cameraInst) {
        return null;
    }

    protected void onFrame(Bitmap bitmap, CameraInstance cameraInst) {}

    protected void onRelease(CameraInstance cameraInst) {}
}
