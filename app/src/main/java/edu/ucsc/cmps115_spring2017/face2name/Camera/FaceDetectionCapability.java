package edu.ucsc.cmps115_spring2017.face2name.Camera;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public final class FaceDetectionCapability extends CameraCapability implements Camera.FaceDetectionListener {
    public FaceDetectionCapability(FaceDetectionListener listener) {
        mListener = listener;
    }

    void startFaceDetection() {
        if (mCameraInst == null) {
            throw new RuntimeException("Cannot call startFaceDetection in an uninitialized state.");
        }
        mCameraInst.getCamera().startFaceDetection();
    }

    void stopFaceDetection() {
        if (mCameraInst == null) {
            throw new RuntimeException("Cannot call stopFaceDetection in an uninitialized state.");
        }
        mCameraInst.getCamera().stopFaceDetection();
    }

    public interface FaceDetectionListener {
        void onFaceDetection(Face[] faces);
    }

    public class Face {
        Face(Camera.Face face) {
            mRect = face.rect;
        }

        Rect getRect() {
            return mRect;
        }

        private Rect mRect;
    }

    @Override
    public void onAttach(CameraInstance cameraInst) {
        mCameraInst = cameraInst;
        Camera.Parameters cameraParams = mCameraInst.getCamera().getParameters();

        if (cameraParams.getMaxNumDetectedFaces() == 0) {
            throw new RuntimeException("The current camera does not support face detection.");
        }
        mCameraInst.getCamera().setFaceDetectionListener(this);
    }

    @Override
    public Bitmap onPreFrame(CameraInstance cameraInst) {
        return null;
    }

    @Override
    public void onFrame(Bitmap bitmap, CameraInstance cameraInst) {

    }

    @Override
    public void onRelease(CameraInstance cameraInst) {
        mCameraInst.getCamera().stopFaceDetection();
        mCameraInst = null;
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Face[] faceObjs = new Face[faces.length];

        for (int i = 0; i < faces.length; ++i) {
            faceObjs[i] = new Face(faces[i]);
        }
        mListener.onFaceDetection(faceObjs);
    }

    private CameraInstance mCameraInst;
    private FaceDetectionListener mListener;
}
