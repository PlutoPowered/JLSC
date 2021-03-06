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

import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.mirror.Reflections;

import java.util.NoSuchElementException;

public class JLSCEnumProcessor extends JLSCNamedProcessor<Enum> {

    public JLSCEnumProcessor() {
        super(Enum.class, "enum", false);
    }

    @Override
    protected void write(Enum value, JLSCArray trg) throws JLSCException {
        trg.add(value.getDeclaringClass().getName());
        trg.add(value.name());
    }

    @Override
    protected Enum read(JLSCArray src) throws JLSCException {
        try {
            return Enum.valueOf(Reflections.forName(src.get(0).get().getAsString().get()), src.get(1).get().getAsString().get());
        } catch (ClassNotFoundException | ArrayIndexOutOfBoundsException | NoSuchElementException e) {
            throw new JLSCException("Expected enum(class, name), but got something else", e);
        }
    }
}
