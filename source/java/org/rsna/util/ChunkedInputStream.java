/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/ChunkedInputStream.java,v 1.16.2.2 2004/02/22 18:21:13 olegk Exp $
 * $Revision: 1.16.2.2 $
 * $Date: 2004/02/22 18:21:13 $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 */

package org.rsna.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Transparently coalesces chunks of a HTTP stream that uses
 * Transfer-Encoding chunked.
 * Note that this class NEVER closes the underlying stream, even when close
 * gets called.  Instead, it will read until the "end" of its chunking on close,
 * which allows for the seamless invocation of subsequent HTTP 1.1 calls, while
 * not requiring the client to remember to read the entire contents of the
 * response.
 *
 * @author Ortwin Gluck
 * @author Sean C. Sullivan
 * @author Martin Elwin
 * @author Eric Johnson
 * @author Mike Bowler
 * @author Michael Becke
 * @author Oleg Kalnichevski
 *
 */

public class ChunkedInputStream extends InputStream {
    /** The inputstream that we're wrapping */
    private InputStream in;

    /** The chunk size */
    private int chunkSize;

    /** The current position within the current chunk */
    private int pos;

    /** True if we are at the beginning of stream */
    private boolean bof = true;

    /** True if we've reached the end of stream */
    private boolean eof = false;

    /** True if this stream is closed */
    private boolean closed = false;

    /**
     * Create a ChunkedInputStream on an InputStream.
     * @param in must be non-null
     * @throws IOException if an IO error occurs
     */
    public ChunkedInputStream(final InputStream in) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("InputStream parameter may not be null");
		}
		this.in = in;
		this.pos = 0;
    }

    /**
     * Returns all the data in a chunked stream in coalesced form. A chunk
     * is followed by a CRLF. The method returns -1 as soon as a chunksize of 0 is detected.
     * @return -1 of the end of the stream has been reached or the next data byte
     * @throws IOException if an IO problem occurs
     */
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        if (eof) {
            return -1;
        } 
        if (pos >= chunkSize) {
            nextChunk();
            if (eof) { 
                return -1;
            }
        }
        pos++;
        return in.read();
    }

    /**
     * Read some bytes from the stream.
     * @param b The byte array that will hold the contents from the stream.
     * @param off The offset into the byte array at which bytes will start to be
     * placed.
     * @param len the maximum number of bytes that can be returned.
     * @return The number of bytes returned or -1 if the end of stream has been
     * reached.
     * @see java.io.InputStream#read(byte[], int, int)
     * @throws IOException if an IO problem occurs.
     */
    public int read (byte[] b, int off, int len) throws IOException {

        if (closed) {
            throw new IOException("Attempted read from closed stream.");
        }

        if (eof) { 
            return -1;
        }
        if (pos >= chunkSize) {
            nextChunk();
            if (eof) { 
                return -1;
            }
        }
        len = Math.min(len, chunkSize - pos);
        int count = in.read(b, off, len);
        pos += count;
        return count;
    }

    /**
     * Read some bytes from the stream.
     * @param b The byte array that will hold the contents from the stream.
     * @return The number of bytes returned or -1 if the end of stream has been
     * reached.
     * @see java.io.InputStream#read(byte[])
     * @throws IOException if an IO problem occurs.
     */
    public int read (byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Read the CRLF terminator.
     * @throws IOException If an IO error occurs.
     */
    private void readCRLF() throws IOException {
        int cr = in.read();
        int lf = in.read();
        if ((cr != '\r') || (lf != '\n')) { 
            throw new IOException(
                "CRLF expected at end of chunk: " + cr + "/" + lf);
        }
    }


    /**
     * Read the next chunk.
     * @throws IOException If an IO error occurs.
     */
    private void nextChunk() throws IOException {
        if (!bof) {
            readCRLF();
        }
        chunkSize = getChunkSizeFromInputStream(in);
        bof = false;
        pos = 0;
        if (chunkSize == 0) {
            eof = true;
        }
    }

    /**
     * Expects the stream to start with a chunksize in hex with optional
     * comments after a semicolon. The line must end with a CRLF: "a3; some
     * comment\r\n" Positions the stream at the start of the next line.
     *
     * @param in The new input stream.
     * 
     * @return the chunk size as integer
     * 
     * @throws IOException when the chunk size could not be parsed
     */
    private static int getChunkSizeFromInputStream(final InputStream in) 
      throws IOException {
            
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // States: 0=normal, 1=\r was scanned, 2=inside quoted string, -1=end
        int state = 0; 
        while (state != -1) {
        int b = in.read();
            if (b == -1) { 
                throw new IOException("chunked stream ended unexpectedly");
            }
            switch (state) {
                case 0: 
                    switch (b) {
                        case '\r':
                            state = 1;
                            break;
                        case '\"':
                            state = 2;
                            /* fall through */
                        default:
                            baos.write(b);
                    }
                    break;

                case 1:
                    if (b == '\n') {
                        state = -1;
                    } else {
                        // this was not CRLF
                        throw new IOException("Protocol violation: Unexpected"
                            + " single newline character in chunk size");
                    }
                    break;

                case 2:
                    switch (b) {
                        case '\\':
                            b = in.read();
                            baos.write(b);
                            break;
                        case '\"':
                            state = 0;
                            /* fall through */
                        default:
                            baos.write(b);
                    }
                    break;
                default: throw new RuntimeException("assertion failed");
            }
        }

        //parse data
        String dataString = new String(baos.toByteArray());
        int separator = dataString.indexOf(';');
        dataString = (separator > 0)
            ? dataString.substring(0, separator).trim()
            : dataString.trim();

        int result;
        try {
            result = Integer.parseInt(dataString.trim(), 16);
        } catch (NumberFormatException e) {
            throw new IOException ("Bad chunk size: " + dataString);
        }
        return result;
    }

    /**
     * Upon close, this reads the remainder of the chunked message,
     * leaving the underlying socket at a position to start reading the
     * next response without scanning.
     * @throws IOException If an IO problem occurs.
     */
    public void close() throws IOException {
        if (!closed) {
            try {
                if (!eof) {
                    exhaustInputStream(this);
                }
            } finally {
                eof = true;
                closed = true;
            }
        }
    }

    /**
     * Exhaust an input stream, reading until EOF has been encountered.
     *
     * Note that this function is intended as a non-public utility.
     * This is a little weird, but it seemed silly to make a utility
     * class for this one function, so instead it is just static and
     * shared that way.
     *
     * @param inStream The {@link InputStream} to exhaust.
     * @throws IOException If an IO problem occurs
     */
    static void exhaustInputStream(InputStream inStream) throws IOException {
        // read and discard the remainder of the message
        byte buffer[] = new byte[1024];
        while (inStream.read(buffer) >= 0) {
            ;
        }
    }
}