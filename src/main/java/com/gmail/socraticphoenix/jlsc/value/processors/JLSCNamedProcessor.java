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
import com.gmail.socraticphoenix.jlsc.io.JLSCReadWriteUtil;
import com.gmail.socraticphoenix.jlsc.io.JLSCStyle;
import com.gmail.socraticphoenix.jlsc.io.JLSCSyntax;
import com.gmail.socraticphoenix.jlsc.io.JLSCTokenizer;
import com.gmail.socraticphoenix.jlsc.value.JLSCDualProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.parse.Strings;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public abstract class JLSCNamedProcessor<T> implements JLSCDualProcessor {
    private static final JLSCStyle STYLE = new NamedProcessorStyle();

    private Class<T> type;
    private String name;
    private boolean strict;

    public JLSCNamedProcessor(Class<T> type, String name, boolean strict) {
        this.type = type;
        this.name = name;
        this.strict = strict;
    }

    public JLSCNamedProcessor(Class<T> type, boolean strict) {
        this(type, type.getName(), strict);
    }

    public JLSCNamedProcessor(Class<T> type) {
        this(type, false);
    }

    protected abstract void write(T value, JLSCArray trg) throws JLSCException;

    protected abstract T read(JLSCArray src) throws JLSCException;

    @Override
    public JLSCValue readBytes(ByteStream buffer) throws JLSCException {
        return null;
    }

    @Override
    public boolean canWriteBytes(JLSCValue value) {
        return this.canWriteBytes(value.type());
    }

    @Override
    public boolean canWriteBytes(Class type) {
        return this.strict ? type.equals(this.type) : this.type.isAssignableFrom(type);
    }

    @Override
    public int size(JLSCValue value) {
        JLSCArray array = new JLSCArray();
        try {
            this.write(value.getAs(this.type).get(), array);
            return JLSCReadWriteUtil.length(array);
        } catch (JLSCException e) {
            return 0;
        }
    }

    @Override
    public void write(ByteStream buffer, JLSCValue value) throws JLSCException {
        JLSCArray array = new JLSCArray();
        this.write(value.getAs(this.type).get(), array);
        JLSCReadWriteUtil.write(array, buffer);
    }

    @Override
    public String id() {
        return this.name;
    }

    @Override
    public boolean canRead(String s, JLSCSyntax syntax, JLSCStyle style) {
        return s.startsWith(this.name) && s.replaceFirst(Pattern.quote(this.name), "").trim().startsWith("(") && s.endsWith(")");
    }

    @Override
    public JLSCValue read(String s, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        s = Strings.cutFirst(Strings.cutLast(s.replaceFirst(Pattern.quote(this.name), "").trim()));
        JLSCTokenizer tokenizer = new JLSCTokenizer(s, JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT, LinkedHashMap::new, ArrayList::new);
        JLSCArray array = new JLSCArray();
        while (tokenizer.hasNext()) {
            array.add(tokenizer.arrayNext());
        }
        return JLSCValue.of(this.read(array));
    }

    @Override
    public boolean canWrite(JLSCValue value) {
        return this.canWrite(value.type());
    }

    @Override
    public boolean canWrite(Class type) {
        return this.strict ? type.equals(this.type) : this.type.isAssignableFrom(type);
    }

    @Override
    public String write(JLSCValue value, int indent, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        JLSCArray array = new JLSCArray();
        this.write(value.getAs(this.type).get(), array);
        return this.name + "(" + JLSCReadWriteUtil.writeArray(array, JLSCSyntax.DEFAULT, JLSCNamedProcessor.STYLE, 0) + ")";
    }


    private static class NamedProcessorStyle implements JLSCStyle {

        @Override
        public String delimiter(int indent) {
            return ": ";
        }

        @Override
        public String compoundValueDelimiter(int indent) {
            return ", ";
        }

        @Override
        public String arrayValueDelimiter(int indent) {
            return ", ";
        }

        @Override
        public String beginArray(int indent) {
            return "[";
        }

        @Override
        public String endArray(int indent) {
            return "]";
        }

        @Override
        public String beginCompound(int indent) {
            return "{";
        }

        @Override
        public String endCompound(int indent) {
            return "}";
        }

        @Override
        public String beginComment(int indent) {
            return "#";
        }

        @Override
        public String endComment(int indent) {
            return "#";
        }

        @Override
        public String beginProperty(int indent) {
            return "@";
        }

        @Override
        public String beginPropertyArgs(int indent) {
            return "(";
        }

        @Override
        public String propertyArgsDelimiter(int indent) {
            return ", ";
        }

        @Override
        public String endPropertyArgs(int indent) {
            return ")";
        }

        @Override
        public String endProperty(int indent) {
            return ":";
        }

        @Override
        public String preArrayVal(int indent) {
            return "";
        }

        @Override
        public String preKey(int indent) {
            return "";
        }

        @Override
        public boolean doLastCompoundValue() {
            return false;
        }

        @Override
        public boolean doLastArrayValue() {
            return false;
        }

    }

}
