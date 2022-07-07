/*
 * Taken from https://github.com/jjenkov/TokenReplacingReader - no license specified there
 */
package org.ml.tools.token;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 *
 */
public class TokenReplacingReader extends Reader {

    protected PushbackReader pushbackReader;
    protected ITokenResolver tokenResolver;
    protected StringBuilder tokenNameBuffer = new StringBuilder();
    protected String tokenValue;
    protected int tokenValueIndex = 0;

    /**
     * @param source
     * @param resolver
     */
    public TokenReplacingReader(Reader source, ITokenResolver resolver) {
        this.pushbackReader = new PushbackReader(source, 2);
        this.tokenResolver = resolver;
    }

    /**
     * @param target
     * @return
     * @throws IOException
     */
    @Override
    public int read(CharBuffer target) {
        throw new RuntimeException("Operation Not Supported");
    }

    /**
     * @return @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (this.tokenValue != null) {
            if (this.tokenValueIndex < this.tokenValue.length()) {
                return this.tokenValue.charAt(this.tokenValueIndex++);
            }
            if (this.tokenValueIndex == this.tokenValue.length()) {
                this.tokenValue = null;
                this.tokenValueIndex = 0;
            }
        }

        int data = this.pushbackReader.read();
        if (data != '$') {
            return data;
        }

        data = this.pushbackReader.read();
        if (data != '{') {
            this.pushbackReader.unread(data);
            return '$';
        }
        this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());

        data = this.pushbackReader.read();
        while (data != '}') {
            this.tokenNameBuffer.append((char) data);
            data = this.pushbackReader.read();
        }

        this.tokenValue = this.tokenResolver.resolveToken(this.tokenNameBuffer.toString());

        if (this.tokenValue == null) {
            this.tokenValue = "${" + this.tokenNameBuffer.toString() + "}";
        }
        if (this.tokenValue.length() == 0) {
            return read();
        }
        return this.tokenValue.charAt(this.tokenValueIndex++);

    }

    /**
     * #
     *
     * @param cbuf
     * @return
     * @throws IOException
     */
    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * @param cbuf
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = 0;
        for (int i = 0; i < len; i++) {
            int nextChar = read();
            if (nextChar == -1) {
                if (charsRead == 0) {
                    charsRead = -1;
                }
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char) nextChar;
        }
        return charsRead;
    }

    /**
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.pushbackReader.close();
    }

    /**
     * @param n
     * @return
     * @throws IOException
     */
    @Override
    public long skip(long n) {
        throw new UnsupportedOperationException("Operation Not Supported");
    }

    /**
     * @return @throws IOException
     */
    @Override
    public boolean ready() throws IOException {
        return this.pushbackReader.ready();
    }

    /**
     * @return
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * @param readAheadLimit
     */
    @Override
    public void mark(int readAheadLimit) {
        throw new UnsupportedOperationException("Operation Not Supported");
    }

    /**
     *
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Operation Not Supported");
    }
}
