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
package com.gmail.socraticphoenix.jlsc.serialization;

import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCSkeleton;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCVerifier;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;

public abstract class JLSCCompoundSerializer<T> implements JLSCSerializer<T> {
    private Class<T> type;
    private boolean strict;
    private JLSCSkeleton skeleton;

    public JLSCCompoundSerializer(Class<T> type, boolean strict, JLSCSkeleton skeleton) {
        this.type = type;
        this.strict = strict;
        this.skeleton = skeleton;
    }

    protected abstract void write(JLSCValue val, JLSCCompound trg) throws JLSCException;

    protected abstract JLSCValue read(JLSCCompound compound) throws JLSCException;

    public JLSCCompoundSerializer(Class<T> type, JLSCSkeleton skeleton) {
        this(type, false, skeleton);
    }

    @Override
    public Class<T> result() {
        return this.type;
    }

    @Override
    public JLSCVerifier verifier() {
        return this.skeleton;
    }

    @Override
    public boolean canSerialize(Object object) {
        return object != null && (this.strict ? this.type.equals(object.getClass()) : this.type.isInstance(object));
    }

    @Override
    public JLSCValue serialize(JLSCValue value) throws JLSCException {
        JLSCCompound compound = new JLSCCompound();
        this.write(value, compound);
        JLSCValue write = JLSCValue.of(compound);
        write.absorbMetadata(value);
        return write;
    }

    @Override
    public JLSCValue deSerialize(JLSCValue value) throws JLSCException {
        JLSCCompound compound = value.getAsCompound().get();
        JLSCValue read = this.read(compound);
        read.absorbMetadata(value);
        return read;
    }
}
