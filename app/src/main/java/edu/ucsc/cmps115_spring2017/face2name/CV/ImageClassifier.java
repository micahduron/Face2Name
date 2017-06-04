package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.ucsc.cmps115_spring2017.face2name.Utils.IOUtils;

/**
 * Created by micah on 5/23/17.
 */

public class ImageClassifier extends CascadeClassifier {
    /**
     * @param context App context
     * @param resourceId The resource ID of the XML file describing the Haar cascade feature.
     */
    public ImageClassifier(Context context, int resourceId) throws IOException {
        super(getHaarFeaturePath(context, resourceId));
    }

    /**
     * @param filename Path of the XML file describing the Haar cascade feature.
     */
    public ImageClassifier(String filename) {
        super(filename);
    }

    private static String getHaarFeaturePath(Context context, int resourceId) throws IOException {
        File featureFile = getHaarFeatureFile(context, resourceId);

        if (!featureFile.exists()) {
            // Disgusting. It's also the Java Way(TM).

            OutputStream outStream = null;
            InputStream inStream = null;

            try {
                outStream = new FileOutputStream(featureFile);
                inStream = context.getResources().openRawResource(resourceId);

                IOUtils.copyStream(inStream, outStream);
            } catch (IOException ex) {
                featureFile.delete();

                throw ex;
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
            }
        }
        return featureFile.getAbsolutePath();
    }

    private static File getHaarFeatureFile(Context context, int resourceId) {
        File cascadesDir = context.getDir("haarcascades", Context.MODE_PRIVATE);
        String filename = context.getResources().getResourceEntryName(resourceId) + ".xml";

        return new File(cascadesDir, filename);
    }
}
