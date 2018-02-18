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
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCSkeleton;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCVerifier;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCVerifiers;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;

import java.util.Map;
import java.util.function.Supplier;

public class JLSCMapSerializer implements JLSCSerializer<Map> {
    private Supplier<Map> mapSupplier;
    private Class<? extends Map> type;
    private boolean strict;
    private String mapId;
    private Class key;
    private Class value;

    private JLSCVerifier verifier;

    public JLSCMapSerializer(Class<? extends Map> type, Class key, Class value, boolean strict, Supplier<Map> mapSupplier, String mapId) {
        this.mapSupplier = mapSupplier;
        this.type = type;
        this.strict = strict;
        this.mapId = mapId;
        this.key = key;
        this.value = value;

        this.verifier = JLSCVerifiers.and(
                JLSCVerifiers.type(JLSCCompound.class),
                JLSCVerifiers.or(
                        JLSCSkeleton.builder()
                                .require("mapId", JLSCVerifiers.is(mapId))
                                .require("useStringKeys", JLSCVerifiers.is(true))
                                .require("map", JLSCVerifiers.compound(JLSCVerifiers.type(value)))
                                .build(),
                        JLSCSkeleton.builder()
                                .require("mapId", JLSCVerifiers.is(mapId))
                                .require("useStringKeys", JLSCVerifiers.is(false))
                                .require("map", JLSCVerifiers.array(JLSCVerifiers.and(JLSCVerifiers.type(JLSCCompound.class),
                                        JLSCSkeleton.builder()
                                                .require("key", JLSCVerifiers.type(key))
                                                .require("value", JLSCVerifiers.type(value))
                                                .build()))
                                )
                                .build()
                )
        );
    }

    @Override
    public Class<Map> result() {
        return (Class<Map>) this.type;
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
        Map map = value.getAs(Map.class).get();
        boolean useStringKeys = map.keySet().stream().allMatch(String.class::isInstance);
        JLSCCompound upper = new JLSCCompound();
        upper.put("mapId", this.mapId);
        upper.put("useStringKeys", useStringKeys);
        if (useStringKeys) {
            JLSCCompound compound = new JLSCCompound();
            map.forEach((k, v) -> compound.put(String.valueOf(k), v));
            upper.put("map", compound);
        } else {
            JLSCArray array = new JLSCArray();
            map.forEach((k, v) -> {
                JLSCCompound entry = new JLSCCompound();
                entry.put("key", k);
                entry.put("value", v);
                array.add(entry);
            });
            upper.put("map", array);
        }
        return JLSCValue.of(upper);
    }

    @Override
    public JLSCValue deSerialize(JLSCValue value) throws JLSCException {
        Map res = this.mapSupplier.get();
        JLSCCompound upper = value.getAsCompound().get();
        boolean useStringKeys = upper.getBoolean("useStringKeys", false);
        if (useStringKeys) {
            JLSCCompound compound = upper.getCompound("map").get();
            compound.forEachValue(res::put);
        } else {
            JLSCArray entries = upper.getArray("map").get();
            entries.forEach(val -> {
                JLSCCompound entry = val.getAsCompound().get();
                res.put(entry.get("key").get().getAs(this.key).get(), entry.get("value").get().getAs(this.value).get());
            });
        }

        return JLSCValue.of(res);
    }

}
