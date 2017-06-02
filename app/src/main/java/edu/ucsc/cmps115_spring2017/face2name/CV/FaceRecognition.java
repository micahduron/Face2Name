package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


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
        long faceId = UUID.randomUUID().getLeastSignificantBits();

        return addFace(face, faceId);
    }

    public Identity addFace(Image face, long id) {
        addToModel(face, id);

        return new Identity(id, null, face);
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

    public static Image normalizeFace(Image image) {
        Image resultImage = new Image(new Mat());

        Imgproc.cvtColor(image.getMat(), resultImage.getMat(), Imgproc.COLOR_RGB2GRAY);

        return resultImage;
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

    private double mConfidenceThreshold = 40.0;
    private Set<Long> mIdSet = new HashSet<>();
    private EyeDetector mEyeDetector;
    private long mNativePtr;
}
