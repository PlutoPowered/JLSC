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
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.pio.ByteStream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JLSCArray implements JLSCDataHolder, Iterable<JLSCValue> {
    private List<JLSCValue> values;

    private JLSCArray(List<JLSCValue> values) {
        this.values = values;
    }

    public JLSCArray() {
        this(new ArrayList<>());
    }

    public JLSCArray(Supplier<List<JLSCValue>> constructor) {
        this(constructor.get());
    }

    public static JLSCArray concurrent() {
        return new JLSCArray(Collections.synchronizedList(new ArrayList<>()));
    }

    public static JLSCArray read(String src, boolean concurrent, JLSCStyle style, JLSCSyntax syntax) throws JLSCException {
        return JLSCReadWriteUtil.readArray(src, syntax, style, concurrent ? () -> Collections.synchronizedMap(new LinkedHashMap<>()) : LinkedHashMap::new, concurrent ? () -> Collections.synchronizedList(new ArrayList<>()) : ArrayList::new);
    }

    public static JLSCArray read(String src, boolean concurrent) throws JLSCException {
        return JLSCArray.read(src, concurrent, JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT);
    }

    public static JLSCArray read(String src) throws JLSCException {
        return JLSCArray.read(src, true);
    }

    public static JLSCArray of(Object... elements) {
        return new JLSCArray(Items.buildList(elements).stream().map(JLSCValue::new).collect(Collectors.toList()));
    }

    public <T> Optional<T> getAs(JLSCQuery query, Class<T> type) {
        Optional<JLSCValue> value = this.get(query);
        return value.isPresent() ? value.get().convert(type) : Optional.empty();
    }

    public <T> T getAs(JLSCQuery query, Class<T> type, T def) {
        Optional<JLSCValue> value = this.get(query);
        return value.isPresent() ? value.get().convert(type, def) : def;
    }

    public <T> Optional<T> getAs(int index, Class<T> type) {
        Optional<JLSCValue> value = this.get(index);
        return value.isPresent() ? value.get().convert(type) : Optional.empty();
    }

    public <T> T getAs(int index, Class<T> type, T def) {
        Optional<JLSCValue> value = this.get(index);
        return value.isPresent() ? value.get().convert(type, def) : def;
    }

    public <T> T getAsOrNull(int index, Class<T> type) {
        return this.getAs(index, type, null);
    }

    public Optional<Byte> getByte(int index) {
        return this.getAs(index, Byte.class);
    }

    public Byte getByte(int index, Byte def) {
        return this.getAs(index, Byte.class, def);
    }

    public Byte getByteOrNull(int index) {
        return this.getByte(index, null);
    }

    public Optional<Short> getShort(int index) {
        return this.getAs(index, Short.class);
    }

    public Short getShort(int index, Short def) {
        return this.getAs(index, Short.class, def);
    }

    public Short getShortOrNull(int index) {
        return this.getShort(index, null);
    }

    public Optional<Character> getCharacter(int index) {
        return this.getAs(index, Character.class);
    }

    public Character getCharacter(int index, Character def) {
        return this.getAs(index, Character.class, def);
    }

    public Character getCharacterOrNull(int index) {
        return this.getCharacter(index, null);
    }

    public Optional<Integer> getInteger(int index) {
        return this.getAs(index, Integer.class);
    }

    public Integer getInteger(int index, Integer def) {
        return this.getAs(index, Integer.class, def);
    }

    public Integer getIntegerOrNull(int index) {
        return this.getInteger(index, null);
    }

    public Optional<Long> getLong(int index) {
        return this.getAs(index, Long.class);
    }

    public Long getLong(int index, Long def) {
        return this.getAs(index, Long.class, def);
    }

    public Long getLongOrNull(int index) {
        return this.getLong(index, null);
    }

    public Optional<Float> getFloat(int index) {
        return this.getAs(index, Float.class);
    }

    public Float getFloat(int index, Float def) {
        return this.getAs(index, Float.class, def);
    }

    public Float getFloatOrNull(int index) {
        return this.getFloat(index, null);
    }

    public Optional<Double> getDouble(int index) {
        return this.getAs(index, Double.class);
    }

    public Double getDouble(int index, Double def) {
        return this.getAs(index, Double.class, def);
    }

    public Double getDoubleOrNull(int index) {
        return this.getDouble(index, null);
    }

    public Optional<Boolean> getBoolean(int index) {
        return this.getAs(index, Boolean.class);
    }

    public Boolean getBoolean(int index, Boolean def) {
        return this.getAs(index, Boolean.class, def);
    }

    public Boolean getBooleanOrNull(int index) {
        return this.getBoolean(index, null);
    }

    public Optional<String> getString(int index) {
        return this.getAs(index, String.class);
    }

    public String getString(int index, String def) {
        return this.getAs(index, String.class, def);
    }

    public String getStringOrNull(int index) {
        return this.getString(index, null);
    }

    public Optional<JLSCDataHolder> getDataHolder(int index) {
        return this.getAs(index, JLSCDataHolder.class);
    }

    public JLSCDataHolder getDataHolder(int index, JLSCDataHolder def) {
        return this.getAs(index, JLSCDataHolder.class, def);
    }

    public JLSCDataHolder getDataHolderOrNull(int index) {
        return this.getDataHolder(index, null);
    }

    public Optional<JLSCArray> getArray(int index) {
        return this.getAs(index, JLSCArray.class);
    }

    public JLSCArray getArray(int index, JLSCArray def) {
        return this.getAs(index, JLSCArray.class, def);
    }

    public JLSCArray getArrayOrNull(int index) {
        return this.getArray(index, null);
    }

    public Optional<JLSCCompound> getCompound(int index) {
        return this.getAs(index, JLSCCompound.class);
    }

    public JLSCCompound getCompound(int index, JLSCCompound def) {
        return this.getAs(index, JLSCCompound.class, def);
    }

    public JLSCCompound getCompoundOrNull(int index) {
        return this.getCompound(index, null);
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

    public void absorbMetadata(JLSCArray other) {
        for (int i = 0; i < Math.min(this.size(), other.size()); i++) {
            this.get(i).get().absorbMetadata(other.get(i).get());
        }
    }

    public byte[] writeBytes() throws JLSCException {
        int size = JLSCReadWriteUtil.length(this);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        JLSCReadWriteUtil.write(this, ByteStream.of(buffer));
        return buffer.array();
    }

    public String write(JLSCStyle style, JLSCSyntax syntax) throws JLSCException {
        return JLSCReadWriteUtil.writeArray(this, syntax, style, 0);
    }

    public String write() throws JLSCException {
        return this.write(JLSCStyle.DEFAULT, JLSCSyntax.DEFAULT);
    }

    public JLSCArray toConcurrent() {
        JLSCArray array = JLSCArray.concurrent();
        array.values.addAll(this.values);
        return array;
    }

    public boolean addProperties(int key, List<JLSCValueProperty> properties) {
        Optional<JLSCValue> valueOptional = this.get(key);
        if (valueOptional.isPresent()) {
            valueOptional.get().getProperties().addAll(properties);
            return true;
        }
        return false;
    }

    public boolean addProperties(int key, String... properties) {
        return this.addProperties(key, Stream.of(properties).map(JLSCValueProperty::new).collect(Collectors.toList()));
    }

    public boolean removeProperties(int key, Predicate<JLSCValueProperty> remove) {
        Optional<JLSCValue> valueOptional = this.get(key);
        if (valueOptional.isPresent()) {
            valueOptional.get().getProperties().removeIf(remove);
            return true;
        }
        return false;
    }

    public boolean removeProperties(int key, String... remove) {
        return this.removeProperties(key, p -> Items.contains(p.getName(), remove));
    }

    public int size() {
        return this.values.size();
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public boolean contains(Object o) {
        return this.values.contains(o) || (o instanceof JLSCValue && this.values.contains(((JLSCValue) o).rawValue()));
    }

    public Iterator<JLSCValue> iterator() {
        return this.values.iterator();
    }

    public JLSCValue[] toArray() {
        return this.values.toArray(new JLSCValue[this.size()]);
    }

    public boolean add(Object value) {
        return this.add(JLSCValue.of(value));
    }

    public boolean add(JLSCValue value) {
        return this.values.add(value);
    }

    public boolean remove(Object value) {
        return this.remove(JLSCValue.of(value));
    }

    public boolean remove(JLSCValue value) {
        return this.values.remove(value);
    }

    public boolean containsAllValues(Collection<?> c) {
        return this.containsAll(c.stream().map(JLSCValue::of).collect(Collectors.toList()));
    }

    public boolean containsAll(Collection<JLSCValue> c) {
        return this.values.containsAll(c);
    }

    public boolean addAllValues(Collection<?> c) {
        return this.addAll(c.stream().map(JLSCValue::of).collect(Collectors.toList()));
    }

    public boolean addAll(Collection<? extends JLSCValue> c) {
        return this.values.addAll(c);
    }

    public boolean addAllValues(int index, Collection<?> c) {
        return this.addAll(index, c.stream().map(JLSCValue::of).collect(Collectors.toList()));
    }

    public boolean addAll(int index, Collection<? extends JLSCValue> c) {
        return this.values.addAll(index, c);
    }

    public boolean removeAllValues(Collection<?> c) {
        return this.values.removeIf(v -> c.contains(v.rawValue()));
    }

    public boolean removeAll(Collection<? extends JLSCValue> c) {
        return this.values.removeIf(c::contains);
    }

    public boolean retainAllValues(Collection<?> c) {
        return this.values.removeIf(v -> !c.contains(v.rawValue()));
    }

    public boolean retainAll(Collection<? extends JLSCValue> c) {
        return this.values.removeIf(v -> !c.contains(v));
    }

    public void clear() {
        this.values.clear();
    }

    public Optional<JLSCValue> get(int index) {
        return this.contains(index) ? Optional.of(this.values.get(index)) : Optional.empty();
    }

    public boolean contains(int index) {
        return index >= 0 && index < this.size();
    }

    public Optional<JLSCValue> set(int index, JLSCValue element) {
        if (index >= this.values.size()) {
            int times = index - this.values.size() + 1;
            for (int i = 0; i < times; i++) {
                this.add(JLSCValue.of(null));
            }
        }
        return Optional.ofNullable(this.values.set(index, element));
    }

    public void add(int index, JLSCValue element) {
        this.values.add(index, element);
    }

    public Optional<JLSCValue> remove(int index) {
        return Optional.ofNullable(this.values.remove(index));
    }

    public int indexOf(Object value) {
        return this.values.indexOf(JLSCValue.of(value));
    }

    public int indexOf(JLSCValue value) {
        return this.values.indexOf(value);
    }

    public int lastIndexOf(Object value) {
        return this.lastIndexOf(JLSCValue.of(value));
    }

    public int lastIndexOf(JLSCValue value) {
        return this.values.lastIndexOf(value);
    }

    @Override
    public Optional<JLSCValue> get(Stack<Switch<String, Integer>> pathStack) {
        if (!pathStack.isEmpty()) {
            Switch<String, Integer> top = pathStack.pop();
            if (top.containsB()) {
                int index = top.getB().get();
                Optional<JLSCValue> valueOptional = this.get(index);
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
            if (top.containsB()) {
                int key = top.getB().get();
                if (pathStack.isEmpty()) {
                    this.set(key, value);
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
                        this.set(key, JLSCValue.of(next.containsA() ? new JLSCCompound() : new JLSCArray()));
                        JLSCValue subVal = this.values.get(key);
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
            return IntStream.range(0, this.size()).mapToObj(JLSCQuery::of).collect(Collectors.toList());
        } else {
            List<JLSCQuery> queries = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                JLSCValue value = this.get(i).get();
                if (value.directCast(JLSCDataHolder.class).isPresent()) {
                    int finalI = i;
                    queries.addAll(value.directCast(JLSCDataHolder.class).get().paths(true).stream().map(q -> q.prepend(finalI)).collect(Collectors.toList()));
                } else {
                    queries.add(JLSCQuery.of(i));
                }
            }
            return queries;
        }
    }

    @Override
    public List<JLSCValue> leaves(boolean deep) {
        if (!deep) {
            return Items.looseClone(this.values);
        } else {
            List<JLSCValue> values = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                JLSCValue value = this.get(i).get();
                if (value.directCast(JLSCDataHolder.class).isPresent()) {
                    values.addAll(value.directCast(JLSCDataHolder.class).get().leaves(true));
                } else {
                    values.add(value);
                }
            }
            return values;
        }
    }
}
