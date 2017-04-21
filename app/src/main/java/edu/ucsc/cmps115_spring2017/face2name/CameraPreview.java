package edu.ucsc.cmps115_spring2017.face2name;

/**
 * Created by micah on 4/19/17.
 */

import android.util.AttributeSet;
import android.content.Context;
import android.view.TextureView;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import java.lang.*;
import java.io.*;



public final class CameraPreview extends TextureView implements TextureView.SurfaceTextureListener {
    /** Configuration constants **/
    public static final int BACK_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int FRONT_CAMERA = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /** Constructors **/
    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** Public methods **/
    public void init(int cameraType, PreviewCallbacks callbacks) {
        release();

        mCallbacks = callbacks;

        int cameraId = CameraPreview.findCameraIdByType(cameraType);

        if (cameraId == -1) {
            throw new RuntimeException("Could not find a camera of the given type.");
        }
        mCameraStarter = new CameraStarter();
        mCameraStarter.execute(cameraId);

        setSurfaceTextureListener(this);
    }

    public void release() {
        if (isInitialized()) {
            uninitializeCamera();
        } else if (mCameraStarter.getStatus() == AsyncTask.Status.RUNNING) {
            mCameraStarter.cancel(false);
        }
        mReadyCallbackExecuted = false;
    }

    public void startPreview() {
        if (!isReady()) {
            throw new RuntimeException("Cannot call startPreview in an unready state.");
        }
        mCamera.startPreview();
    }

    public void stopPreview() {
        if (!isReady()) {
            throw new RuntimeException("Cannot call stopPreview in an unready state.");
        }
        mCamera.stopPreview();
    }

    public boolean isInitialized() {
        return mCamera != null;
    }

    public boolean isReady() {
        return isInitialized() && (getSurfaceTexture() != null);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void setCallbacks(PreviewCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    /** Interfaces **/
    public interface PreviewCallbacks {
        /**
         * Executed when a connection to the camera has been established. It does not indicate
         * that the object is ready to start fetching preview frames.
         */
        void onCameraStart();

        /**
         * Executed immediately before the release of a camera connection.
         */
        void onCameraRelease();

        /**
         * Executed when the camera is ready to fetch preview frames. This is always executed
         * after the corresponding call to onCameraStart().
         * @param width -- Preview's width in pixels.
         * @param height -- Preview's height in pixels.
         */
        void onCameraReady(int width, int height);

        /**
         * Executed when the size of the preview frame changes.
         * @param width -- Preview's new width in pixels.
         * @param height -- Preview's new height in pixels.
         */
        void onPreviewResize(int width, int height);

        /**
         * Executed on every preview frame.
         * @param bitmap -- A Bitmap object containing the current preview frame's image data.
         */
        void onPreviewFrame(Bitmap bitmap);
    }

    /** TextureView.SurfaceTextureListener overrides **/
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        setCameraTexture();

        tryOnCameraReady();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        release();

        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        // Nothing needs to be done here.
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        mCallbacks.onPreviewFrame(createBitmap());
    }

    /** Private methods **/
    private void uninitializeCamera() {
        if (!isInitialized()) {
            throw new RuntimeException("Cannot call uninitializeCamera in an uninitialized state.");
        }
        mCamera.stopPreview();

        mCallbacks.onCameraRelease();

        mCamera.release();
        mCamera = null;
    }

    private void setupCamera(Camera camera) {
        try {
            setCameraTexture(camera);

            mCamera = camera;

            mCallbacks.onCameraStart();

            tryOnCameraReady();
        } catch (IOException ex) {
            camera.release();
            // ...
        }
    }

    private void tryOnCameraReady() {
        if (!mReadyCallbackExecuted && isReady()) {
            Camera.Size previewDimensions = mCamera.getParameters().getPreviewSize();
            mCallbacks.onCameraReady(previewDimensions.width, previewDimensions.height);

            mReadyCallbackExecuted = true;
        }
    }

    private void setCameraTexture() {
        if (!isInitialized()) return;

        try {
            setCameraTexture(mCamera);
        } catch (IOException ex) {
            uninitializeCamera();
            // ...
        }
    }

    private void setCameraTexture(Camera camera) throws IOException {
        camera.setPreviewTexture(getSurfaceTexture());
    }

    private Bitmap createBitmap() {
        return mBitmap == null ? getBitmap() : getBitmap(mBitmap);
    }

    /** Static helper functions **/
    private static int findCameraIdByType(int cameraType) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        for (int currId = 0; currId < Camera.getNumberOfCameras(); ++currId) {
            Camera.getCameraInfo(currId, cameraInfo);

            if (cameraInfo.facing == cameraType) {
                return currId;
            }
        }
        return -1;
    }

    /** Helper classes **/
    private class CameraStarter extends AsyncTask<Integer, Void, Camera> {
        @Override
        protected Camera doInBackground(Integer... cameraId) {
            return Camera.open(cameraId[0]);
        }

        @Override
        protected void onCancelled(Camera cameraInst) {
            cameraInst.release();
        }

        @Override
        protected void onPostExecute(Camera cameraInst) {
            setupCamera(cameraInst);
        }
    }

    /** Data members **/
    private PreviewCallbacks mCallbacks;
    private Camera mCamera;
    private CameraStarter mCameraStarter;
    private Bitmap mBitmap;
    private boolean mReadyCallbackExecuted;
}
