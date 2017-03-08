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

import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JLSCArrayHeader {
    private List<JLSCValueHeader> valueHeaders;
    
    public JLSCArrayHeader(JLSCArray src) {
        this.valueHeaders = src.leaves(false).stream().map(JLSCValueHeader::new).collect(Collectors.toList());
    }
 
    public JLSCArrayHeader() {
        this.valueHeaders = new ArrayList<>();
    }
    
    public static JLSCArrayHeader read(ByteStream buffer) throws JLSCException {
        int numHeaders = 0;
        try {
            numHeaders = buffer.getInt();
        } catch (IOException e) {
            throw new JLSCException("Unable to read header amount (halted at: " + buffer.position() + ")", e);
        }
        JLSCArrayHeader header = new JLSCArrayHeader();
        for (int i = 0; i < numHeaders; i++) {
            header.getValueHeaders().add(JLSCValueHeader.read(buffer));
        }
        return header;
    }
    
    public int length() {
        int len = Integer.BYTES;
        for(JLSCValueHeader header : this.valueHeaders) {
            len += header.length();
        }
        return len;
    }
    
    public void write(ByteStream buffer) throws JLSCException {
        try {
            buffer.putInt(valueHeaders.size());
        } catch (IOException e) {
            throw new JLSCException("Unable to write header amount (halted at: " + buffer.position() + ")", e);
        }
        for (int i = 0; i < this.valueHeaders.size(); i++) {
            this.valueHeaders.get(i).write(buffer);
        }
    }

    public List<JLSCValueHeader> getValueHeaders() {
        return this.valueHeaders;
    }
    
}
