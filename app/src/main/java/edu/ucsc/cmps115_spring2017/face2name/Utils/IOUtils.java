package edu.ucsc.cmps115_spring2017.face2name.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by micah on 5/23/17.
 */

public class IOUtils {
    public static void copyStream(InputStream inStream, OutputStream outStream)
        throws IOException
    {
        copyStream(inStream, outStream, 256);
    }

    public static void copyStream(InputStream inStream, OutputStream outStream, int bufferSize)
        throws IOException
    {
        byte[] streamBuffer = new byte[bufferSize];
        int bytesRead;

        while ((bytesRead = inStream.read(streamBuffer)) != -1) {
            outStream.write(streamBuffer, 0, bytesRead);
        }
    }
}
