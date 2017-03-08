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
import com.gmail.socraticphoenix.mirror.Reflections;
import com.gmail.socraticphoenix.parse.CharacterStream;
import com.gmail.socraticphoenix.parse.ParserData;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class JLSCSimpleArrayProcessor implements JLSCDualProcessor {
    private static ParserData parserData = new ParserData().quote('"').escapeChar('\\');

    private JLSCDualProcessor processor;
    private Class component;
    private Class array;

    public JLSCSimpleArrayProcessor(JLSCDualProcessor type, Class array) {
        this.processor = type;
        this.component = array.getComponentType();
        this.array = array;
    }

    @Override
    public boolean canRead(String s, JLSCSyntax syntax, JLSCStyle style) {
        CharacterStream stream = new CharacterStream(s);
        if(!stream.isNext('(')) {
            return false;
        }
        stream.consume('(');
        while (!stream.isNext(')') && stream.hasNext()) {
            stream.consumeAll(syntax.ignore());
            String piece = stream.nextUntil(c -> c == ',' || c == ')', JLSCSimpleArrayProcessor.parserData.reset());
            stream.consume(',');
            if(!this.processor.canRead(piece, syntax, style)) {
                return false;
            }
        }
        if(!stream.isNext(')')) {
            return false;
        }
        stream.consume(')');
        return true;
    }

    @Override
    public JLSCValue read(String s, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        List<Object> pieces = new ArrayList<>();
        CharacterStream stream = new CharacterStream(s);
        stream.consume('(');
        while (!stream.isNext(')') && stream.hasNext()) {
            stream.consume(syntax.ignore());
            pieces.add(this.processor.read(stream.nextUntil(c -> c == ',' || c == ')', JLSCSimpleArrayProcessor.parserData.reset()), syntax, style).rawValue());
            stream.consume(',');
        }
        stream.consume(')');
        return JLSCValue.of(Reflections.deepCast(this.array, pieces.toArray()));
    }

    @Override
    public boolean canWrite(JLSCValue value) {
        return this.canWrite(value.type());
    }

    @Override
    public boolean canWrite(Class type) {
        return type.isArray() && (this.processor.canWrite(type.getComponentType()) || this.processor.canWrite(Reflections.boxingType(type.getComponentType())));
    }

    @Override
    public String write(JLSCValue value, int indent, JLSCSyntax syntax, JLSCStyle style) throws JLSCException {
        StringBuilder builder = new StringBuilder().append("(");
        Object array = value.rawValue();
        int len = Array.getLength(array);
        for (int l = 0; l < len; l++) {
            builder.append(this.processor.write(JLSCValue.of(Array.get(array, l)), indent, syntax, style));
            if(l < len - 1) {
                builder.append(", ");
            }
        }
        return builder.append(")").toString();
    }

    @Override
    public JLSCValue readBytes(ByteStream buffer) throws JLSCException {
        int amount = 0;
        try {
            amount = buffer.getInt();
        } catch (IOException e) {
            throw new JLSCException("Unable to read array length (halted at " + buffer.position() + ")");
        }
        Object array = Array.newInstance(this.component, amount);
        for (int i = 0; i < amount; i++) {
            Array.set(array, i, this.processor.readBytes(buffer).rawValue());
        }
        return JLSCValue.of(array);
    }

    @Override
    public boolean canWriteBytes(JLSCValue value) {
        return this.canWrite(value.type());
    }

    @Override
    public boolean canWriteBytes(Class type) {
        return type.isArray() && this.processor.canWrite(type.getComponentType());
    }

    @Override
    public int size(JLSCValue value) {
        int len = Integer.BYTES;
        for(Object piece : value.getAs(Object[].class, new Object[0])) {
            len += this.processor.size(JLSCValue.of(piece));
        }
        return len;
    }

    @Override
    public void write(ByteStream buffer, JLSCValue value) throws JLSCException {
        Object array = value.rawValue();
        int len = Array.getLength(array);
        try {
            buffer.putInt(len);
        } catch (IOException e) {
            throw new JLSCException("Unable to write array length (halted at " + buffer.position() + ")");
        }

        for (int i = 0; i < len; i++) {
            this.processor.write(buffer, JLSCValue.of(Array.get(array, i)));
        }
    }

    @Override
    public String id() {
        return this.processor.id() + "arr";
    }
}
