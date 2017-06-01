package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.graphics.PointF;

import org.opencv.core.Mat;
import org.opencv.core.CvType;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.Math;


import edu.ucsc.cmps115_spring2017.face2name.Identity.Identity;
import edu.ucsc.cmps115_spring2017.face2name.Utils.Image;
import edu.ucsc.cmps115_spring2017.face2name.Utils.Rectangle;

/**
 * Created by micah on 5/23/17.
 */

public final class FaceRecognition {
    public static final int RECOG_SUCCESS = 1;
    public static final int FACE_FOUND = 1 << 1;

    private static final int RECOG_FAILED = 0;

    public FaceRecognition(Context context) {
        try {
            mEyeDetector = new EyeDetector(context);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void initialize() {
        native_initialize();
    }

    private void initialize(List<Identity> identities) {
        initialize();

        for (final Identity ident : identities) {
            addToModel(ident.image, ident.key);
        }
    }
    private native void native_initialize();

    public Identity addFace(Image face, Long id) {
        Image normalizedFace = normalizeFace(face);

        if (normalizedFace == null) return null;

        addToModel(normalizedFace, id);

        return new Identity(id, null, normalizedFace);
    }

    private void addToModel(Image faceImage, Long id) {
        if (mIdSet.contains(id)) {
            throw new RuntimeException("ID already exists within model.");
        }
        mIdSet.add(id);

        native_addToModel(faceImage.getMat().getNativeObjAddr(), id);
    }

    private native void native_addToModel(long matPtr, long id);

    public RecognitionResult identify(Image faceImage) {
        Image normalizedFace = normalizeFace(faceImage);

        if (normalizedFace == null) {
            return new RecognitionResult(RECOG_FAILED);
        }
        IdentifyResult identResult = native_identify(normalizedFace.getMat().getNativeObjAddr());

        if ((identResult.status & RECOG_SUCCESS) == 0) {
            return new RecognitionResult(RECOG_FAILED);
        } else if ((identResult.status & FACE_FOUND) == 0) {
            return new RecognitionResult(RECOG_SUCCESS);
        }
        Identity ident = new Identity(identResult.id, null, normalizedFace);

        return new RecognitionResult(RECOG_SUCCESS | FACE_FOUND, ident);
    }

    private native IdentifyResult native_identify(long faceImagePtr);

    private Image normalizeFace(Image faceImage) {

        Mat faceImageMat = faceImage.getMat();
        Mat eyeMat = new Mat(2, 3, CvType.CV_32FC1); //2x3 Matrix holding 3 pairs of point coords
        List<Rectangle> eyeCentersList = mEyeDetector.detect(faceImage);

        if(eyeCentersList.size() != 2){
            //Log.e(EYE ARRAY SIZE ERRR, "eye detect array size= " + eyeCentersList.size());
            return null;
        }

        PointF third = thirdEquilateralPoint(eyeCentersList.get(0).getCenter(), eyeCentersList.get(1).getCenter());
        putInMat(eyeMat, 0, (int) eyeCentersList.get(0).centerX(), (int) eyeCentersList.get(0).centerY());
        putInMat(eyeMat, 1, (int) eyeCentersList.get(1).centerX(), (int) eyeCentersList.get(1).centerY());
        putInMat(eyeMat, 2, (int) third.x, (int) third.y);

        mMTransform = org.opencv.video.Video.estimateRigidTransform(faceImageMat, eyeMat, false);
        org.opencv.imgproc.Imgproc.warpAffine(faceImageMat, faceImageMat, mMTransform, faceImageMat.size());

        return new Image(faceImageMat);
    }
    //puts information into a Matrix
    private void putInMat(Mat m, int i, int x, int y){
        m.put(0, i, x);
        m.put(1, i, y);
    }
    //Creates third point of an equilateral triangle pointing downwards
    private PointF thirdEquilateralPoint(PointF point1, PointF point2){
        float length = Math.abs(point1.x - point2.x);
        return new PointF((point1.x + point2.x)/2, (float) (point1.y - Math.hypot(length, length/2)));
    }

    public native void close();


    private class IdentifyResult {
        int status;
        long id;

        IdentifyResult(int status) {
            this.status = status;
        }

        IdentifyResult(int status, long id) {
            this.status = status;
            this.id = id;
        }
    }

    public class RecognitionResult {
        RecognitionResult(int status) {
            mStatus = status;
        }

        RecognitionResult(int status, Identity identity) {
            mStatus = status;
            mIdentity = identity;
        }

        public boolean error() {
            return (mStatus & RECOG_SUCCESS) == 0;
        }

        public boolean faceFound() {
            return (mStatus & FACE_FOUND) != 0;
        }

        public Identity getIdentity() {
            return mIdentity;
        }

        private int mStatus;
        private Identity mIdentity;
    }

    private Set<Long> mIdSet = new HashSet<>();
    private EyeDetector mEyeDetector;
    private long mNativePtr;
    private Mat mMTransform;
}
