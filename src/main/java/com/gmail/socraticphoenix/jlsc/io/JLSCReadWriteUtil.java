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
package com.gmail.socraticphoenix.jlsc.io;

import com.gmail.socraticphoenix.collect.Items;
import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.header.JLSCArrayHeader;
import com.gmail.socraticphoenix.jlsc.header.JLSCCompoundHeader;
import com.gmail.socraticphoenix.jlsc.header.JLSCKeyValueHeader;
import com.gmail.socraticphoenix.jlsc.header.JLSCValueHeader;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.registry.JLSCRegistry;
import com.gmail.socraticphoenix.jlsc.value.JLSCByteProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.jlsc.value.JLSCProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.parse.Strings;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class JLSCReadWriteUtil {

    public static JLSCCompound readCompound(String src, JLSCSyntax syntax, JLSCStyle style, Supplier<Map<String, JLSCKeyValue>> compoundConstructor, Supplier<List<JLSCValue>> arrayConstructor) throws JLSCException {
        src = src.trim();
        if (src.startsWith("{") && src.endsWith("}")) {
            src = Strings.cutFirst(Strings.cutLast(src));
        }
        JLSCTokenizer tokenizer = new JLSCTokenizer(src, style, syntax, compoundConstructor, arrayConstructor);
        JLSCCompound compound = new JLSCCompound(compoundConstructor);
        while (tokenizer.hasNext()) {
            compound.put(tokenizer.compoundNext());
        }
        return compound;
    }

    public static JLSCArray readArray(String src, JLSCSyntax syntax, JLSCStyle style, Supplier<Map<String, JLSCKeyValue>> compoundConstructor, Supplier<List<JLSCValue>> arrayConstructor) throws JLSCException {
        src = src.trim();
        if (src.startsWith("[") && src.endsWith("]")) {
            src = Strings.cutFirst(Strings.cutLast(src));
        }
        JLSCTokenizer tokenizer = new JLSCTokenizer(src, style, syntax, compoundConstructor, arrayConstructor);
        JLSCArray array = new JLSCArray(arrayConstructor);
        while (tokenizer.hasNext()) {
            array.add(tokenizer.arrayNext());
        }
        return array;
    }

    public static boolean requiresSpecifier(JLSCValue value, String read, JLSCStyle style, JLSCSyntax syntax) {
        return JLSCRegistry.getProcessorFor(read, syntax, style).orElse(null) != JLSCRegistry.getProcessorFor(value).orElse(null);
    }

    public static boolean requiresQuotation(String key, String eKey, JLSCStyle style, JLSCSyntax syntax) {
        if(!key.equals(eKey) || style.alwaysUseKeyQuotes()) {
            return true;
        }

        for(char c : key.toCharArray()) {
            if(syntax.keyValueDelimiter().test(c) || syntax.nonConsumableDelimiter().test(c)) {
                return true;
            }
        }

        return false;
    }

    public static String writeCompound(JLSCCompound compound, JLSCSyntax syntax, JLSCStyle style, int indent) throws JLSCException {
        StringBuilder builder = new StringBuilder();
        Iterator<JLSCKeyValue> iterator = compound.iterator();
        while (iterator.hasNext()) {
            JLSCKeyValue keyValue = iterator.next();
            for (String comment : keyValue.getComments()) {
                builder.append(style.beginComment(indent)).append(comment).append(style.endComment(indent));
            }
            builder.append(style.preKey(indent));
            String eKey = Strings.escape(keyValue.getKey());
            builder.append(!JLSCReadWriteUtil.requiresQuotation(keyValue.getKey(), eKey, style, syntax) ? keyValue.getKey() : "\"" + eKey + "\"");
            builder.append(style.delimiter(indent));

            List<JLSCValueProperty> properties = Items.looseClone(keyValue.getProperties());
            JLSCValue value = keyValue.getValue().getForWriting();
            String result;
            if (value.getAsCompound().isPresent()) {
                try {
                    result = style.beginCompound(indent) + JLSCReadWriteUtil.writeCompound(value.getAsCompound().get(), syntax, style, indent + 1) + style.endCompound(indent);
                } catch (JLSCException e) {
                    throw new JLSCException("Error while writing value at key \"" + eKey + "\"", e);
                }
            } else if (value.getAsArray().isPresent()) {
                try {
                    result = style.beginArray(indent) + JLSCReadWriteUtil.writeArray(value.getAsArray().get(), syntax, style, indent + 1) + style.endArray(indent);
                } catch (JLSCException e) {
                    throw new JLSCException("Error while writing value at key \"" + eKey + "\"", e);
                }
            } else {
                Optional<JLSCProcessor> processor = JLSCRegistry.getProcessorFor(value);
                if (processor.isPresent()) {
                    try {
                        result = processor.get().write(value, indent, syntax, style);
                        if (JLSCReadWriteUtil.requiresSpecifier(value, result, style, syntax)) {
                            properties.add(0, new JLSCValueProperty(processor.get().id()));
                        }
                    } catch (JLSCException e) {
                        throw new JLSCException("Error while writing value at key \"" + eKey + "\"", e);
                    }
                } else {
                    throw new JLSCException("No processor for value of type \"" + value.type().getName() + "\"");
                }
            }

            for (JLSCValueProperty property : properties) {
                builder.append(style.beginProperty(indent)).append(property.getName());
                if (!property.getArguments().isEmpty()) {
                    builder.append(style.beginPropertyArgs(indent));
                    List<String> arguments = property.getArguments();
                    for (int i = 0; i < arguments.size(); i++) {
                        builder.append(arguments.get(i));
                        if (i < arguments.size() - 1) {
                            builder.append(style.propertyArgsDelimiter(indent));
                        }
                    }
                    builder.append(style.endPropertyArgs(indent));
                }
                builder.append(style.endProperty(indent));
            }

            builder.append(result);

            if (iterator.hasNext() || style.doLastCompoundValue()) {
                builder.append(style.compoundValueDelimiter(indent));
            }

        }
        return builder.toString();
    }

    public static String writeArray(JLSCArray array, JLSCSyntax syntax, JLSCStyle style, int indent) throws JLSCException {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        Iterator<JLSCValue> iterator = array.iterator();
        while (iterator.hasNext()) {
            builder.append(style.preArrayVal(indent));

            JLSCValue value = iterator.next().getForWriting();
            List<JLSCValueProperty> properties = Items.looseClone(value.getProperties());
            String result;
            if (value.getAsCompound().isPresent()) {
                try {
                    result = style.beginCompound(indent) + JLSCReadWriteUtil.writeCompound(value.getAsCompound().get(), syntax, style, indent + 1) + style.endCompound(indent);
                } catch (JLSCException e) {
                    throw new JLSCException("Error while writing value at index " + index, e);
                }
            } else if (value.getAsArray().isPresent()) {
                try {
                    result = style.beginArray(indent) + JLSCReadWriteUtil.writeArray(value.getAsArray().get(), syntax, style, indent + 1) + style.endArray(indent);
                } catch (JLSCException e) {
                    throw new JLSCException("Error while writing value at index " + index, e);
                }
            } else {
                Optional<JLSCProcessor> processor = JLSCRegistry.getProcessorFor(value);
                if (processor.isPresent()) {
                    try {
                        result = processor.get().write(value, indent, syntax, style);
                        if (JLSCReadWriteUtil.requiresSpecifier(value, result, style, syntax)) {
                            properties.add(0, new JLSCValueProperty(processor.get().id()));
                        }
                    } catch (JLSCException e) {
                        throw new JLSCException("Error while writing value at index " + indent, e);
                    }
                } else {
                    throw new JLSCException("No processor for value of type \"" + value.type().getName() + "\"");
                }
            }

            for (JLSCValueProperty property : properties) {
                builder.append(style.beginProperty(indent)).append(property.getName());
                if (!property.getArguments().isEmpty()) {
                    builder.append(style.beginPropertyArgs(indent));
                    List<String> arguments = property.getArguments();
                    for (int i = 0; i < arguments.size(); i++) {
                        builder.append(arguments.get(i));
                        if (i < arguments.size() - 1) {
                            builder.append(style.propertyArgsDelimiter(indent));
                        }
                    }
                    builder.append(style.endPropertyArgs(indent));
                }
                builder.append(style.endProperty(indent));
            }

            builder.append(result);

            if (iterator.hasNext() || style.doLastArrayValue()) {
                builder.append(style.arrayValueDelimiter(indent));
            }

            index++;
        }

        return builder.toString();
    }

    public static JLSCCompound readCompound(ByteStream buffer, Supplier<Map<String, JLSCKeyValue>> compoundConstructor, Supplier<List<JLSCValue>> arrayConstructor) throws JLSCException {
        try {
            JLSCCompound compound = new JLSCCompound(compoundConstructor);
            JLSCCompoundHeader header = JLSCCompoundHeader.read(buffer);
            for (JLSCKeyValueHeader keyValueHeader : header.getKeyValueHeaders()) {
                try {
                    JLSCValue value;
                    String type = keyValueHeader.getValueHeader().getTypeSpecifier().getName();
                    if (type.equals("array")) {
                        value = JLSCValue.of(JLSCReadWriteUtil.readArray(buffer, compoundConstructor, arrayConstructor));
                    } else if (type.equals("compound")) {
                        value = JLSCValue.of(JLSCReadWriteUtil.readCompound(buffer, compoundConstructor, arrayConstructor));
                    } else {
                        Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.getByteProcessor(type);
                        if (processorOptional.isPresent()) {
                            value = processorOptional.get().readBytes(buffer);
                        } else {
                            throw new JLSCException("Unable to read value at key \"" + Strings.escape(keyValueHeader.getKey()) + "\", no byte processor found for id \"" + keyValueHeader.getValueHeader().getTypeSpecifier().getName() + "\"");
                        }
                    }
                    value.getProperties().addAll(keyValueHeader.getValueHeader().getProperties());
                    value.setTypeSpecifier(keyValueHeader.getValueHeader().getTypeSpecifier());
                    JLSCKeyValue keyValue = new JLSCKeyValue(keyValueHeader.getKey(), value);
                    keyValue.getComments().addAll(keyValueHeader.getComments());
                    compound.put(keyValue);
                } catch (BufferOverflowException | BufferUnderflowException e) {
                    throw new JLSCException("Incorrect buffer size at key \"" + Strings.escape(keyValueHeader.getKey()) + "\"", e);
                }
            }
            return compound;
        } catch (BufferOverflowException | BufferUnderflowException e) {
            throw new JLSCException("Incorrect buffer size", e);
        }
    }

    public static JLSCArray readArray(ByteStream buffer, Supplier<Map<String, JLSCKeyValue>> compoundConstructor, Supplier<List<JLSCValue>> arrayConstructor) throws JLSCException {
        try {
            JLSCArray array = new JLSCArray(arrayConstructor);
            JLSCArrayHeader header = JLSCArrayHeader.read(buffer);
            List<JLSCValueHeader> valueHeaders = header.getValueHeaders();
            for (int i = 0; i < valueHeaders.size(); i++) {
                try {
                    JLSCValueHeader valueHeader = valueHeaders.get(i);
                    JLSCValue value;
                    String type = valueHeader.getTypeSpecifier().getName();
                    if (type.equals("array")) {
                        value = JLSCValue.of(JLSCReadWriteUtil.readArray(buffer, compoundConstructor, arrayConstructor));
                    } else if (type.equals("compound")) {
                        value = JLSCValue.of(JLSCReadWriteUtil.readCompound(buffer, compoundConstructor, arrayConstructor));
                    } else {
                        Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.getByteProcessor(type);
                        if (processorOptional.isPresent()) {
                            value = processorOptional.get().readBytes(buffer);
                        } else {
                            throw new JLSCException("Unable to read vale at index " + i + ", no byte processor found for id \"" + valueHeader.getTypeSpecifier().getName() + "\"");
                        }
                    }
                    value.getProperties().addAll(valueHeader.getProperties());
                    value.setTypeSpecifier(valueHeader.getTypeSpecifier());
                    array.add(value);
                } catch (BufferOverflowException | BufferUnderflowException e) {
                    throw new JLSCException("Incorrect buffer size at index " + i, e);
                }
            }
            return array;
        } catch (BufferOverflowException | BufferUnderflowException e) {
            throw new JLSCException("Incorrect buffer size", e);
        }
    }

    public static int length(JLSCCompound compound) {
        JLSCCompoundHeader header = new JLSCCompoundHeader(compound);
        int len = header.length();
        for (JLSCKeyValue keyValue : compound.entries()) {
            JLSCValue value = keyValue.getValue().getForWriting();
            if (value.getAsArray().isPresent()) {
                len += JLSCReadWriteUtil.length(value.getAsArray().get());
            } else if (value.getAsCompound().isPresent()) {
                len += JLSCReadWriteUtil.length(value.getAsCompound().get());
            } else {
                Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.getByteProcessorFor(value);
                if (processorOptional.isPresent()) {
                    len += processorOptional.get().size(value);
                }
            }
        }
        return len;
    }

    public static int length(JLSCArray array) {
        JLSCArrayHeader header = new JLSCArrayHeader(array);
        int len = header.length();
        for (JLSCValue value : array.leaves(false)) {
            value = value.getForWriting();
            if (value.getAsArray().isPresent()) {
                len += JLSCReadWriteUtil.length(value.getAsArray().get());
            } else if (value.getAsCompound().isPresent()) {
                len += JLSCReadWriteUtil.length(value.getAsCompound().get());
            } else {
                Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.getByteProcessorFor(value);
                if (processorOptional.isPresent()) {
                    len += processorOptional.get().size(value);
                }
            }
        }
        return len;
    }

    public static void write(JLSCCompound compound, ByteStream buffer) throws JLSCException {
        JLSCCompoundHeader header = new JLSCCompoundHeader(compound);
        header.write(buffer);
        for (JLSCKeyValue keyValue : compound.entries()) {
            JLSCValue value = keyValue.getValue().getForWriting();
            if (value.getAsArray().isPresent()) {
                JLSCReadWriteUtil.write(value.getAsArray().get(), buffer);
            } else if (value.getAsCompound().isPresent()) {
                JLSCReadWriteUtil.write(value.getAsCompound().get(), buffer);
            } else {
                Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.getByteProcessorFor(value);
                if (processorOptional.isPresent()) {
                    JLSCByteProcessor processor = processorOptional.get();
                    processor.write(buffer, value);
                } else {
                    throw new JLSCException("Unable to write value at key \"" + Strings.escape(keyValue.getKey()) + "\", no byte processor found for value of type \"" + value.type().getName() + "\"");
                }
            }
        }
    }

    public static void write(JLSCArray array, ByteStream buffer) throws JLSCException {
        JLSCArrayHeader header = new JLSCArrayHeader(array);
        header.write(buffer);
        for (int i = 0; i < array.size(); i++) {
            JLSCValue value = array.get(i).get().getForWriting();
            if (value.getAsArray().isPresent()) {
                JLSCReadWriteUtil.write(value.getAsArray().get(), buffer);
            } else if (value.getAsCompound().isPresent()) {
                JLSCReadWriteUtil.write(value.getAsCompound().get(), buffer);
            } else {
                Optional<JLSCByteProcessor> processorOptional = JLSCRegistry.getByteProcessorFor(value);
                if (processorOptional.isPresent()) {
                    JLSCByteProcessor processor = processorOptional.get();
                    processor.write(buffer, value);
                } else {
                    throw new JLSCException("Unable to write value at index " + i + ", no byte processor found for value of type \"" + value.type().getName() + "\"");
                }
            }
        }
    }

}
