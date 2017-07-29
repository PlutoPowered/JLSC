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
package com.gmail.socraticphoenix.jlsc;

import com.gmail.socraticphoenix.collect.coupling.Switch;
import com.gmail.socraticphoenix.jlsc.io.JLSCReadWriteUtil;
import com.gmail.socraticphoenix.jlsc.io.JLSCStyle;
import com.gmail.socraticphoenix.jlsc.io.JLSCSyntax;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.pio.ByteStream;
import com.gmail.socraticphoenix.pio.Bytes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JLSCConfiguration implements JLSCDataHolder, Iterable<JLSCKeyValue> {
    private JLSCCompound compound;
    private File file;
    private JLSCFormat format;
    private JLSCStyle style;
    private JLSCSyntax syntax;
    private boolean concurrent;

    public JLSCConfiguration(JLSCCompound compound, File file, JLSCFormat format, JLSCStyle style, JLSCSyntax syntax, boolean concurrent) {
        this.compound = compound;
        this.file = file;
        this.format = format;
        this.style = style;
        this.syntax = syntax;
        this.concurrent = concurrent;
    }

    public JLSCConfiguration(JLSCCompound compound, File file, JLSCFormat format, boolean concurrent) {
        this(compound, file, format, JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT, concurrent);
    }

    public static JLSCConfiguration fromBytes(File file, boolean concurrent) throws IOException, JLSCException {
        JLSCConfiguration configuration = new JLSCConfiguration(null, file, JLSCFormat.BYTES, concurrent);
        configuration.load();
        return configuration;
    }

    public static JLSCConfiguration fromCompressed(File file, boolean concurrent) throws IOException, JLSCException {
        JLSCConfiguration configuration = new JLSCConfiguration(null, file, JLSCFormat.COMPRESSED_BYTES, concurrent);
        configuration.load();
        return configuration;
    }

    public static JLSCConfiguration fromBytes(File file) throws IOException, JLSCException {
        return JLSCConfiguration.fromBytes(file, true);
    }

    public static JLSCConfiguration fromCompressed(File file) throws IOException, JLSCException {
        return JLSCConfiguration.fromCompressed(file, true);
    }

    public static JLSCConfiguration fromText(File file, JLSCStyle style, JLSCSyntax syntax, boolean concurrent) throws IOException, JLSCException {
        JLSCConfiguration configuration = new JLSCConfiguration(null, file, JLSCFormat.TEXT, style, syntax, concurrent);
        configuration.load();
        return configuration;
    }

    public static JLSCConfiguration fromText(File file, boolean concurrent) throws IOException, JLSCException {
        return JLSCConfiguration.fromText(file, JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT, concurrent);
    }

    public static JLSCConfiguration fromText(File file) throws IOException, JLSCException {
        return JLSCConfiguration.fromText(file, true);
    }

    public static JLSCCompound concurrent() {
        return JLSCCompound.concurrent();
    }

    public static JLSCCompound read(String src, boolean concurrent, JLSCStyle style, JLSCSyntax syntax) throws JLSCException {
        return JLSCCompound.read(src, concurrent, style, syntax);
    }

    public static JLSCCompound read(String src, boolean concurrent) throws JLSCException {
        return JLSCCompound.read(src, concurrent);
    }

    public static JLSCCompound read(String src) throws JLSCException {
        return JLSCCompound.read(src);
    }

    public void save() throws IOException, JLSCException {
        try (FileOutputStream fos = new FileOutputStream(this.file)) {
            switch (this.format) {
                case TEXT:
                    fos.write(this.compound.write(this.style, this.syntax).getBytes(StandardCharsets.UTF_8));
                    break;
                case BYTES:
                    fos.write(this.compound.writeBytes());
                    break;
                case COMPRESSED_BYTES:
                    fos.write(Bytes.compress(this.compound.writeBytes()));
                    break;
            }
        } catch (JLSCException | IOException e) {
            throw e;
        }
    }

    public void load() throws IOException, JLSCException {
        switch (this.format) {
            case TEXT:
                this.compound = JLSCCompound.read(Bytes.readAllText(this.file), false);
                break;
            case BYTES:
                this.compound = JLSCReadWriteUtil.readCompound(ByteStream.of(Files.readAllBytes(this.file.toPath())), LinkedHashMap::new, ArrayList::new);
                break;
            case COMPRESSED_BYTES:
                this.compound = JLSCReadWriteUtil.readCompound(ByteStream.of(Bytes.decompress(Files.readAllBytes(this.file.toPath()))), LinkedHashMap::new, ArrayList::new);
                break;
        }
        if (this.concurrent) {
            this.compound = compound.toConcurrent();
        }
    }

    public boolean isConcurrent() {
        return this.concurrent;
    }

    public JLSCConfiguration setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
        return this;
    }

    public JLSCCompound getCompound() {
        return this.compound;
    }

    public File getFile() {
        return this.file;
    }

    public JLSCConfiguration setFile(File file) {
        this.file = file;
        return this;
    }

    public JLSCFormat getFormat() {
        return this.format;
    }

    public JLSCConfiguration setFormat(JLSCFormat format) {
        this.format = format;
        return this;
    }

    public JLSCStyle getStyle() {
        return this.style;
    }

    public JLSCConfiguration setStyle(JLSCStyle style) {
        this.style = style;
        return this;
    }

    public JLSCSyntax getSyntax() {
        return this.syntax;
    }

    public JLSCConfiguration setSyntax(JLSCSyntax syntax) {
        this.syntax = syntax;
        return this;
    }

    public boolean contains(JLSCQuery query) {
        return this.compound.contains(query);
    }

    public void put(JLSCQuery query, JLSCValue value) {
        this.compound.put(query, value);
    }

    public Optional<JLSCValue> get(JLSCQuery query) {
        return this.compound.get(query);
    }

    public Optional<JLSCValue> get(Object... path) {
        return this.compound.get(path);
    }

    public void traverse(Consumer<JLSCValue> consumer, boolean deep) {
        this.compound.traverse(consumer, deep);
    }

    public void absorbMetadata(JLSCCompound other) {
        this.compound.absorbMetadata(other);
    }

    public byte[] writeBytes() throws JLSCException {
        return this.compound.writeBytes();
    }

    public String write(JLSCStyle style, JLSCSyntax syntax) throws JLSCException {
        return this.compound.write(style, syntax);
    }

    public String write() throws JLSCException {
        return this.compound.write();
    }

    public JLSCCompound toConcurrent() {
        return this.compound.toConcurrent();
    }

    public boolean addComments(String key, List<String> comments) {
        return this.compound.addComments(key, comments);
    }

    public boolean addComments(String key, String... comments) {
        return this.compound.addComments(key, comments);
    }

    public boolean removeComments(String key, Predicate<String> remove) {
        return this.compound.removeComments(key, remove);
    }

    public boolean removeComments(String key, String... remove) {
        return this.compound.removeComments(key, remove);
    }

    public boolean addProperties(String key, List<JLSCValueProperty> properties) {
        return this.compound.addProperties(key, properties);
    }

    public boolean addProperties(String key, String... properties) {
        return this.compound.addProperties(key, properties);
    }

    public boolean removeProperties(String key, Predicate<JLSCValueProperty> remove) {
        return this.compound.removeProperties(key, remove);
    }

    public boolean removeProperties(String key, String... remove) {
        return this.compound.removeProperties(key, remove);
    }

    public int size() {
        return this.compound.size();
    }

    public boolean isEmpty() {
        return this.compound.isEmpty();
    }

    public boolean containsKey(String key) {
        return this.compound.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.compound.containsValue(value);
    }

    public boolean containsValue(JLSCValue value) {
        return this.compound.containsValue(value);
    }

    public Optional<JLSCKeyValue> put(String key, Object value) {
        return this.compound.put(key, value);
    }

    public Optional<JLSCKeyValue> put(String key, JLSCValue value) {
        return this.compound.put(key, value);
    }

    public Optional<JLSCKeyValue> remove(String key) {
        return this.compound.remove(key);
    }

    public void putAllValues(Map<? extends String, ?> m) {
        this.compound.putAllValues(m);
    }

    public void putAll(Map<? extends String, ? extends JLSCValue> m) {
        this.compound.putAll(m);
    }

    public void clear() {
        this.compound.clear();
    }

    public Set<String> keys() {
        return this.compound.keys();
    }

    public Collection<JLSCValue> values() {
        return this.compound.values();
    }

    public List<JLSCKeyValue> entries() {
        return this.compound.entries();
    }

    public void forEachValue(BiConsumer<? super String, Object> action) {
        this.compound.forEachValue(action);
    }

    public void forEach(BiConsumer<? super String, ? super JLSCValue> action) {
        this.compound.forEach(action);
    }

    public void replaceAllValues(BiFunction<? super String, Object, Object> function) {
        this.compound.replaceAllValues(function);
    }

    public void replaceAll(BiFunction<? super String, ? super JLSCValue, ? extends JLSCValue> function) {
        this.compound.replaceAll(function);
    }

    public Optional<JLSCKeyValue> putIfAbsent(String key, Object value) {
        return this.compound.putIfAbsent(key, value);
    }

    public Optional<JLSCKeyValue> putIfAbsent(String key, JLSCValue value) {
        return this.compound.putIfAbsent(key, value);
    }

    public Optional<JLSCKeyValue> replace(String key, Object value) {
        return this.compound.replace(key, value);
    }

    public Optional<JLSCKeyValue> replace(String key, JLSCValue value) {
        return this.compound.replace(key, value);
    }

    public Optional<JLSCValue> get(String key) {
        return this.compound.get(key);
    }

    public <T> Optional<T> getAs(JLSCQuery query, Class<T> type) {
        return this.compound.getAs(query, type);
    }

    public <T> T getAs(JLSCQuery query, Class<T> type, T def) {
        return this.compound.getAs(query, type, def);
    }

    public <T> Optional<T> getAs(String key, Class<T> type) {
        return this.compound.getAs(key, type);
    }

    public <T> T getAs(String key, Class<T> type, T def) {
        return this.compound.getAs(key, type, def);
    }

    public <T> T getAsOrNull(String key, Class<T> type) {
        return this.compound.getAsOrNull(key, type);
    }

    public Optional<Byte> getByte(String key) {
        return this.compound.getByte(key);
    }

    public Byte getByte(String key, Byte def) {
        return this.compound.getByte(key, def);
    }

    public Byte getByteOrNull(String key) {
        return this.compound.getByteOrNull(key);
    }

    public Optional<Short> getShort(String key) {
        return this.compound.getShort(key);
    }

    public Short getShort(String key, Short def) {
        return this.compound.getShort(key, def);
    }

    public Short getShortOrNull(String key) {
        return this.compound.getShortOrNull(key);
    }

    public Optional<Character> getCharacter(String key) {
        return this.compound.getCharacter(key);
    }

    public Character getCharacter(String key, Character def) {
        return this.compound.getCharacter(key, def);
    }

    public Character getCharacterOrNull(String key) {
        return this.compound.getCharacterOrNull(key);
    }

    public Optional<Integer> getInteger(String key) {
        return this.compound.getInteger(key);
    }

    public Integer getInteger(String key, Integer def) {
        return this.compound.getInteger(key, def);
    }

    public Integer getIntegerOrNull(String key) {
        return this.compound.getIntegerOrNull(key);
    }

    public Optional<Long> getLong(String key) {
        return this.compound.getLong(key);
    }

    public Long getLong(String key, Long def) {
        return this.compound.getLong(key, def);
    }

    public Long getLongOrNull(String key) {
        return this.compound.getLongOrNull(key);
    }

    public Optional<Float> getFloat(String key) {
        return this.compound.getFloat(key);
    }

    public Float getFloat(String key, Float def) {
        return this.compound.getFloat(key, def);
    }

    public Float getFloatOrNull(String key) {
        return this.compound.getFloatOrNull(key);
    }

    public Optional<Double> getDouble(String key) {
        return this.compound.getDouble(key);
    }

    public Double getDouble(String key, Double def) {
        return this.compound.getDouble(key, def);
    }

    public Double getDoubleOrNull(String key) {
        return this.compound.getDoubleOrNull(key);
    }

    public Optional<Boolean> getBoolean(String key) {
        return this.compound.getBoolean(key);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return this.compound.getBoolean(key, def);
    }

    public Boolean getBooleanOrNull(String key) {
        return this.compound.getBooleanOrNull(key);
    }

    public Optional<String> getString(String key) {
        return this.compound.getString(key);
    }

    public String getString(String key, String def) {
        return this.compound.getString(key, def);
    }

    public String getStringOrNull(String key) {
        return this.compound.getStringOrNull(key);
    }

    public Optional<JLSCDataHolder> getDataHolder(String key) {
        return this.compound.getDataHolder(key);
    }

    public JLSCDataHolder getDataHolder(String key, JLSCDataHolder def) {
        return this.compound.getDataHolder(key, def);
    }

    public JLSCDataHolder getDataHolderOrNull(String key) {
        return this.compound.getDataHolderOrNull(key);
    }

    public Optional<JLSCArray> getArray(String key) {
        return this.compound.getArray(key);
    }

    public JLSCArray getArray(String key, JLSCArray def) {
        return this.compound.getArray(key, def);
    }

    public JLSCArray getArrayOrNull(String key) {
        return this.compound.getArrayOrNull(key);
    }

    public Optional<JLSCCompound> getCompound(String key) {
        return this.compound.getCompound(key);
    }

    public JLSCCompound getCompound(String key, JLSCCompound def) {
        return this.compound.getCompound(key, def);
    }

    public JLSCCompound getCompoundOrNull(String key) {
        return this.compound.getCompoundOrNull(key);
    }

    public Optional<Byte> getByte(JLSCQuery query) {
        return this.compound.getByte(query);
    }

    public Byte getByte(JLSCQuery query, Byte def) {
        return this.compound.getByte(query, def);
    }

    public Byte getByteOrNull(JLSCQuery query) {
        return this.compound.getByteOrNull(query);
    }

    public Optional<Short> getShort(JLSCQuery query) {
        return this.compound.getShort(query);
    }

    public Short getShort(JLSCQuery query, Short def) {
        return this.compound.getShort(query, def);
    }

    public Short getShortOrNull(JLSCQuery query) {
        return this.compound.getShortOrNull(query);
    }

    public Optional<Character> getCharacter(JLSCQuery query) {
        return this.compound.getCharacter(query);
    }

    public Character getCharacter(JLSCQuery query, Character def) {
        return this.compound.getCharacter(query, def);
    }

    public Character getCharacterOrNull(JLSCQuery query) {
        return this.compound.getCharacterOrNull(query);
    }

    public Optional<Integer> getInteger(JLSCQuery query) {
        return this.compound.getInteger(query);
    }

    public Integer getInteger(JLSCQuery query, Integer def) {
        return this.compound.getInteger(query, def);
    }

    public Integer getIntegerOrNull(JLSCQuery query) {
        return this.compound.getIntegerOrNull(query);
    }

    public Optional<Long> getLong(JLSCQuery query) {
        return this.compound.getLong(query);
    }

    public Long getLong(JLSCQuery query, Long def) {
        return this.compound.getLong(query, def);
    }

    public Long getLongOrNull(JLSCQuery query) {
        return this.compound.getLongOrNull(query);
    }

    public Optional<Float> getFloat(JLSCQuery query) {
        return this.compound.getFloat(query);
    }

    public Float getFloat(JLSCQuery query, Float def) {
        return this.compound.getFloat(query, def);
    }

    public Float getFloatOrNull(JLSCQuery query) {
        return this.compound.getFloatOrNull(query);
    }

    public Optional<Double> getDouble(JLSCQuery query) {
        return this.compound.getDouble(query);
    }

    public Double getDouble(JLSCQuery query, Double def) {
        return this.compound.getDouble(query, def);
    }

    public Double getDoubleOrNull(JLSCQuery query) {
        return this.compound.getDoubleOrNull(query);
    }

    public Optional<Boolean> getBoolean(JLSCQuery query) {
        return this.compound.getBoolean(query);
    }

    public Boolean getBoolean(JLSCQuery query, Boolean def) {
        return this.compound.getBoolean(query, def);
    }

    public Boolean getBooleanOrNull(JLSCQuery query) {
        return this.compound.getBooleanOrNull(query);
    }

    public Optional<String> getString(JLSCQuery query) {
        return this.compound.getString(query);
    }

    public String getString(JLSCQuery query, String def) {
        return this.compound.getString(query, def);
    }

    public String getStringOrNull(JLSCQuery query) {
        return this.compound.getStringOrNull(query);
    }

    public Optional<JLSCDataHolder> getDataHolder(JLSCQuery query) {
        return this.compound.getDataHolder(query);
    }

    public JLSCDataHolder getDataHolder(JLSCQuery query, JLSCDataHolder def) {
        return this.compound.getDataHolder(query, def);
    }

    public JLSCDataHolder getDataHolderOrNull(JLSCQuery query) {
        return this.compound.getDataHolderOrNull(query);
    }

    public Optional<JLSCArray> getArray(JLSCQuery query) {
        return this.compound.getArray(query);
    }

    public JLSCArray getArray(JLSCQuery query, JLSCArray def) {
        return this.compound.getArray(query, def);
    }

    public JLSCArray getArrayOrNull(JLSCQuery query) {
        return this.compound.getArrayOrNull(query);
    }

    public Optional<JLSCCompound> getCompound(JLSCQuery query) {
        return this.compound.getCompound(query);
    }

    public JLSCCompound getCompound(JLSCQuery query, JLSCCompound def) {
        return this.compound.getCompound(query, def);
    }

    public JLSCCompound getCompoundOrNull(JLSCQuery query) {
        return this.compound.getCompoundOrNull(query);
    }

    @Override
    public Optional<JLSCValue> get(Stack<Switch<String, Integer>> pathStack) {
        return this.compound.get(pathStack);
    }

    @Override
    public void put(Stack<Switch<String, Integer>> pathStack, JLSCValue value) {
        this.compound.put(pathStack, value);
    }

    @Override
    public List<JLSCQuery> paths(boolean deep) {
        return this.compound.paths(deep);
    }

    @Override
    public List<JLSCValue> leaves(boolean deep) {
        return this.compound.leaves(deep);
    }

    public void put(JLSCKeyValue keyValue) {
        this.compound.put(keyValue);
    }

    @Override
    public Iterator<JLSCKeyValue> iterator() {
        return this.compound.iterator();
    }

    public void forEach(Consumer<? super JLSCKeyValue> action) {
        this.compound.forEach(action);
    }

    public Spliterator<JLSCKeyValue> spliterator() {
        return this.compound.spliterator();
    }
}
