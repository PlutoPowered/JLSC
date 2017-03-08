/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 socraticphoenix@gmail.com
 * Copyright (c) 2016 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmail.socraticphoenix.jlsc.header;

import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.pio.ByteStream;
import com.gmail.socraticphoenix.pio.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JLSCKeyValueHeader {
    private String key;
    private List<String> comments;
    private JLSCValueHeader valueHeader;

    public JLSCKeyValueHeader(JLSCKeyValue src) {
        this.key = src.getKey();
        this.comments = src.getComments();
        this.valueHeader = new JLSCValueHeader(src.getValue());
    }

    public JLSCKeyValueHeader(String key, List<String> comments, JLSCValueHeader valueHeader) {
        this.key = key;
        this.comments = comments;
        this.valueHeader = valueHeader;
    }

    public static JLSCKeyValueHeader read(ByteStream buffer) throws JLSCException {
        String key = null;
        try {
            key = Bytes.readString(buffer);
        } catch (IOException | UnsupportedOperationException e) {
            throw new JLSCException("Unable to read key (halted at: " + buffer.position() + ")", e);
        }
        int commentsNum = 0;
        try {
            commentsNum = buffer.getInt();
        } catch (IOException e) {
            throw new JLSCException("Unable to read comment amount (halted at: " + buffer.position() + ")", e);
        }
        List<String> comments = new ArrayList<>();
        for (int i = 0; i < commentsNum; i++) {
            try {
                comments.add(Bytes.readString(buffer));
            } catch (IOException e) {
                throw new JLSCException("Unable to read comment #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
        }
        return new JLSCKeyValueHeader(key, comments, JLSCValueHeader.read(buffer));
    }

    public int length() {
        int len = Bytes.length(this.key) + Integer.BYTES;
        for (String s : this.comments) {
            len += Bytes.length(s);
        }
        len += this.valueHeader.length();
        return len;
    }

    public void write(ByteStream buffer) throws JLSCException {
        try {
            Bytes.writeString(buffer, this.key);
        } catch (IOException e) {
            throw new JLSCException("Unable to write key (halted at: " + buffer.position() + ")", e);
        }
        try {
            buffer.putInt(comments.size());
        } catch (IOException e) {
            throw new JLSCException("Unable to write comment amount (halted at: " + buffer.position() + ")", e);
        }
        for (int i = 0; i < this.comments.size(); i++) {
            String c = this.comments.get(i);
            try {
                Bytes.writeString(buffer, c);
            } catch (IOException e) {
                throw new JLSCException("Unable to write comment #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
        }
        this.valueHeader.write(buffer);
    }

    public String getKey() {
        return this.key;
    }

    public List<String> getComments() {
        return this.comments;
    }

    public JLSCValueHeader getValueHeader() {
        return this.valueHeader;
    }

}
