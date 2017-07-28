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
package com.gmail.socraticphoenix.jlsc.registry;

import com.gmail.socraticphoenix.jlsc.io.JLSCStyle;
import com.gmail.socraticphoenix.jlsc.io.JLSCSyntax;
import com.gmail.socraticphoenix.jlsc.serialization.JLSCSerializer;
import com.gmail.socraticphoenix.jlsc.serialization.annotation.JLSCAnnotationSerializePreSerialize;
import com.gmail.socraticphoenix.jlsc.serialization.annotation.JLSCAnnotationSerializerGenerator;
import com.gmail.socraticphoenix.jlsc.serialization.annotation.Serializable;
import com.gmail.socraticphoenix.jlsc.value.JLSCByteProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCDualProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.jlsc.value.annotation.Convertible;
import com.gmail.socraticphoenix.jlsc.value.annotation.JLSCAnnotationProcessorGenerator;
import com.gmail.socraticphoenix.jlsc.value.annotation.JLSCAnnotationProcessorPreID;
import com.gmail.socraticphoenix.jlsc.value.annotation.JLSCAnnotationProcessorPreParse;
import com.gmail.socraticphoenix.jlsc.value.annotation.JLSCAnnotationProcessorPreProcess;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCEnumProcessor;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCFileProcecssor;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCNullProcessor;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCPrimitiveProcessors;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCSimpleArrayProcessor;
import com.gmail.socraticphoenix.jlsc.value.processors.JLSCUUIDProcessor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JLSCRegistry {
    public static final JLSCProcessor DEFAULT_PROCESSOR = JLSCPrimitiveProcessors.STRING;

    private static Map<Class, JLSCSerializer> serializers;
    private static Map<String, JLSCProcessor> processors;
    private static Map<String, JLSCByteProcessor> byteProcessors;
    private static Set<JLSCPreProcess> preProcesses;
    private static Set<JLSCPreSerialize> preSerializes;
    private static Set<JLSCPreParse> preParses;
    private static Set<JLSCPreID> preIDs;

    static {
        JLSCRegistry.processors = Collections.synchronizedMap(new LinkedHashMap<>());
        JLSCRegistry.byteProcessors = Collections.synchronizedMap(new LinkedHashMap<>());
        JLSCRegistry.serializers = Collections.synchronizedMap(new LinkedHashMap<>());
        JLSCRegistry.preProcesses = new HashSet<>();
        JLSCRegistry.preSerializes = new HashSet<>();
        JLSCRegistry.preParses = new HashSet<>();
        JLSCRegistry.preIDs = new HashSet<>();

        JLSCRegistry.initDefaults();
    }

    private static void initDefaults() {
        register(new JLSCAnnotationSerializePreSerialize());
        register(new JLSCAnnotationProcessorPreProcess());
        register(new JLSCAnnotationProcessorPreID());
        register(new JLSCAnnotationProcessorPreParse());

        register(new JLSCNullProcessor());

        register(JLSCPrimitiveProcessors.BIG_INT);
        register(JLSCPrimitiveProcessors.BIG_DECIMAL);

        register(JLSCPrimitiveProcessors.BYTE);
        register(JLSCPrimitiveProcessors.CHARACTER);
        register(JLSCPrimitiveProcessors.SHORT);
        register(JLSCPrimitiveProcessors.INTEGER);
        register(JLSCPrimitiveProcessors.LONG);
        register(JLSCPrimitiveProcessors.FLOAT);
        register(JLSCPrimitiveProcessors.DOUBLE);
        register(JLSCPrimitiveProcessors.BOOLEAN);
        register(JLSCPrimitiveProcessors.STRING);

        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.BIG_INT, BigInteger[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.BIG_DECIMAL, BigDecimal[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.BYTE, Byte[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.CHARACTER, Character[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.SHORT, Short[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.INTEGER, Integer[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.LONG, Long[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.FLOAT, Float[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.DOUBLE, Double[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.BOOLEAN, Boolean[].class));
        register(new JLSCSimpleArrayProcessor(JLSCPrimitiveProcessors.STRING, String[].class));

        register(new JLSCUUIDProcessor());
        register(new JLSCEnumProcessor());
        register(new JLSCFileProcecssor());

        JLSCRegistry.validate();
    }

    public static void registerAnnotated(Class clazz) {
        if(clazz.getAnnotation(Serializable.class) != null) {
            register(JLSCAnnotationSerializerGenerator.generate(clazz));
        }

        if(clazz.getAnnotation(Convertible.class) != null) {
            register(JLSCAnnotationProcessorGenerator.generate(clazz));
        }
    }

    public static void registerAnnotated(Class clazz, boolean strict) {
        if(clazz.getAnnotation(Serializable.class) != null) {
            register(JLSCAnnotationSerializerGenerator.generate(clazz, strict));
        }

        if(clazz.getAnnotation(Convertible.class) != null) {
            register(JLSCAnnotationProcessorGenerator.generate(clazz, strict, clazz.getName()));
        }
    }

    public static void registerAnnotated(Class clazz, boolean strict, String name) {
        if(clazz.getAnnotation(Serializable.class) != null) {
            register(JLSCAnnotationSerializerGenerator.generate(clazz, strict));
        }

        if(clazz.getAnnotation(Convertible.class) != null) {
            register(JLSCAnnotationProcessorGenerator.generate(clazz, strict, name));
        }
    }

    public static boolean containsSerializer(Class clazz) {
        return JLSCRegistry.serializers.entrySet().stream().anyMatch(e -> e.getKey().isAssignableFrom(clazz));
    }

    public static boolean containsProcessor(String id) {
        return JLSCRegistry.processors.containsKey(id);
    }

    public static boolean containsByteProcessor(String id) {
        return JLSCRegistry.byteProcessors.containsKey(id);
    }

    public static boolean containsProcessorFor(Class clazz) {
        return JLSCRegistry.processors.values().stream().anyMatch(p -> p.canWrite(clazz));
    }

    public static boolean containsByteProcessorFor(Class clazz) {
        return JLSCRegistry.byteProcessors.values().stream().anyMatch(p -> p.canWriteBytes(clazz));
    }

    public static void validate() throws IllegalStateException {
        for (String key : JLSCRegistry.processors.keySet()) {
            if (!JLSCRegistry.byteProcessors.containsKey(key)) {
                throw new IllegalStateException("Processor map contains \"" + key + "\" but no equivalent Byte Processor exists");
            }
        }

        for (String key : JLSCRegistry.byteProcessors.keySet()) {
            if (!JLSCRegistry.processors.containsKey(key)) {
                throw new IllegalStateException("Byte Processor map contains \"" + key + "\" but no equivalent Processor exists");
            }
        }
    }

    public static boolean register(JLSCPreProcess preProcess) {
        if (!JLSCRegistry.preProcesses.contains(preProcess)) {
            JLSCRegistry.preProcesses.add(preProcess);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCPreParse preParse) {
        if (!JLSCRegistry.preParses.contains(preParse)) {
            JLSCRegistry.preParses.add(preParse);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCPreID preID) {
        if (!JLSCRegistry.preIDs.contains(preID)) {
            JLSCRegistry.preIDs.add(preID);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCPreSerialize preSerialize) {
        if (!JLSCRegistry.preSerializes.contains(preSerialize)) {
            JLSCRegistry.preSerializes.add(preSerialize);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCProcessor processor) {
        if (!JLSCRegistry.processors.containsKey(processor.id())) {
            JLSCRegistry.processors.put(processor.id(), processor);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCByteProcessor byteProcessor) {
        if (!JLSCRegistry.byteProcessors.containsKey(byteProcessor.id())) {
            JLSCRegistry.byteProcessors.put(byteProcessor.id(), byteProcessor);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCSerializer serializer) {
        if (!JLSCRegistry.serializers.containsKey(serializer.result())) {
            JLSCRegistry.serializers.put(serializer.result(), serializer);
            return true;
        }
        return false;
    }

    public static boolean register(JLSCDualProcessor dualProcessor) {
        return JLSCRegistry.register((JLSCProcessor) dualProcessor) && JLSCRegistry.register((JLSCByteProcessor) dualProcessor);
    }

    public static Optional<JLSCProcessor> getProcessor(String id) {
        Optional<JLSCProcessor> processorOptional = Optional.ofNullable(JLSCRegistry.processors.get(id));
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreID> preIDOptional = JLSCRegistry.preIDs.stream().filter(p -> p.affects(id)).findFirst();
            if (preIDOptional.isPresent()) {
                JLSCPreID preID = preIDOptional.get();
                preID.apply(id);
                processorOptional = Optional.ofNullable(JLSCRegistry.processors.get(id));
            }
        }
        return processorOptional;
    }

    public static Optional<JLSCByteProcessor> getByteProcessor(String id) {
        Optional<JLSCByteProcessor> processorOptional = Optional.ofNullable(JLSCRegistry.byteProcessors.get(id));
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreID> preIDOptional = JLSCRegistry.preIDs.stream().filter(p -> p.affects(id)).findFirst();
            if (preIDOptional.isPresent()) {
                JLSCPreID preID = preIDOptional.get();
                preID.apply(id);
                processorOptional = Optional.ofNullable(JLSCRegistry.byteProcessors.get(id));
            }
        }
        return processorOptional;
    }

    public static <T> Optional<JLSCSerializer<T>> getSerializerFor(Class<T> type, JLSCValue value) {
        Optional<JLSCSerializer<T>> serializerOptional = (Optional) JLSCRegistry.serializers.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(type) && e.getValue().verifier().isValid(value)).map(Map.Entry::getValue).findFirst();
        if (!serializerOptional.isPresent()) {
            Optional<JLSCPreSerialize> preSerializeOptional = JLSCRegistry.preSerializes.stream().filter(p -> p.affects(type)).findFirst();
            if (preSerializeOptional.isPresent()) {
                JLSCPreSerialize preSerialize = preSerializeOptional.get();
                preSerialize.apply(type);
                serializerOptional = (Optional) JLSCRegistry.serializers.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(type) && e.getValue().verifier().isValid(value)).map(Map.Entry::getValue).findFirst();
            }
        }
        return serializerOptional;
    }

    public static Optional<JLSCProcessor> getProcessorFor(Class target) {
        Optional<JLSCProcessor> processorOptional = JLSCRegistry.processors.values().stream().filter(p -> p.canWrite(target)).findFirst();
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreProcess> preProcessOptional = JLSCRegistry.preProcesses.stream().filter(p -> p.affects(target)).findFirst();
            if (preProcessOptional.isPresent()) {
                JLSCPreProcess preProcess = preProcessOptional.get();
                preProcess.apply(target);
                processorOptional = JLSCRegistry.processors.values().stream().filter(p -> p.canWrite(target)).findFirst();
            }
        }
        return processorOptional;
    }

    public static Optional<JLSCProcessor> getProcessorFor(JLSCValue value) {
        Optional<JLSCProcessor> processorOptional = JLSCRegistry.processors.values().stream().filter(p -> p.canWrite(value)).findFirst();
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreProcess> preProcessOptional = JLSCRegistry.preProcesses.stream().filter(p -> p.affects(value.type())).findFirst();
            if (preProcessOptional.isPresent()) {
                JLSCPreProcess preProcess = preProcessOptional.get();
                preProcess.apply(value.type());
                processorOptional = JLSCRegistry.processors.values().stream().filter(p -> p.canWrite(value)).findFirst();
            }
        }
        return processorOptional;
    }

    public static Optional<JLSCProcessor> getProcessorFor(String read, JLSCSyntax syntax, JLSCStyle style) {
        Optional<JLSCProcessor> processorOptional = JLSCRegistry.processors.values().stream().filter(p -> p.canRead(read, syntax, style)).findFirst();
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreParse> preParseOptional = JLSCRegistry.preParses.stream().filter(p -> p.affects(read)).findFirst();
            if (preParseOptional.isPresent()) {
                JLSCPreParse preProcess = preParseOptional.get();
                preProcess.apply(read);
                processorOptional = JLSCRegistry.processors.values().stream().filter(p -> p.canRead(read, syntax, style)).findFirst();
            }
        }
        return processorOptional;
    }

    public static Optional<JLSCByteProcessor> getByteProcessorFor(Class target) {
        Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.byteProcessors.values().stream().filter(p -> p.canWriteBytes(target)).findFirst();
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreProcess> preProcessOptional = JLSCRegistry.preProcesses.stream().filter(p -> p.affects(target)).findFirst();
            if (preProcessOptional.isPresent()) {
                JLSCPreProcess preProcess = preProcessOptional.get();
                preProcess.apply(target);
                processorOptional = JLSCRegistry.byteProcessors.values().stream().filter(p -> p.canWriteBytes(target)).findFirst();
            }
        }
        return processorOptional;
    }

    public static Optional<JLSCByteProcessor> getByteProcessorFor(JLSCValue value) {
        Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.byteProcessors.values().stream().filter(p -> p.canWriteBytes(value)).findFirst();
        if (!processorOptional.isPresent()) {
            Optional<JLSCPreProcess> preProcessOptional = JLSCRegistry.preProcesses.stream().filter(p -> p.affects(value.type())).findFirst();
            if (preProcessOptional.isPresent()) {
                JLSCPreProcess preProcess = preProcessOptional.get();
                preProcess.apply(value.type());
                processorOptional = JLSCRegistry.byteProcessors.values().stream().filter(p -> p.canWriteBytes(value)).findFirst();
            }
        }
        return processorOptional;
    }

    public static Optional<JLSCSerializer> getSerializerFor(Object value) {
        Optional<JLSCSerializer> serializerOptional = JLSCRegistry.serializers.values().stream().filter(s -> s.canSerialize(value)).findFirst();
        if (!serializerOptional.isPresent() && value != null) {
            Optional<JLSCPreSerialize> preSerializeOptional = JLSCRegistry.preSerializes.stream().filter(p -> p.affects(value.getClass())).findFirst();
            if (preSerializeOptional.isPresent()) {
                JLSCPreSerialize preSerialize = preSerializeOptional.get();
                preSerialize.apply(value.getClass());
                serializerOptional = JLSCRegistry.serializers.values().stream().filter(s -> s.canSerialize(value)).findFirst();
            }
        }
        return serializerOptional;
    }

    public static Optional<JLSCSerializer> getSerializerFor(JLSCValue value) {
        Optional<JLSCSerializer> serializerOptional = JLSCRegistry.serializers.values().stream().filter(s -> s.verifier().isValid(value)).findFirst();
        if (!serializerOptional.isPresent() && value != null) {
            Optional<JLSCPreSerialize> preSerializeOptional = JLSCRegistry.preSerializes.stream().filter(p -> p.affects(value.getClass())).findFirst();
            if (preSerializeOptional.isPresent()) {
                JLSCPreSerialize preSerialize = preSerializeOptional.get();
                preSerialize.apply(value.getClass());
                serializerOptional = JLSCRegistry.serializers.values().stream().filter(s -> s.verifier().isValid(value)).findFirst();
            }
        }
        return serializerOptional;
    }

    public static long applicableCount(String read, JLSCSyntax syntax, JLSCStyle style) {
        return JLSCRegistry.processors.values().stream().filter(p -> p.canRead(read, syntax, style)).count();
    }

}
