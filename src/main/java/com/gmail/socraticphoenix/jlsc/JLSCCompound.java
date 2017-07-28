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

import com.gmail.socraticphoenix.collect.Items;
import com.gmail.socraticphoenix.collect.coupling.Switch;
import com.gmail.socraticphoenix.jlsc.io.JLSCReadWriteUtil;
import com.gmail.socraticphoenix.jlsc.io.JLSCStyle;
import com.gmail.socraticphoenix.jlsc.io.JLSCSyntax;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JLSCCompound implements JLSCDataHolder, Iterable<JLSCKeyValue> {
    private Map<String, JLSCKeyValue> values;

    private JLSCCompound(Map<String, JLSCKeyValue> values) {
        this.values = values;
    }

    public JLSCCompound() {
        this(new LinkedHashMap<>());
    }

    public JLSCCompound(Supplier<Map<String, JLSCKeyValue>> constructor) {
        this(constructor.get());
    }

    public static JLSCCompound concurrent() {
        return new JLSCCompound(Collections.synchronizedMap(new LinkedHashMap<>()));
    }

    public static JLSCCompound read(String src, boolean concurrent, JLSCStyle style, JLSCSyntax syntax) throws JLSCException {
        return JLSCReadWriteUtil.readCompound(src, syntax, style, concurrent ? () -> Collections.synchronizedMap(new LinkedHashMap<>()) : LinkedHashMap::new, concurrent ? () -> Collections.synchronizedList(new ArrayList<>()) : ArrayList::new);
    }

    public static JLSCCompound read(String src, boolean concurrent) throws JLSCException {
        return JLSCCompound.read(src, concurrent, JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT);
    }

    public static JLSCCompound read(String src) throws JLSCException {
        return JLSCCompound.read(src, true);
    }

    public void absorbMetadata(JLSCCompound other) {
        for (String key : this.keys()) {
            JLSCKeyValue keyValue = other.values.get(key);
            if (keyValue != null) {
                this.values.get(key).absorbMetadata(keyValue);
            }
        }
    }

    public byte[] writeBytes() throws JLSCException {
        int size = JLSCReadWriteUtil.length(this);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        JLSCReadWriteUtil.write(this, ByteStream.of(buffer));
        return buffer.array();
    }

    public String write(JLSCStyle style, JLSCSyntax syntax) throws JLSCException {
        return JLSCReadWriteUtil.writeCompound(this, syntax, style, 0);
    }

    public String write() throws JLSCException {
        return this.write(JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT);
    }

    public JLSCCompound toConcurrent() {
        JLSCCompound compound = JLSCCompound.concurrent();
        compound.values.putAll(this.values);
        return compound;
    }

    public boolean addComments(String key, List<String> comments) {
        JLSCKeyValue keyValue = this.values.get(key);
        if (keyValue != null) {
            keyValue.getComments().addAll(comments);
            return true;
        }
        return false;
    }

    public boolean addComments(String key, String... comments) {
        return this.addComments(key, Items.buildList(comments));
    }

    public boolean removeComments(String key, Predicate<String> remove) {
        JLSCKeyValue keyValue = this.values.get(key);
        if (keyValue != null) {
            keyValue.getComments().removeIf(remove);
            return true;
        }
        return false;
    }

    public boolean removeComments(String key, String... remove) {
        return this.removeComments(key, s -> Items.contains(s, remove));
    }

    public boolean addProperties(String key, List<JLSCValueProperty> properties) {
        JLSCKeyValue keyValue = this.values.get(key);
        if (keyValue != null) {
            keyValue.getProperties().addAll(properties);
            return true;
        }
        return false;
    }

    public boolean addProperties(String key, String... properties) {
        return this.addProperties(key, Stream.of(properties).map(JLSCValueProperty::new).collect(Collectors.toList()));
    }

    public boolean removeProperties(String key, Predicate<JLSCValueProperty> remove) {
        JLSCKeyValue keyValue = this.values.get(key);
        if (keyValue != null) {
            keyValue.getProperties().removeIf(remove);
            return true;
        }
        return false;
    }

    public boolean removeProperties(String key, String... remove) {
        return this.removeProperties(key, p -> Items.contains(p.getName(), remove));
    }

    public int size() {
        return this.values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean containsKey(String key) {
        return this.values.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.containsValue(JLSCValue.of(value));
    }

    public boolean containsValue(JLSCValue value) {
        return this.values.values().stream().filter(k -> k.getValue().equals(value)).findFirst().isPresent();
    }

    public Optional<JLSCKeyValue> put(String key, Object value) {
        return this.put(key, JLSCValue.of(value));
    }

    public Optional<JLSCKeyValue> put(String key, JLSCValue value) {
        return Optional.ofNullable(this.values.put(key, new JLSCKeyValue(key, value)));
    }

    public Optional<JLSCKeyValue> remove(String key) {
        return Optional.ofNullable(this.values.remove(key));
    }

    public void putAllValues(Map<? extends String, ?> m) {
        m.entrySet().forEach(e -> this.put(e.getKey(), JLSCValue.of(e.getValue())));
    }

    public void putAll(Map<? extends String, ? extends JLSCValue> m) {
        m.entrySet().forEach(e -> this.put(e.getKey(), e.getValue()));
    }

    public void clear() {
        this.values.clear();
    }

    public Set<String> keys() {
        return this.values.keySet();
    }

    public Collection<JLSCValue> values() {
        return this.values.values().stream().map(JLSCKeyValue::getValue).collect(Collectors.toList());
    }

    public List<JLSCKeyValue> entries() {
        return Items.looseClone(this.values.values(), ArrayList::new);
    }

    public void forEachValue(BiConsumer<? super String, Object> action) {
        this.forEach((a, b) -> action.accept(a, b.rawValue()));
    }

    public void forEach(BiConsumer<? super String, ? super JLSCValue> action) {
        this.values.forEach((a, b) -> action.accept(a, b.getValue()));
    }

    public void replaceAllValues(BiFunction<? super String, Object, Object> function) {
        this.replaceAll((a, b) -> JLSCValue.of(function.apply(a, b.rawValue())));
    }

    public void replaceAll(BiFunction<? super String, ? super JLSCValue, ? extends JLSCValue> function) {
        this.values.replaceAll((a, b) -> new JLSCKeyValue(a, function.apply(a, b.getValue())));
    }

    public Optional<JLSCKeyValue> putIfAbsent(String key, Object value) {
        return this.putIfAbsent(key, JLSCValue.of(value));
    }

    public Optional<JLSCKeyValue> putIfAbsent(String key, JLSCValue value) {
        return Optional.ofNullable(this.values.putIfAbsent(key, new JLSCKeyValue(key, value)));
    }

    public Optional<JLSCKeyValue> replace(String key, Object value) {
        return this.replace(key, JLSCValue.of(value));
    }

    public Optional<JLSCKeyValue> replace(String key, JLSCValue value) {
        return Optional.ofNullable(this.values.replace(key, new JLSCKeyValue(key, value)));
    }

    public Optional<JLSCValue> get(String key) {
        JLSCKeyValue keyValue = this.values.get(key);
        if (keyValue == null) {
            return Optional.empty();
        } else {
            return Optional.of(keyValue.getValue());
        }
    }

    public <T> Optional<T> getAs(JLSCQuery query, Class<T> type) {
        Optional<JLSCValue> value = this.get(query);
        return value.isPresent() ? value.get().getAs(type) : Optional.empty();
    }

    public <T> T getAs(JLSCQuery query, Class<T> type, T def) {
        Optional<JLSCValue> value = this.get(query);
        return value.isPresent() ? value.get().getAs(type, def) : def;
    }

    public <T> Optional<T> getAs(String key, Class<T> type) {
        Optional<JLSCValue> value = this.get(key);
        return value.isPresent() ? value.get().getAs(type) : Optional.empty();
    }

    public <T> T getAs(String key, Class<T> type, T def) {
        Optional<JLSCValue> value = this.get(key);
        return value.isPresent() ? value.get().getAs(type, def) : def;
    }

    public <T> T getAsOrNull(String key, Class<T> type) {
        return this.getAs(key, type, null);
    }

    public Optional<Byte> getByte(String key) {
        return this.getAs(key, Byte.class);
    }

    public Byte getByte(String key, Byte def) {
        return this.getAs(key, Byte.class, def);
    }

    public Byte getByteOrNull(String key) {
        return this.getByte(key, null);
    }

    public Optional<Short> getShort(String key) {
        return this.getAs(key, Short.class);
    }

    public Short getShort(String key, Short def) {
        return this.getAs(key, Short.class, def);
    }

    public Short getShortOrNull(String key) {
        return this.getShort(key, null);
    }

    public Optional<Character> getCharacter(String key) {
        return this.getAs(key, Character.class);
    }

    public Character getCharacter(String key, Character def) {
        return this.getAs(key, Character.class, def);
    }

    public Character getCharacterOrNull(String key) {
        return this.getCharacter(key, null);
    }

    public Optional<Integer> getInteger(String key) {
        return this.getAs(key, Integer.class);
    }

    public Integer getInteger(String key, Integer def) {
        return this.getAs(key, Integer.class, def);
    }

    public Integer getIntegerOrNull(String key) {
        return this.getInteger(key, null);
    }

    public Optional<Long> getLong(String key) {
        return this.getAs(key, Long.class);
    }

    public Long getLong(String key, Long def) {
        return this.getAs(key, Long.class, def);
    }

    public Long getLongOrNull(String key) {
        return this.getLong(key, null);
    }

    public Optional<Float> getFloat(String key) {
        return this.getAs(key, Float.class);
    }

    public Float getFloat(String key, Float def) {
        return this.getAs(key, Float.class, def);
    }

    public Float getFloatOrNull(String key) {
        return this.getFloat(key, null);
    }

    public Optional<Double> getDouble(String key) {
        return this.getAs(key, Double.class);
    }

    public Double getDouble(String key, Double def) {
        return this.getAs(key, Double.class, def);
    }

    public Double getDoubleOrNull(String key) {
        return this.getDouble(key, null);
    }

    public Optional<Boolean> getBoolean(String key) {
        return this.getAs(key, Boolean.class);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return this.getAs(key, Boolean.class, def);
    }

    public Boolean getBooleanOrNull(String key) {
        return this.getBoolean(key, null);
    }

    public Optional<String> getString(String key) {
        return this.getAs(key, String.class);
    }

    public String getString(String key, String def) {
        return this.getAs(key, String.class, def);
    }

    public String getStringOrNull(String key) {
        return this.getString(key, null);
    }

    public Optional<JLSCDataHolder> getDataHolder(String key) {
        return this.getAs(key, JLSCDataHolder.class);
    }

    public JLSCDataHolder getDataHolder(String key, JLSCDataHolder def) {
        return this.getAs(key, JLSCDataHolder.class, def);
    }

    public JLSCDataHolder getDataHolderOrNull(String key) {
        return this.getDataHolder(key, null);
    }

    public Optional<JLSCArray> getArray(String key) {
        return this.getAs(key, JLSCArray.class);
    }

    public JLSCArray getArray(String key, JLSCArray def) {
        return this.getAs(key, JLSCArray.class, def);
    }

    public JLSCArray getArrayOrNull(String key) {
        return this.getArray(key, null);
    }

    public Optional<JLSCCompound> getCompound(String key) {
        return this.getAs(key, JLSCCompound.class);
    }

    public JLSCCompound getCompound(String key, JLSCCompound def) {
        return this.getAs(key, JLSCCompound.class, def);
    }

    public JLSCCompound getCompoundOrNull(String key) {
        return this.getCompound(key, null);
    }

    public Optional<Byte> getByte(JLSCQuery query) {
        return this.getAs(query, Byte.class);
    }

    public Byte getByte(JLSCQuery query, Byte def) {
        return this.getAs(query, Byte.class, def);
    }

    public Byte getByteOrNull(JLSCQuery query) {
        return this.getByte(query, null);
    }

    public Optional<Short> getShort(JLSCQuery query) {
        return this.getAs(query, Short.class);
    }

    public Short getShort(JLSCQuery query, Short def) {
        return this.getAs(query, Short.class, def);
    }

    public Short getShortOrNull(JLSCQuery query) {
        return this.getShort(query, null);
    }

    public Optional<Character> getCharacter(JLSCQuery query) {
        return this.getAs(query, Character.class);
    }

    public Character getCharacter(JLSCQuery query, Character def) {
        return this.getAs(query, Character.class, def);
    }

    public Character getCharacterOrNull(JLSCQuery query) {
        return this.getCharacter(query, null);
    }

    public Optional<Integer> getInteger(JLSCQuery query) {
        return this.getAs(query, Integer.class);
    }

    public Integer getInteger(JLSCQuery query, Integer def) {
        return this.getAs(query, Integer.class, def);
    }

    public Integer getIntegerOrNull(JLSCQuery query) {
        return this.getInteger(query, null);
    }

    public Optional<Long> getLong(JLSCQuery query) {
        return this.getAs(query, Long.class);
    }

    public Long getLong(JLSCQuery query, Long def) {
        return this.getAs(query, Long.class, def);
    }

    public Long getLongOrNull(JLSCQuery query) {
        return this.getLong(query, null);
    }

    public Optional<Float> getFloat(JLSCQuery query) {
        return this.getAs(query, Float.class);
    }

    public Float getFloat(JLSCQuery query, Float def) {
        return this.getAs(query, Float.class, def);
    }

    public Float getFloatOrNull(JLSCQuery query) {
        return this.getFloat(query, null);
    }

    public Optional<Double> getDouble(JLSCQuery query) {
        return this.getAs(query, Double.class);
    }

    public Double getDouble(JLSCQuery query, Double def) {
        return this.getAs(query, Double.class, def);
    }

    public Double getDoubleOrNull(JLSCQuery query) {
        return this.getDouble(query, null);
    }

    public Optional<Boolean> getBoolean(JLSCQuery query) {
        return this.getAs(query, Boolean.class);
    }

    public Boolean getBoolean(JLSCQuery query, Boolean def) {
        return this.getAs(query, Boolean.class, def);
    }

    public Boolean getBooleanOrNull(JLSCQuery query) {
        return this.getBoolean(query, null);
    }

    public Optional<String> getString(JLSCQuery query) {
        return this.getAs(query, String.class);
    }

    public String getString(JLSCQuery query, String def) {
        return this.getAs(query, String.class, def);
    }

    public String getStringOrNull(JLSCQuery query) {
        return this.getString(query, null);
    }

    public Optional<JLSCDataHolder> getDataHolder(JLSCQuery query) {
        return this.getAs(query, JLSCDataHolder.class);
    }

    public JLSCDataHolder getDataHolder(JLSCQuery query, JLSCDataHolder def) {
        return this.getAs(query, JLSCDataHolder.class, def);
    }

    public JLSCDataHolder getDataHolderOrNull(JLSCQuery query) {
        return this.getDataHolder(query, null);
    }

    public Optional<JLSCArray> getArray(JLSCQuery query) {
        return this.getAs(query, JLSCArray.class);
    }

    public JLSCArray getArray(JLSCQuery query, JLSCArray def) {
        return this.getAs(query, JLSCArray.class, def);
    }

    public JLSCArray getArrayOrNull(JLSCQuery query) {
        return this.getArray(query, null);
    }

    public Optional<JLSCCompound> getCompound(JLSCQuery query) {
        return this.getAs(query, JLSCCompound.class);
    }

    public JLSCCompound getCompound(JLSCQuery query, JLSCCompound def) {
        return this.getAs(query, JLSCCompound.class, def);
    }

    public JLSCCompound getCompoundOrNull(JLSCQuery query) {
        return this.getCompound(query, null);
    }

    @Override
    public Optional<JLSCValue> get(Stack<Switch<String, Integer>> pathStack) {
        if (!pathStack.isEmpty()) {
            Switch<String, Integer> top = pathStack.pop();
            if (top.containsA()) {
                String key = top.getA().get();
                Optional<JLSCValue> valueOptional = this.get(key);
                if (valueOptional.isPresent()) {
                    JLSCValue value = valueOptional.get();
                    if (pathStack.isEmpty()) {
                        return Optional.of(value);
                    } else if (value.directCast(JLSCDataHolder.class).isPresent()) {
                        return value.directCast(JLSCDataHolder.class).get().get(pathStack);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void put(Stack<Switch<String, Integer>> pathStack, JLSCValue value) {
        if (!pathStack.isEmpty()) {
            Switch<String, Integer> top = pathStack.pop();
            if (top.containsA()) {
                String key = top.getA().get();
                if (pathStack.isEmpty()) {
                    this.put(key, value);
                } else {
                    Switch<String, Integer> next = pathStack.peek();
                    JLSCValue val = this.get(key).orElse(null);
                    if (val != null && ((val.getAsCompound().isPresent() && next.containsA()) || (val.getAsArray().isPresent() && next.containsB()))) {
                        if (next.containsA()) {
                            val.getAsCompound().get().put(pathStack, value);
                        } else if (next.containsB()) {
                            val.getAsArray().get().put(pathStack, value);
                        }
                    } else {
                        this.put(key, next.containsA() ? new JLSCCompound() : new JLSCArray());
                        JLSCValue subVal = this.values.get(key).getValue();
                        if ((subVal.getAsCompound().isPresent() && next.containsA()) || (subVal.getAsArray().isPresent() && next.containsB())) {
                            if (next.containsA()) {
                                subVal.getAsCompound().get().put(pathStack, value);
                            } else if (next.containsB()) {
                                subVal.getAsArray().get().put(pathStack, value);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<JLSCQuery> paths(boolean deep) {
        if (!deep) {
            return this.values.keySet().stream().map(JLSCQuery::of).collect(Collectors.toList());
        } else {
            List<JLSCQuery> queries = new ArrayList<>();
            for (JLSCKeyValue keyValue : this.values.values()) {
                JLSCValue value = keyValue.getValue();
                if (value.directCast(JLSCDataHolder.class).isPresent()) {
                    queries.addAll(value.directCast(JLSCDataHolder.class).get().paths(true).stream().map(q -> q.prepend(keyValue.getKey())).collect(Collectors.toList()));
                } else {
                    queries.add(JLSCQuery.of(keyValue.getKey()));
                }
            }

            return queries;
        }
    }

    @Override
    public List<JLSCValue> leaves(boolean deep) {
        if (!deep) {
            return this.values.values().stream().map(JLSCKeyValue::getValue).collect(Collectors.toList());
        } else {
            List<JLSCValue> values = new ArrayList<>();
            for (JLSCKeyValue keyValue : this.values.values()) {
                JLSCValue value = keyValue.getValue();
                if (value.directCast(JLSCDataHolder.class).isPresent()) {
                    values.addAll(value.directCast(JLSCDataHolder.class).get().leaves(true));
                } else {
                    values.add(value);
                }
            }
            return values;
        }
    }

    public void put(JLSCKeyValue keyValue) {
        this.values.put(keyValue.getKey(), keyValue);
    }

    @Override
    public Iterator<JLSCKeyValue> iterator() {
        return this.entries().iterator();
    }

}
