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
package com.gmail.socraticphoenix.jlsc.serialization.annotation;

import com.gmail.socraticphoenix.collect.Items;
import com.gmail.socraticphoenix.collect.coupling.Pair;
import com.gmail.socraticphoenix.inversey.many.DangerousConsumer2;
import com.gmail.socraticphoenix.inversey.many.DangerousFunction1;
import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.metadata.Property;
import com.gmail.socraticphoenix.jlsc.serialization.JLSCSerializer;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCSkeleton;
import com.gmail.socraticphoenix.jlsc.skeleton.JLSCVerifiers;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JLSCAnnotationSerializerGenerator {
    public static boolean validate = true;

    public static <T> JLSCSerializer<T> generate(Class<T> type, boolean strict) {
        if(JLSCAnnotationSerializerGenerator.validate) {
            JLSCAnnotationSerializerGenerator.validate(type);
        }
        Pair<JLSCSkeleton, List<DangerousConsumer2<T, JLSCCompound, JLSCException>>> info = JLSCAnnotationSerializerGenerator.write(type);
        return new JLSCAnnotationSerializer<>(info.getB(), JLSCAnnotationSerializerGenerator.read(type), info.getA(), type, strict);
    }

    public static <T> JLSCSerializer<T> generate(Class<T> type) {
        return JLSCAnnotationSerializerGenerator.generate(type, false);
    }

    public static <T> Pair<JLSCSkeleton, List<DangerousConsumer2<T, JLSCCompound, JLSCException>>> write(Class<T> type) {
        List<DangerousConsumer2<T, JLSCCompound, JLSCException>> list = new ArrayList<>();
        JLSCSkeleton.Builder builder = JLSCVerifiers.skeleton();

        Stream.of(type.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Serialize.class) && !Modifier.isStatic(f.getModifiers())).forEach(f -> {
            Serialize serialize = f.getAnnotation(Serialize.class);
            String name = serialize.value();
            List<String> comments = Items.buildList(serialize.comments());
            List<JLSCValueProperty> properties = new ArrayList<>();
            Property[] propertyTemplates = serialize.properties();
            for (Property propertyTemplate : propertyTemplates) {
                JLSCValueProperty property = new JLSCValueProperty(propertyTemplate.name());
                for (String s : propertyTemplate.arguments()) {
                    property.getArguments().add(s);
                }
                properties.add(property);
            }
            list.add((t, j) -> {
                try {
                    boolean access = f.isAccessible();
                    f.setAccessible(true);
                    Object value = f.get(t);
                    f.setAccessible(access);
                    JLSCKeyValue keyValue = new JLSCKeyValue(name, JLSCValue.of(value));
                    keyValue.getProperties().addAll(properties);
                    keyValue.getComments().addAll(comments);
                    j.put(keyValue);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unreachable code", e);
                }
            });
            builder.require(name, JLSCVerifiers.nullOrConvertible(f.getType()));
        });

        Stream.of(type.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Serialize.class) && !Modifier.isStatic(m.getModifiers()) && m.getReturnType() != void.class && m.getReturnType() != Void.class && m.getParameters().length == 0).forEach(m -> {
            Serialize serialize = m.getAnnotation(Serialize.class);
            String name = serialize.value();
            List<String> comments = Items.buildList(serialize.comments());
            List<JLSCValueProperty> properties = new ArrayList<>();
            Property[] propertyTemplates = serialize.properties();
            for (Property propertyTemplate : propertyTemplates) {
                JLSCValueProperty property = new JLSCValueProperty(propertyTemplate.name());
                for (String s : propertyTemplate.arguments()) {
                    property.getArguments().add(s);
                }
                properties.add(property);
            }
            list.add((t, j) -> {
                try {
                    boolean access = m.isAccessible();
                    m.setAccessible(true);
                    Object value = m.invoke(t);
                    m.setAccessible(access);
                    JLSCKeyValue keyValue = new JLSCKeyValue(name, JLSCValue.of(value));
                    keyValue.getProperties().addAll(properties);
                    keyValue.getComments().addAll(comments);
                    j.put(keyValue);
                } catch (InvocationTargetException e) {
                    throw new JLSCException("Error while invoking method of class " + type.getName(), e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unreachable code", e);
                }
            });
            builder.require(name, JLSCVerifiers.nullOrConvertible(m.getReturnType()));
        });


        return Pair.of(builder.build(), list);
    }

    public static <T> DangerousFunction1<JLSCCompound, T, JLSCException> read(Class<T> type) {
        Constructor<T> chosen = null;
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameters().length == 0 && chosen == null) {
                chosen = (Constructor<T>) constructor;
            } else if (constructor.isAnnotationPresent(SerializationConstructor.class)) {
                chosen = (Constructor<T>) constructor;
                break;
            }
        }

        if (chosen == null) {
            throw new IllegalArgumentException("No @SerializationConstructor or default constructor found in " + type.getName());
        }

        String[] names = new String[chosen.getParameters().length];
        Class[] types = new Class[chosen.getParameters().length];
        for (int i = 0; i < chosen.getParameters().length; i++) {
            Parameter parameter = chosen.getParameters()[i];
            names[i] = parameter.getAnnotation(Name.class).value();
            types[i] = parameter.getType();
        }

        List<Pair<String, Field>> reflective = Stream.of(type.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Serialize.class) && !Modifier.isStatic(f.getModifiers()) && f.getAnnotation(Serialize.class).reflect()).map(f -> Pair.of(f.getAnnotation(Serialize.class).value(), f)).collect(Collectors.toList());
        Constructor<T> finalChosen = chosen;
        return j -> {
            Object[] params = new Object[names.length];
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                Optional<JLSCValue> value = j.get(name);
                if(value.isPresent()) {
                    params[i] = value.get().convert(types[i], null);
                }
            }

            boolean access = finalChosen.isAccessible();
            finalChosen.setAccessible(access);
            try {
                T val = finalChosen.newInstance(params);
                finalChosen.setAccessible(access);
                for(Pair<String, Field> reflect : reflective) {
                    String key = reflect.getA();
                    Field field = reflect.getB();
                    boolean faccess = field.isAccessible();
                    try {
                        field.setAccessible(true);
                        Optional<JLSCValue> value = j.get(key);
                        if(value.isPresent()) {
                            field.set(val, value.get().convert(field.getType(), null));
                        }
                        field.setAccessible(faccess);
                    } catch (IllegalAccessException e) {
                        field.setAccessible(faccess);
                        throw e;
                    }
                }
                return val;
            } catch (InvocationTargetException e) {
                finalChosen.setAccessible(access);
                throw new JLSCException("Error while instantiating object of class " + type.getName(), e);
            } catch (InstantiationException | IllegalAccessException ignore) {

            }
            finalChosen.setAccessible(access);
            return null;
        };
    }

    public static <T> void validate(Class<T> type) {

    }

}
