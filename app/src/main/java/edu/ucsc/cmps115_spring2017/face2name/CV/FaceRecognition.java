package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.graphics.PointF;


import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

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


    private static final int STANDARD_IMAGE_HEIGHT = 100;
    private static final int STANDARD_IMAGE_WIDTH = 100;

    static {
        PointF[] eyePoints = new PointF[] {
                new PointF(STANDARD_IMAGE_WIDTH * 0.3f, STANDARD_IMAGE_HEIGHT / 3.0f),
                new PointF(STANDARD_IMAGE_WIDTH * 0.7f, STANDARD_IMAGE_HEIGHT / 3.0f)
        };
        mReferenceEyePoints = calcEyeMat(eyePoints);
    }

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

    public void initialize(List<Identity> identities) {
        initialize();

        train(identities);
    }

    private native void native_initialize();

    public void train(List<Identity> identities) {
        FaceModel faceModel = new FaceModel(identities.size());

        for (final Identity identity : identities) {
            faceModel.addToModel(identity);
        }
        native_trainModel(faceModel);

        faceModel.close();
    }

    private native void native_trainModel(FaceModel model);

    public Identity addFace(Image face) {
        Identity ident = Identity.makeIdentity(face);

        addToModel(ident.image, ident.key);

        return ident;
    }

    public void addFace(Identity ident) {
        addToModel(ident.image, ident.key);
    }

    private void addToModel(Image faceImage, long id) {
        if (mIdSet.contains(id)) {
            throw new RuntimeException("ID already exists within model.");
        }
        mIdSet.add(id);

        native_addToModel(faceImage.getMat().getNativeObjAddr(), Long.toString(id));
    }

    private native void native_addToModel(long matPtr, String id);

    public RecognitionResult identify(Image faceImage) {
        IdentifyResult identResult = new IdentifyResult();
        native_identify(faceImage.getMat().getNativeObjAddr(), identResult);

        if ((identResult.status & RECOG_SUCCESS) == 0) {
            return new RecognitionResult(RECOG_FAILED);
        } else if ((identResult.status & FACE_FOUND) == 0) {
            return new RecognitionResult(RECOG_SUCCESS);
        }
        Identity ident = new Identity(identResult.id, null, faceImage);
        return new RecognitionResult(RECOG_SUCCESS | FACE_FOUND, ident);
    }

    private native void native_identify(long faceImagePtr, IdentifyResult identResult);

    public void setConfidenceThreshold(double threshold) {
        mConfidenceThreshold = threshold;
    }

    public Image normalizeFace(Image faceImage) {
        //Get Matrix of Image
        Mat faceImageMat = faceImage.getMat();
        Mat tempMat = new Mat();

        //Changes the ImageMatrix to grayscale
        Imgproc.cvtColor(faceImageMat, tempMat, Imgproc.COLOR_RGB2GRAY);
        faceImageMat = tempMat;

        List<Rectangle> eyeRects = mEyeDetector.detect(faceImage);

        //Checks to see that exactly two eyes are detected
        if(eyeRects.size() != 2) {
            //Log.e(EYE ARRAY SIZE ERROR, "eye detect array size= " + eyeRects.size());
            return new Image(faceImageMat);
        }
        Mat eyeMat = calcEyeMat(eyeRects);

        //Apply estimateRigidTransform's transformation Matrix to the face's matrix using warpAffine

        Mat imageTransform = Video.estimateRigidTransform(eyeMat, mReferenceEyePoints, false);

        if (imageTransform.cols() == 0 || imageTransform.rows() == 0) {
            return new Image(faceImageMat);
        }
        Imgproc.warpAffine(
                faceImageMat,
                tempMat,
                imageTransform,
                new Size(STANDARD_IMAGE_WIDTH, STANDARD_IMAGE_HEIGHT));
        faceImageMat = tempMat;

        return new Image(faceImageMat);
    }

    private static Mat calcEyeMat(List<Rectangle> eyeRects) {
        return calcEyeMat(eyeRects.get(0).getCenter(), eyeRects.get(1).getCenter());
    }

    private static Mat calcEyeMat(PointF... eyeCenter) {
        Mat eyeMat = new Mat(1, 3, CvType.CV_32FC2);

        putInMat(eyeMat, 0, eyeCenter[0]);
        putInMat(eyeMat, 1, eyeCenter[1]);
        putInMat(eyeMat, 2, thirdEquilateralPoint(eyeCenter));

        return eyeMat;
    }

    //puts information into a Matrix
    private static void putInMat(Mat m, int pointIndex, PointF point) {
        m.put(0, pointIndex, point.x, point.y);
    }

    //Creates third point of an equilateral triangle pointing downwards
    private static PointF thirdEquilateralPoint(PointF[] eyeCenter) {
        float length = Math.abs(eyeCenter[0].x - eyeCenter[1].x);

        float xVal = (eyeCenter[0].x + eyeCenter[1].x) / 2;
        float yVal = (float) (eyeCenter[0].y - Math.hypot(length, length / 2));

        return new PointF(xVal, yVal);
    }

    public native void close();


    private class IdentifyResult {
        int status;
        long id;

        IdentifyResult() {}

        void set(int status, String idStr) {
            this.status = status;
            this.id = idStr != null ? Long.parseLong(idStr) : 0;
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

    private class FaceModel {
        FaceModel(int initialSize) {
            this.native_initialize(initialSize);
        }

        void addToModel(Identity identity) {
            this.native_addToModel(identity.image.getMat().getNativeObjAddr(), Long.toString(identity.key));
        }

        public native void close();

        private native void native_addToModel(long imagePtr, String label);

        private native void native_initialize(int initSize);

        private long mNativePtr;
    }

    private static Mat mReferenceEyePoints;

    private double mConfidenceThreshold = 70.0;
    private Set<Long> mIdSet = new HashSet<>();
    private EyeDetector mEyeDetector;
    private long mNativePtr;
}
