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
package com.gmail.socraticphoenix.jlsc.value;

import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCDataHolder;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.registry.JLSCRegistry;
import com.gmail.socraticphoenix.jlsc.serialization.JLSCSerializer;
import com.gmail.socraticphoenix.jlsc.value.annotation.JLSCAnnotationProcessorGenerator;
import com.gmail.socraticphoenix.mirror.CastableValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JLSCValue extends CastableValue {
    private List<JLSCValueProperty> properties;
    private JLSCValueProperty typeSpecifier;

    public JLSCValue(Object value) {
        super(value);
        this.properties = new ArrayList<>();
        Optional<JLSCProcessor> processor = JLSCRegistry.getProcessorFor(this);
        Optional<JLSCByteProcessor> byteProcessor = JLSCRegistry.getByteProcessorFor(this);
        if (processor.isPresent()) {
            this.typeSpecifier = new JLSCValueProperty(processor.get().id());
        } else if (byteProcessor.isPresent()) {
            this.typeSpecifier = new JLSCValueProperty(byteProcessor.get().id());
        } else if (value instanceof JLSCCompound) {
            this.typeSpecifier = new JLSCValueProperty("compound");
        } else if (value instanceof JLSCArray) {
            this.typeSpecifier = new JLSCValueProperty("array");
        } else {
            this.typeSpecifier = new JLSCValueProperty("deSerializedObject");
        }
    }

    public static JLSCValue of(Object value) {
        return new JLSCValue(value);
    }

    public void absorbMetadata(JLSCValue other) {
        this.properties.clear();
        this.properties.addAll(other.properties);

        if (this.getAsCompound().isPresent() && other.getAsCompound().isPresent()) {
            this.getAsCompound().get().absorbMetadata(other.getAsCompound().get());
        } else if (this.getAsArray().isPresent() && other.getAsArray().isPresent()) {
            this.getAsArray().get().absorbMetadata(other.getAsArray().get());
        }
    }


    private <T> Optional<T> convert(Class<T> type) {
        Optional<T> casted = super.getAs(type);
        if (casted.isPresent()) {
            return casted;
        }

        return this.deserialize(type);
    }

    @Override
    public <T> Optional<T> getAs(Class<T> type) {
        return this.convert(type);
    }

    @Override
    public <T> T getAs(Class<T> type, T def) {
        return this.getAs(type).orElse(def);
    }

    @Override
    public <T> T getAsOrNull(Class<T> type) {
        return this.getAs(type).orElse(null);
    }

    public JLSCValue getForWriting() {
        Optional<JLSCValue> serialized = this.serialize();
        return serialized.orElse(this);
    }

    public Optional<JLSCValue> serialize() {
        Optional<JLSCSerializer> serializerOptional = JLSCRegistry.getSerializerFor(this.value);
        if (serializerOptional.isPresent()) {
            try {
                return Optional.ofNullable(serializerOptional.get().serialize(this));
            } catch (JLSCException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public JLSCValue getObjectified() {
        Optional<JLSCSerializer> serializerOptional = JLSCRegistry.getSerializerFor(this);
        if (serializerOptional.isPresent()) {
            JLSCSerializer serializer = serializerOptional.get();
            JLSCValue result = null;
            try {
                result = serializer.deSerialize(this);
            } catch (JLSCException e) {
                return this;
            }
            result.absorbMetadata(this);
            return result;
        }

        return this;
    }

    public <T> Optional<T> deserialize(Class<T> type) {
        Optional<JLSCSerializer<T>> serializerOptional = JLSCRegistry.getSerializerFor(type, this);
        if (serializerOptional.isPresent()) {
            JLSCSerializer<T> serializer = serializerOptional.get();
            try {
                JLSCValue result = serializer.deSerialize(this);
                result.absorbMetadata(this);
                return result.getAs(type);
            } catch (JLSCException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public Optional<JLSCDataHolder> getAsDataHolder() {
        return this.getAs(JLSCDataHolder.class);
    }

    public Optional<JLSCArray> getAsArray() {
        return this.getAs(JLSCArray.class);
    }

    public Optional<JLSCCompound> getAsCompound() {
        return this.getAs(JLSCCompound.class);
    }

    public List<JLSCValueProperty> getProperties() {
        return this.properties;
    }

    public JLSCValueProperty getTypeSpecifier() {
        return this.typeSpecifier;
    }

    public void setTypeSpecifier(JLSCValueProperty typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    public <T> Optional<T> superCast(Class<T> type) {
        return super.getAs(type);
    }
}
