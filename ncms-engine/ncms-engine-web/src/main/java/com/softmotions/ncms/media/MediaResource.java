package com.softmotions.ncms.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface MediaResource {

    /**
     * Get the resource name.
     */
    String getName();

    /**
     * Get the the resource encoding.
     */
    String getEncoding();

    /**
     * Get resource content type.
     * Return <code>null</code> if content type is not known
     */
    String getContentType();

    /**
     * Get the the template last modified time.
     * Return <code>-1L</code> if last modified time is not known.
     */
    long getLastModified();

    /**
     * Get resource length.
     * Returns <code>-1L</code> if length is not known.
     */
    long getLength();

    /**
     * Get the template source.
     *
     * @return source
     * @throws java.io.IOException - If an I/O error occurs
     */
    String getSource() throws IOException;

    /**
     * Get the template source reader.
     * <p/>
     * NOTE: Don't forget close the reader.
     * <p/>
     * <pre>
     * Reader reader = resource.openReader();
     * try {
     * 	 // do something ...
     * } finally {
     * 	 reader.close();
     * }
     * </pre>
     *
     * @return source reader
     * @throws IOException - If an I/O error occurs
     */
    Reader openReader() throws IOException;

    /**
     * Get the template source input stream.
     * <p/>
     * NOTE: Don't forget close the input stream.
     * <p/>
     * <pre>
     * InputStream stream = resource.openStream();
     * try {
     * 	 // do something ...
     * } finally {
     * 	 stream.close();
     * }
     * </pre>
     *
     * @return source input stream
     * @throws IOException - If an I/O error occurs
     */
    InputStream openStream() throws IOException;
}
