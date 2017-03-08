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

import com.gmail.socraticphoenix.inversey.many.DangerousConsumer2;
import com.gmail.socraticphoenix.inversey.many.DangerousFunction1;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.io.JLSCStyle;
import com.gmail.socraticphoenix.jlsc.io.JLSCSyntax;
import com.gmail.socraticphoenix.jlsc.value.JLSCDualProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.parse.Strings;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SimpleDualProcessor implements JLSCDualProcessor {
    private Predicate<String> canRead;
    private DangerousFunction1<String, Optional<JLSCValue>, Exception> read;
    private Predicate<Class> canWrite;
    public DangerousFunction1<JLSCValue, Optional<String>, Exception> write;

    private DangerousFunction1<ByteStream, Optional<JLSCValue>, Exception> readBytes;
    private Function<JLSCValue, Integer> size;
    private DangerousConsumer2<JLSCValue, ByteStream, Exception> writeBytes;

    private String id;

    public SimpleDualProcessor(Predicate<String> canRead, DangerousFunction1<String, Optional<JLSCValue>, Exception> read, Predicate<Class> canWrite, DangerousFunction1<JLSCValue, Optional<String>, Exception> write, DangerousFunction1<ByteStream, Optional<JLSCValue>, Exception> readBytes, Function<JLSCValue, Integer> size, DangerousConsumer2<JLSCValue, ByteStream, Exception> writeBytes, String id) {
        this.canRead = canRead;
        this.read = read;
        this.canWrite = canWrite;
        this.write = write;
        this.readBytes = readBytes;
        this.size = size;
        this.writeBytes = writeBytes;
        this.id = id;
    }

    @Override
    public boolean canRead(String s, JLSCSyntax syntax, JLSCStyle style) {
        return this.canRead.test(s);
    }

    @Override
    public JLSCValue read(String s, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        try {
            return this.read.invoke(s).orElseThrow(() -> new JLSCException("Unable to read \"" + Strings.escape(s) + "\" as " + this.id()));
        } catch (Exception e) {
            throw new JLSCException("Unable to read \"" + Strings.escape(s) + "\" as " + this.id(), e);
        }
    }

    @Override
    public boolean canWrite(JLSCValue value) {
        return this.canWrite(value.type());
    }

    @Override
    public boolean canWrite(Class type) {
        return this.canWrite.test(type);
    }

    @Override
    public String write(JLSCValue value, int indent, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        try {
            return this.write.invoke(value).orElseThrow(() -> new JLSCException("Unable to write value of type \"" + value.type().getName() + "\" as " + this.id()));
        } catch (Exception e) {
            throw new JLSCException("Unable to write value of type \"" + value.type().getName() + "\" as " + this.id(), e);
        }
    }

    @Override
    public JLSCValue readBytes(ByteStream buffer) throws JLSCException {
        try {
            return this.readBytes.invoke(buffer).orElseThrow(() -> new JLSCException("Unable to read bytes (halted at " + buffer.position() + ") as " + this.id()));
        } catch (Exception e) {
            throw new JLSCException("Unable to read bytes (halted at " + buffer.position() + ") as " + this.id(), e);
        }
    }

    @Override
    public boolean canWriteBytes(JLSCValue value) {
        return this.canWrite(value.type());
    }

    @Override
    public boolean canWriteBytes(Class type) {
        return this.canWrite(type);
    }

    @Override
    public int size(JLSCValue value) {
        return this.size.apply(value);
    }

    @Override
    public void write(ByteStream buffer, JLSCValue value) {
        try {
            this.writeBytes.call(value, buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String id() {
        return this.id;
    }
}
