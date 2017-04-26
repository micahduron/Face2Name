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
    protected void onAttach(CameraInstance cameraInst) {
        int cameraAngle = OrientationCapability.calcCameraAngle(mOrientationSetting);

        cameraInst.getCamera().setDisplayOrientation(cameraAngle);
    }

    @Override
    protected Bitmap onPreFrame(CameraInstance cameraInst) {
        return null;
    }

    @Override
    protected void onFrame(Bitmap bitmap, CameraInstance cameraInst) {

    }

    @Override
    protected void onRelease(CameraInstance cameraInst) {
        mCameraInst = null;
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
    private CameraInstance mCameraInst;
}
