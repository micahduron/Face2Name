package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.util.Log;

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

        for (final Identity ident : identities) {
            addToModel(ident.image, ident.key);
        }
    }

    private native void native_initialize();

    public Identity addFace(Image face) {
        long faceId = UUID.randomUUID().getLeastSignificantBits();

        return addFace(face, faceId);
    }

    public Identity addFace(Image face, long id) {
        Image normalizedFace = normalizeFace(face);

        if (normalizedFace == null) return null;

        addToModel(normalizedFace, id);

        return new Identity(id, null, normalizedFace);
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
        Image normalizedFace = normalizeFace(faceImage);

        //Log.d("Face", "Face identified");

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

    private Image normalizeFace(Image image) {
        return image;
    }

    public native void close();


    private class IdentifyResult {
        int status;
        long id;

        IdentifyResult(int status) {
            this.status = status;
        }

        IdentifyResult(int status, String id) {
            this.status = status;
            this.id = Long.parseLong(id);
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
