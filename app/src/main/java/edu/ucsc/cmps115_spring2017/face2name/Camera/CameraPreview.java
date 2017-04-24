package edu.ucsc.cmps115_spring2017.face2name.Camera;

/**
 * Created by micah on 4/19/17.
 */

import edu.ucsc.cmps115_spring2017.face2name.CameraCapabilities.CameraCapability;
import android.util.AttributeSet;
import android.content.Context;
import android.view.TextureView;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap;
import android.hardware.Camera;
import java.lang.*;
import java.io.*;



public final class CameraPreview
        extends TextureView
        implements TextureView.SurfaceTextureListener, CameraInstance.Callbacks
{
    /** Configuration constants **/
    public static final int BACK_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int FRONT_CAMERA = Camera.CameraInfo.CAMERA_FACING_FRONT;

    {
        mCameraInst = new CameraInstance();
    }

    /** Constructors **/
    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** Public methods **/
    public void init(int cameraType, PreviewCallbacks callbacks) {
        if (isInitialized()) {
            throw new RuntimeException("Cannot call init while object is initialized.");
        }
        mCallbacks = callbacks;

        int cameraId = CameraPreview.findCameraIdByType(cameraType);

        if (cameraId == -1) {
            throw new RuntimeException("Could not find a camera of the given type.");
        }
        mCameraInst.open(cameraId, this);

        setSurfaceTextureListener(this);
    }

    public void init(int cameraType, PreviewCallbacks callbacks, CameraCapability... capabilities) {
        setCapabilities(capabilities);
        init(cameraType, callbacks);
    }

    public void setCapabilities(CameraCapability... capabilities) {
        releaseCapabilities();

        mCapabilities = capabilities;
        initializeCapabilities();
    }

    public void releaseCapabilities() {
        if (!isInitialized()) return;
        if (mCapabilities == null) return;

        for (final CameraCapability cap : mCapabilities) {
            cap.onRelease(getCamera());
        }
    }

    public void release() {
        mCameraInst.release();
    }

    public void startPreview() {
        if (!isReady()) {
            throw new RuntimeException("Cannot call startPreview in an unready state.");
        }
        getCamera().startPreview();
    }

    public void stopPreview() {
        if (!isReady()) {
            throw new RuntimeException("Cannot call stopPreview in an unready state.");
        }
        getCamera().stopPreview();
    }

    public boolean isInitialized() {
        return mCameraInst.isInitialized();
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
        Bitmap bitmap = createBitmap();

        if (mCapabilities != null) {
            for (final CameraCapability cap : mCapabilities) {
                cap.onPreviewFrame(bitmap, mCamera);
            }
        }
        mCallbacks.onPreviewFrame(bitmap);
    }

    @Override
    public void onCameraStart() {
        try {
            getCamera().setPreviewTexture(getSurfaceTexture());

            mCallbacks.onCameraStart();

            initializeCapabilities();

            tryOnCameraReady();
        } catch (IOException ex) {
            mCameraInst.release();
            // ...
        }
    }

    @Override
    public void onCameraRelease() {
        releaseCapabilities();

        mCallbacks.onCameraRelease();

        mReadyCallbackExecuted = false;
    }

    protected Camera getCamera() {
        return mCameraInst.getCamera();
    }

    /** Private methods **/


    private void initializeCapabilities() {
        if (!isInitialized()) return;
        if (mCapabilities == null) return;

        for (final CameraCapability cap : mCapabilities) {
            cap.onAttach(getCamera());
        }
    }

    private void tryOnCameraReady() {
        if (!mReadyCallbackExecuted && isReady()) {
            Camera.Size previewDimensions = getCamera().getParameters().getPreviewSize();
            mCallbacks.onCameraReady(previewDimensions.width, previewDimensions.height);

            mReadyCallbackExecuted = true;
        }
    }

    private void setCameraTexture() {
        if (!isInitialized()) return;

        try {
            getCamera().setPreviewTexture(getSurfaceTexture());
        } catch (IOException ex) {
            mCameraInst.release();
            // ...
        }
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

    /** Data members **/
    private PreviewCallbacks mCallbacks;
    private CameraInstance mCameraInst;
    private Bitmap mBitmap;
    private boolean mReadyCallbackExecuted;
    private CameraCapability[] mCapabilities;
}
