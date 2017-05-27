package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ucsc.cmps115_spring2017.face2name.Identity.Identity;
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
            Mat matImage = new Mat();
            Utils.bitmapToMat(ident.image, matImage);

            addToModel(matImage, ident.key);
        }
    }

    private native void native_initialize();

    public Identity addFace(Bitmap face, Long id) {
        Mat normalizedFace = normalizeFace(face);

        if (normalizedFace == null) return null;

        addToModel(normalizedFace, id);

        Bitmap normalizedFaceBitmap = Bitmap.createBitmap(normalizedFace.cols(), normalizedFace.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(normalizedFace, normalizedFaceBitmap);

        return new Identity(id, null, normalizedFaceBitmap);
    }

    private void addToModel(Mat face, Long id) {
        if (mIdSet.contains(id)) {
            throw new RuntimeException("ID already exists within model.");
        }
        mIdSet.add(id);

        native_addToModel(face.getNativeObjAddr(), id);
    }

    private native void native_addToModel(long matPtr, long id);

    public RecognitionResult identify(Bitmap faceImage) {
        Mat normalizedFace = normalizeFace(faceImage);

        if (normalizedFace == null) {
            return new RecognitionResult(RECOG_FAILED);
        }
        IdentifyResult identResult = native_identify(normalizedFace.getNativeObjAddr());

        if ((identResult.status & RECOG_SUCCESS) == 0) {
            return new RecognitionResult(RECOG_FAILED);
        } else if ((identResult.status & FACE_FOUND) == 0) {
            return new RecognitionResult(RECOG_SUCCESS);
        }
        Bitmap normalizedFaceBitmap = Bitmap.createBitmap(normalizedFace.cols(), normalizedFace.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(normalizedFace, normalizedFaceBitmap);

        Identity ident = new Identity(identResult.id, null, normalizedFaceBitmap);

        return new RecognitionResult(RECOG_SUCCESS | FACE_FOUND, ident);
    }

    private native IdentifyResult native_identify(long faceImagePtr);

    private Mat normalizeFace(Bitmap bitmapImage) {
        Mat matImage = new Mat();
        Utils.bitmapToMat(bitmapImage, matImage);

        return normalizeFace(matImage);
    }

    private Mat normalizeFace(Mat matImage) {
        return matImage;
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
}
