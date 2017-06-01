package edu.ucsc.cmps115_spring2017.face2name.Utils;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by micah on 5/31/17.
 */

public class Image {
    public enum ReprType {
        REPR_BITMAP,
        REPR_MAT
    }

    public Image(Bitmap bitmap) {
        setImage(bitmap);
    }

    public Image(Mat mat) {
        setImage(mat);
    }

    public Bitmap getBitmap() {
        return isBitmap() ? mBitmap : matToBitmap(mMat);
    }

    public Image toBitmap() {
        Bitmap bitmap = getBitmap();
        setImage(bitmap);

        return this;
    }

    public Mat getMat() {
        return isMat() ? mMat : bitmapToMat(mBitmap);
    }

    /**
     * Assigns the stored image.
     *
     * @param bitmap the image to be stored
     */
    public void setImage(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        mBitmap = bitmap;
        mRepr = ReprType.REPR_BITMAP;

        mMat = null;
    }

    /**
     * Assigns the stored image.
     *
     * @param mat the image to be stored
     */
    public void setImage(Mat mat) {
        if (mat == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }
        mMat = mat;
        mRepr = ReprType.REPR_MAT;

        mBitmap = null;
    }

    /**
     * Changes the internal image representation to a Mat.
     *
     * @return The current object
     */
    public Image toMat() {
        Mat mat = getMat();
        setImage(mat);

        return this;
    }

    /**
     * Returns the current internal representation of the image.
     *
     * @return ReprType value corresponding to the current internal image representation.
     */
    public ReprType getRepr() {
        return mRepr;
    }

    /**
     * Returns whether or not the current object is storing its image as a Bitmap.
     *
     * @return true if the image is stored as a Bitmap object, false otherwise.
     */
    public boolean isBitmap() {
        return getRepr() == ReprType.REPR_BITMAP;
    }

    /**
     * Returns whether or not the current object is storing its image as a Mat.
     *
     * @return true if the image is stored as a Mat object, false otherwise.
     */
    public boolean isMat() {
        return getRepr() == ReprType.REPR_MAT;
    }

    private static Bitmap matToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        return bitmap;
    }

    private static Mat bitmapToMat(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        return mat;
    }

    private Bitmap mBitmap;
    private Mat mMat;

    private ReprType mRepr;
}
