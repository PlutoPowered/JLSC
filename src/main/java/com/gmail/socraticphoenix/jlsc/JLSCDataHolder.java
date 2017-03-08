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
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;

public interface JLSCDataHolder {

    default boolean contains(JLSCQuery query) {
        return this.get(query).isPresent();
    }

    default void put(JLSCQuery query, JLSCValue value) {
        this.put(query.makeStack(), value);
    }

    default Optional<JLSCValue> get(JLSCQuery query) {
        return this.get(query.makeStack());
    }

    default Optional<JLSCValue> get(Object... path) {
        return this.get(JLSCQuery.of(path));
    }

    default void traverse(Consumer<JLSCValue> consumer, boolean deep) {
        this.leaves(deep).forEach(consumer);
    }

    Optional<JLSCValue> get(Stack<Switch<String, Integer>> pathStack);

    void put(Stack<Switch<String, Integer>> pathStack, JLSCValue value);

    List<JLSCQuery> paths(boolean deep);

    List<JLSCValue> leaves(boolean deep);

}
