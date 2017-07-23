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
import com.gmail.socraticphoenix.mirror.CastableValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JLSCValue extends CastableValue {
    private List<JLSCValueProperty> properties;
    private JLSCValueProperty typeSpecifier;

    private Map<Class, Object> serializedCache;
    private JLSCValue backing;

    public JLSCValue(Object value) {
        super(value);
        this.properties = new ArrayList<>();
        this.serializedCache = new HashMap<>();
        this.backing = this.makeBacking();
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
        } else if (this.backing != null) {
            this.typeSpecifier = this.backing.getTypeSpecifier();
        } else {
            throw new IllegalArgumentException("No applicable processor, byte or string, for object of type: " + value.getClass().toString());
        }
    }

    public JLSCValue(Object value, JLSCValue backing) {
        super(value);
        this.properties = new ArrayList<>();
        this.serializedCache = new HashMap<>();
        this.backing = backing;
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
        } else if (this.backing != null) {
            this.typeSpecifier = this.backing.getTypeSpecifier();
        } else {
            throw new IllegalArgumentException("No applicable processor, byte or string, for object of type: " + value.getClass().toString());
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

    private JLSCValue makeBacking() {
        if (!JLSCRegistry.getProcessorFor(this).isPresent() && !JLSCRegistry.getByteProcessorFor(this).isPresent()) {
            Optional<JLSCSerializer> serializerOptional = JLSCRegistry.getSerializerFor(this.rawValue());
            if (serializerOptional.isPresent()) {
                try {
                    return serializerOptional.get().serialize(this);
                } catch (JLSCException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public <T> Optional<T> convert(Class<T> type) {
        Optional<T> casted = this.getAs(type);
        if (casted.isPresent()) {
            return casted;
        }

        Optional<T> serialized = this.deserialize(type);
        if (serialized.isPresent()) {
            return serialized;
        }

        return Optional.empty();
    }

    public <T> T convert(Class<T> type, T def) {
        Optional<T> casted = this.getAs(type);
        if (casted.isPresent()) {
            return casted.get();
        }

        Optional<T> serialized = this.deserialize(type);
        if (serialized.isPresent()) {
            return serialized.get();
        }

        return def;
    }

    @Override
    public <T> Optional<T> getAs(Class<T> type) {
        Optional<T> val = super.getAs(type);
        return val.isPresent() || this.backing == null ? val : this.backing.getAs(type);
    }

    public <T> Optional<JLSCValue> serialize(Class<T> type) {
        Optional<T> tOptional = this.deserialize(type);
        if (tOptional.isPresent()) {
            JLSCValue value = new JLSCValue(tOptional.get());
            value.absorbMetadata(this);
            value.backing = this;
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    public <T> Optional<T> deserialize(Class<T> type) {
        Object object = this.serializedCache.get(type);
        if (object != null) {
            return Optional.of((T) object);
        } else {
            Optional<JLSCSerializer<T>> serializerOptional = JLSCRegistry.getSerializer(type);
            if (serializerOptional.isPresent()) {
                JLSCSerializer<T> serializer = serializerOptional.get();
                JLSCValue result;
                try {
                    if (serializer.verifier().isValid(this)) {
                        result = serializer.deSerialize(this);
                        result.absorbMetadata(this);
                        Optional<T> val = result.getAs(type);
                        if(val.isPresent()) {
                            this.serializedCache.put(type, val.get());
                        }
                        return val;
                    } else if (this.backing != null) {
                        return this.backing.deserialize(type);
                    }
                } catch (JLSCException e) {
                    return Optional.empty();
                }
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

    public JLSCValue getFarthestBacking() {
        if (this.backing == null) {
            return this;
        } else {
            return this.backing.getFarthestBacking();
        }
    }

    public Optional<JLSCValue> getBacking() {
        return Optional.ofNullable(this.backing);
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

}
