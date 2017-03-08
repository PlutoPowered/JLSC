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

import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JLSCCompoundHeader {
    private List<JLSCKeyValueHeader> keyValueHeaders;

    public JLSCCompoundHeader(JLSCCompound src) {
        this.keyValueHeaders = src.entries().stream().map(JLSCKeyValueHeader::new).collect(Collectors.toList());
    }

    public JLSCCompoundHeader() {
        this.keyValueHeaders = new ArrayList<>();
    }

    public static JLSCCompoundHeader read(ByteStream buffer) throws JLSCException {
        int numHeaders = 0;
        try {
            numHeaders = buffer.getInt();
        } catch (IOException | UnsupportedOperationException e) {
            throw new JLSCException("Unable to read header amount", e);
        }
        JLSCCompoundHeader header = new JLSCCompoundHeader();
        for (int i = 0; i < numHeaders; i++) {
            header.getKeyValueHeaders().add(JLSCKeyValueHeader.read(buffer));
        }
        return header;
    }

    public int length() {
        int len = Integer.BYTES;
        for (JLSCKeyValueHeader header : this.keyValueHeaders) {
            len += header.length();
        }
        return len;
    }

    public void write(ByteStream buffer) throws JLSCException {
        try {
            buffer.putInt(this.keyValueHeaders.size());
        } catch (IOException | UnsupportedOperationException e) {
            throw new JLSCException("Unable to write header amount", e);
        }
        for (int i = 0; i < this.keyValueHeaders.size(); i++) {
            JLSCKeyValueHeader header = this.keyValueHeaders.get(i);
            header.write(buffer);
        }
    }

    public List<JLSCKeyValueHeader> getKeyValueHeaders() {
        return this.keyValueHeaders;
    }

}
