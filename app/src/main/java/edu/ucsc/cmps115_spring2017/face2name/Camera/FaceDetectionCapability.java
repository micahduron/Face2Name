package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public final class FaceDetectionCapability extends CameraCapability implements Camera.FaceDetectionListener {
    public void startFaceDetection() {
        if (mCameraInst == null) {
            throw new RuntimeException("Cannot call startFaceDetection in an uninitialized state.");
        }
        mCameraInst.getCamera().startFaceDetection();
    }

    public void stopFaceDetection() {
        if (mCameraInst == null) {
            throw new RuntimeException("Cannot call stopFaceDetection in an uninitialized state.");
        }
        mCameraInst.getCamera().stopFaceDetection();
        mFaces = null;
    }

    /**
     * Returns the number of faces the capability is detecting at the current moment.
     * @return Number of detected faces.
     */
    public int getNumDetectedFaces() {
        return mFaces != null ? mFaces.length : 0;
    }

    public Face[] getFaces() {
        if (mFaces == null) return null;

        Face[] faces = new Face[mFaces.length];

        for (int i = 0; i < mFaces.length; ++i) {
            faces[i] = new Face(mFaces[i]);
        }
        return faces;
    }

    public class Face {
        final static int NORMALIZED_WIDTH = 2000;
        final static int NORMALIZED_HEIGHT = 2000;

        Face(Camera.Face face) {
            mRect = new RectF(face.rect);
        }

        /**
         * Returns a face's bounding rectangle with respect to a reference coordinate system
         * ranging from the points (-1000, -1000) to (1000, 1000).
         * @return A face's bounding rectangle normalized to a reference coordinate system.
         */
        public RectF getRect() {
            return mRect;
        }

        final private RectF mRect;
    }

    /**
     * Returns an affine transform that converts a normalized face rectangle to one conforming to
     * specific screen parameters.
     * @param angle Camera's orientation angle. The same value used to call Camera.setOrientationAngle(int).
     * @param width Width (in pixels) of the camera frame.
     * @param height Height (in pixels) of the camera frame.
     * @return Transformation matrix.
     */
    public static Matrix getFaceTransform(int angle, int width, int height) {
        Matrix transform = new Matrix();

        float widthF = width;
        float heightF = height;

        transform.postRotate(angle);
        transform.postScale(widthF / Face.NORMALIZED_WIDTH, heightF / Face.NORMALIZED_HEIGHT);
        transform.postTranslate(widthF / 2, heightF / 2);

        return transform;
    }

    @Override
    protected void onAttach(CameraInstance cameraInst) {
        mCameraInst = cameraInst;
        Camera.Parameters cameraParams = mCameraInst.getCamera().getParameters();

        if (cameraParams.getMaxNumDetectedFaces() == 0) {
            throw new RuntimeException("The current camera does not support face detection.");
        }
        mCameraInst.getCamera().setFaceDetectionListener(this);
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
        mCameraInst.getCamera().stopFaceDetection();
        mCameraInst = null;
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        mFaces = faces;
    }

    private CameraInstance mCameraInst;
    private Camera.Face[] mFaces;
}
