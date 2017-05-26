package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.List;

import edu.ucsc.cmps115_spring2017.face2name.Identity.Identity;
import edu.ucsc.cmps115_spring2017.face2name.R;

/**
 * Created by micah on 5/23/17.
 */

public final class FaceRecognition {
    public FaceRecognition(Context context, List<Identity> identities) {
        try {
            mEyeClassifier = new ImageClassifier(context, R.raw.haarcascade_eye_tree_eyeglasses);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // init(identities);
    }

    private native void init(List<Identity> identities);

    public native void close();

    Identity identify(Bitmap image, RectF Face) {
        Mat imageMat = new Mat();
        Utils.bitmapToMat(image, imageMat);

        // ...

        return null;
    }

    private native long nativeIdentify(long matPtr);

    ImageClassifier mEyeClassifier;
    long mNativePtr;
}
