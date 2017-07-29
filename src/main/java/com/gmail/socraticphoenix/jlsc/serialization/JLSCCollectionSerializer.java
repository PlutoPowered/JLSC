/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 socraticphoenix@gmail.com
 * Copyright (c) 2017 contributors
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
package com.gmail.socraticphoenix.jlsc.serialization;

import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCVerifier;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCVerifiers;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class JLSCCollectionSerializer implements JLSCSerializer<Collection> {
    private Supplier<Collection> listSupplier;
    private Class<? extends Collection> type;
    private boolean strict;
    private String listId;

    private JLSCVerifier verifier;
    private Class element;

    public JLSCCollectionSerializer(Class<? extends Collection> type, Class element, boolean strict, Supplier<Collection> listSupplier, String listId) {
        this.listSupplier = listSupplier;
        this.type = type;
        this.strict = strict;
        this.listId = listId;
        this.element = element;

        this.verifier = JLSCVerifiers.and(
                JLSCVerifiers.type(JLSCCompound.class),
                JLSCVerifiers.skeleton()
                        .require("listId", JLSCVerifiers.is(listId))
                        .require("list", JLSCVerifiers.array(JLSCVerifiers.type(element)))
                        .build()
        );
    }

    @Override
    public Class<Collection> result() {
        return (Class<Collection>) this.type;
    }

    @Override
    public JLSCVerifier verifier() {
        return this.verifier;
    }

    @Override
    public boolean canSerialize(Object object) {
        return object != null && (this.strict ? this.type.equals(object.getClass()) : this.type.isInstance(object));
    }

    @Override
    public JLSCValue serialize(JLSCValue value) throws JLSCException {
        Collection collection = value.getAs(Collection.class).get();
        JLSCCompound upper = new JLSCCompound();
        upper.put("listId", this.listId);

        JLSCArray array = new JLSCArray();
        array.addAllValues(collection);
        upper.put("list", array);

        return JLSCValue.of(upper);
    }

    @Override
    public JLSCValue deSerialize(JLSCValue value) throws JLSCException {
        JLSCArray array = value.getAsCompound().get().getArray("list").get();
        Collection collection = this.listSupplier.get();
        array.forEach(v -> collection.add(v.getAs(this.element).get()));
        return JLSCValue.of(collection);
    }

}
