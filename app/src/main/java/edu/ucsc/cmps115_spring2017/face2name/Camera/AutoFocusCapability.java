package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.hardware.Camera.Parameters;

import java.util.List;

/**
 * Created by micah on 5/9/17.
 */

public final class AutoFocusCapability extends CameraCapability {
    @Override
    protected void onAttach(CameraInstance cameraInst) {
        Parameters cameraParams = cameraInst.getCamera().getParameters();
        List<String> focusModes = cameraParams.getSupportedFocusModes();

        if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mFocusMode = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        } else if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mFocusMode = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        } else {
            mFocusMode = Parameters.FOCUS_MODE_AUTO;
        }
        cameraParams.setFocusMode(mFocusMode);

        cameraInst.getCamera().setParameters(cameraParams);
    }

    @Override
    protected void onRelease(CameraInstance cameraInst) {
        mFocusMode = null;
    }

    public String getFocusMode() {
        return mFocusMode;
    }

    private String mFocusMode;
}
