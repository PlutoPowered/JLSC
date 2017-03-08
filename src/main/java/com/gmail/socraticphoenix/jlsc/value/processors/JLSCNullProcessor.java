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
package com.gmail.socraticphoenix.jlsc.value.processors;

import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.io.JLSCStyle;
import com.gmail.socraticphoenix.jlsc.io.JLSCSyntax;
import com.gmail.socraticphoenix.jlsc.value.JLSCDualProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.pio.ByteStream;

public class JLSCNullProcessor implements JLSCDualProcessor {

    @Override
    public JLSCValue readBytes(ByteStream buffer) throws JLSCException {
        return JLSCValue.of(null);
    }

    @Override
    public boolean canWriteBytes(JLSCValue value) {
        return value.isNull();
    }

    @Override
    public boolean canWriteBytes(Class type) {
        return false;
    }

    @Override
    public int size(JLSCValue value) {
        return 0;
    }

    @Override
    public void write(ByteStream buffer, JLSCValue value) throws JLSCException {

    }

    @Override
    public String id() {
        return "null";
    }

    @Override
    public boolean canRead(String s, JLSCSyntax syntax, JLSCStyle style) {
        return s.equals("null");
    }

    @Override
    public JLSCValue read(String s, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        return JLSCValue.of(null);
    }

    @Override
    public boolean canWrite(JLSCValue value) {
        return value.isNull();
    }

    @Override
    public boolean canWrite(Class type) {
        return false;
    }

    @Override
    public String write(JLSCValue value, int indent, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        return "null";
    }



}
