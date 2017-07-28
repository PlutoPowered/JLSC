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

import com.gmail.socraticphoenix.collect.Items;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.pio.ByteStream;
import com.gmail.socraticphoenix.pio.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JLSCValueHeader {
    private JLSCValueProperty typeSpecifier;
    private List<JLSCValueProperty> properties;

    public JLSCValueHeader(JLSCValue src) {
        src = src.getForWriting();
        this.typeSpecifier = src.getTypeSpecifier();
        this.properties = Items.looseClone(src.getProperties());
    }

    public JLSCValueHeader(JLSCValueProperty typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
        this.properties = new ArrayList<>();
    }

    public static JLSCValueHeader read(ByteStream buffer) throws JLSCException {
        String typeName = null;
        try {
            typeName = Bytes.readString(buffer);
        } catch (IOException e) {
            throw new JLSCException("Unable to read type specifier (halted at: " + buffer.position() + ")", e);
        }
        JLSCValueProperty typeSpecifier = new JLSCValueProperty(typeName);
        int typeArgNum = 0;
        try {
            typeArgNum = buffer.getInt();
        } catch (IOException e) {
            throw new JLSCException("Unable to read type specifier argument amount (halted at: " + buffer.position() + ")", e);
        }
        for (int i = 0; i < typeArgNum; i++) {
            try {
                typeSpecifier.getArguments().add(Bytes.readString(buffer));
            } catch (IOException e) {
                throw new JLSCException("Unable to read type specifier argument #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
        }

        JLSCValueHeader header = new JLSCValueHeader(typeSpecifier);
        int propertyNum = 0;
        try {
            propertyNum = buffer.getInt();
        } catch (IOException e) {
            throw new JLSCException("Unable to read property amount (halted at: " + buffer.position() + ")", e);
        }
        for (int i = 0; i < propertyNum; i++) {
            String name = null;
            try {
                name = Bytes.readString(buffer);
            } catch (IOException e) {
                throw new JLSCException("Unable to read name of property #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
            JLSCValueProperty property = new JLSCValueProperty(name);
            int argNum = 0;
            try {
                argNum = buffer.getInt();
            } catch (IOException e) {
                throw new JLSCException("Unable to read argument amount of property #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
            for (int j = 0; j < argNum; j++) {
                try {
                    property.getArguments().add(Bytes.readString(buffer));
                } catch (IOException e) {
                    throw new JLSCException("Unable to read argument #" + (j + 1) + " for property #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
                }
            }
            header.getProperties().add(property);
        }

        return header;
    }

    public int length() {
        int len = 0;
        len += Bytes.length(this.typeSpecifier.getName()) + Integer.BYTES;
        for(String s : this.typeSpecifier.getArguments()) {
            len += Bytes.length(s);
        }

        len += Integer.BYTES;
        for (JLSCValueProperty property : this.properties) {
            len += Bytes.length(property.getName()) + Integer.BYTES;
            for(String s : property.getArguments()) {
                len += Bytes.length(s);
            }
        }
        return len;
    }

    public void write(ByteStream buffer) throws JLSCException {
        try {
            Bytes.writeString(buffer, this.typeSpecifier.getName());
        } catch (IOException e) {
            throw new JLSCException("Unable to write type specifier (halted at: " + buffer.position() + ")", e);
        }
        try {
            buffer.putInt(this.typeSpecifier.getArguments().size());
        } catch (IOException e) {
            throw new JLSCException("Unable to write type specifier argument amount (halted at: " + buffer.position() + ")", e);
        }
        for (int i = 0; i < this.typeSpecifier.getArguments().size(); i++) {
            try {
                Bytes.writeString(buffer, this.typeSpecifier.getArguments().get(i));
            } catch (IOException e) {
                throw new JLSCException("Unable to write type specifier argument #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
        }
        try {
            buffer.putInt(this.properties.size());
        } catch (IOException e) {
            throw new JLSCException("Unable to write property amount (halted at: " + buffer.position() + ")", e);
        }

        for (int i = 0; i < this.properties.size(); i++) {
            JLSCValueProperty property = this.properties.get(i);
            try {
                Bytes.writeString(buffer, property.getName());
            } catch (IOException e) {
                throw new JLSCException("Unable to write property #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
            try {
                buffer.putInt(property.getArguments().size());
            } catch (IOException e) {
                throw new JLSCException("Unable to write argument amount for property #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
            }
            for (int j = 0; j < property.getArguments().size(); j++) {
                try {
                    Bytes.writeString(buffer, property.getArguments().get(j));
                } catch (IOException e) {
                    throw new JLSCException("Unable to write argument #" + (j + 1) + " for property #" + (i + 1) + " (halted at: " + buffer.position() + ")", e);
                }
            }
        }
    }

    public JLSCValueProperty getTypeSpecifier() {
        return this.typeSpecifier;
    }

    public List<JLSCValueProperty> getProperties() {
        return this.properties;
    }

}
