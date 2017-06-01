package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;

/**
 * Created by micah on 4/24/17.
 */

public class OrientationCapability extends CameraCapability {
    public OrientationCapability(Display windowDisplay) {
        mDisplay = windowDisplay;
    }

    public void updateOrientation() {
        if (mCameraInst == null) {
            throw new RuntimeException("Cannot call updateOrientation while detached.");
        }
        int displayAngle = getDisplayAngle();
        Camera.CameraInfo cameraInfo = mCameraInst.getCameraInfo();

        mCameraAngle = OrientationCapability.calcCameraAngle(cameraInfo, displayAngle);

        mCameraInst.getCamera().setDisplayOrientation(mCameraAngle);
    }

    public int getCameraAngle() {
        return mCameraAngle;
    }

    @Override
    protected void onAttach(CameraInstance cameraInst) {
        mCameraInst = cameraInst;

        updateOrientation();
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

    private int getDisplayAngle() {
        int displayRotation = mDisplay.getRotation();

        switch (displayRotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                throw new IllegalArgumentException("Unknown rotation value.");
        }
    }

    private static int calcCameraAngle(Camera.CameraInfo cameraInfo, int displayAngle) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            return (cameraInfo.orientation - displayAngle + 360) % 360;
        } else {
            int reflectedAngle = (cameraInfo.orientation + displayAngle) % 360;

            return (360 - reflectedAngle) % 360;
        }
    }

    private Display mDisplay;
    private CameraInstance mCameraInst;
    private int mCameraAngle;
}
