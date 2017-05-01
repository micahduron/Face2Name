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
        mCancelInit = false;

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
        cleanup();
    }

    private void cleanup() {
        mInitState = InitState.UNINITIALIZED;
        mCameraId = -1;
    }

    public interface Callbacks {
        void onCameraStart();
        void onCameraRelease();
        void onCameraError(Exception ex);
    }

    /** Helper classes **/
    private static class StarterResult {
        Camera camera;
        Exception ex;
    }

    private class CameraStarter extends AsyncTask<Integer, Void, StarterResult> {
        @Override
        protected StarterResult doInBackground(Integer... cameraId) {
            StarterResult result = new StarterResult();

            try {
                result.camera = Camera.open(cameraId[0]);
            } catch (Exception ex) {
                result.ex = ex;
            }
            return result;
        }

        @Override
        protected void onPostExecute(StarterResult result) {
            if (result.camera == null) {
                mCallbacks.onCameraError(result.ex);
                cleanup();

                return;
            }
            Camera camera = result.camera;

            if (mCancelInit) {
                camera.release();

                return;
            }
            mInitState = InitState.FINISHED;
            mCamera = camera;

            mCallbacks.onCameraStart();
        }
    }

    private int mCameraId;
    private Camera mCamera;
    private Callbacks mCallbacks;
    private boolean mCancelInit;
    private InitState mInitState;
}
