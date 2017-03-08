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
package com.gmail.socraticphoenix.jlsc.value.annotation;

import com.gmail.socraticphoenix.inversey.many.DangerousConsumer2;
import com.gmail.socraticphoenix.inversey.many.DangerousFunction1;
import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCNamedProcessor;

import java.util.List;

public class JLSCAnnotationProcessor<T> extends JLSCNamedProcessor<T> {
    private List<DangerousConsumer2<T, JLSCArray, JLSCException>> write;
    private DangerousFunction1<JLSCArray, T, JLSCException> read;

    public JLSCAnnotationProcessor(Class<T> type, String name, boolean strict, List<DangerousConsumer2<T, JLSCArray, JLSCException>> write, DangerousFunction1<JLSCArray, T, JLSCException> read) {
        super(type, name, strict);
        this.write = write;
        this.read = read;
    }

    @Override
    protected void write(T value, JLSCArray trg) throws JLSCException {
        for(DangerousConsumer2<T, JLSCArray, JLSCException> piece : this.write) {
            piece.call(value, trg);
        }
    }

    @Override
    protected T read(JLSCArray src) throws JLSCException {
        return this.read.invoke(src);
    }

}
