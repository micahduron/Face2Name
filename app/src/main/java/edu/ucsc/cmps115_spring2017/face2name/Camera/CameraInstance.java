package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.hardware.Camera;
import android.os.AsyncTask;

/**
 * Created by micah on 4/23/17.
 */

final class CameraInstance {
    public enum InitState {
        UNINITIALIZED,
        RUNNING,
        FINISHED
    }

    {
        mInitState = InitState.UNINITIALIZED;
        mCameraId = -1;
    }

    public CameraInstance() {}

    public CameraInstance(int cameraId, Callbacks callbacks) {
        open(cameraId, callbacks);
    }

    public void open(int cameraId, Callbacks callbacks) {
        if (getInitState() != InitState.UNINITIALIZED) {
            throw new RuntimeException("Cannot call open while object is initialized.");
        }
        mCameraId = cameraId;
        mCallbacks = callbacks;

        mInitState = InitState.RUNNING;

        (new CameraStarter()).execute(cameraId);
    }

    public InitState getInitState() {
        return mInitState;
    }

    public boolean isInitializing() {
        return getInitState() == InitState.RUNNING;
    }

    public boolean isInitialized() {
        return getInitState() == InitState.FINISHED;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public Camera.CameraInfo getCameraInfo() {
        return getCameraInfo(new Camera.CameraInfo());
    }

    public Camera.CameraInfo getCameraInfo(Camera.CameraInfo info) {
        Camera.getCameraInfo(getCameraId(), info);

        return info;
    }

    public void release() {
        InitState initState = getInitState();

        if (initState == InitState.FINISHED) {
            mCallbacks.onCameraRelease();

            mCamera.release();
            mCamera = null;
        } else if (initState == InitState.RUNNING) {
            mCancelInit = true;
        }
        mInitState = InitState.UNINITIALIZED;
        mCameraId = -1;
    }

    public interface Callbacks {
        void onCameraStart();
        void onCameraRelease();
    }

    /** Helper classes **/
    private class CameraStarter extends AsyncTask<Integer, Void, Camera> {
        @Override
        protected Camera doInBackground(Integer... cameraId) {
            return Camera.open(cameraId[0]);
        }

        @Override
        protected void onPostExecute(Camera cameraInst) {
            if (mCancelInit) {
                cameraInst.release();
                mCancelInit = false;

                return;
            }
            mInitState = InitState.FINISHED;
            mCamera = cameraInst;

            mCallbacks.onCameraStart();
        }
    }

    private int mCameraId;
    private Camera mCamera;
    private Callbacks mCallbacks;
    private boolean mCancelInit;
    private InitState mInitState;
}
