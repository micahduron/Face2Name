package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

/**
 * Created by micah on 4/24/17.
 */

public class OrientationCapability extends CameraCapability {
    public enum OrientationSetting {
        PORTRAIT,
        LANDSCAPE
    }

    public OrientationCapability(OrientationSetting setting) {
        mOrientationSetting = setting;
    }

    @Override
    protected void onAttach(Camera camera) {
        int cameraAngle = OrientationCapability.calcCameraAngle(mOrientationSetting);
        camera.setDisplayOrientation(cameraAngle);
    }

    @Override
    protected Bitmap onPreFrame(Camera camera) {
        return null;
    }

    @Override
    protected void onFrame(Bitmap bitmap, Camera camera) {

    }

    @Override
    protected void onRelease(Camera camera) {

    }

    private static int calcCameraAngle(OrientationSetting setting) {
        switch (setting) {
            case PORTRAIT:
                return 270;
            case LANDSCAPE:
                return 0;
            default:
                throw new IllegalArgumentException("Unknown orientation setting.");
        }
    }

    private OrientationSetting mOrientationSetting;
}
