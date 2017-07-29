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
package com.gmail.socraticphoenix.jlsc.value.annotation;

import com.gmail.socraticphoenix.collect.coupling.Pair;
import com.gmail.socraticphoenix.collect.coupling.Switch;
import com.gmail.socraticphoenix.inversey.many.DangerousConsumer2;
import com.gmail.socraticphoenix.inversey.many.DangerousFunction1;
import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.metadata.Property;
import com.gmail.socraticphoenix.jlsc.serialization.annotation.JLSCAnnotationSerializerGenerator;
import com.gmail.socraticphoenix.jlsc.value.JLSCDualProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.pio.Bytes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JLSCAnnotationProcessorGenerator {
    public static boolean validate = true;

    public static <T> JLSCDualProcessor generate(Class<T> type, boolean strict, String name) {
        if(JLSCAnnotationProcessorGenerator.validate) {
            JLSCAnnotationSerializerGenerator.validate(type);
        }
        return new JLSCAnnotationProcessor<>(type, name, strict, JLSCAnnotationProcessorGenerator.write(type), JLSCAnnotationProcessorGenerator.read(type));
    }

    public static <T> JLSCDualProcessor generate(Class<T> type) {
        return JLSCAnnotationProcessorGenerator.generate(type, false, type.getName());
    }

    public static <T> List<DangerousConsumer2<T, JLSCArray, JLSCException>> write(Class<T> type) {
        List<DangerousConsumer2<T, JLSCArray, JLSCException>> list = new ArrayList<>();
        List<Switch<Method, Field>> pieces = new ArrayList<>();
        for(Field field : type.getDeclaredFields()) {
            if(field.isAnnotationPresent(Convert.class) && !Modifier.isStatic(field.getModifiers())) {
                Convert convert = field.getAnnotation(Convert.class);
                if(pieces.isEmpty()) {
                    pieces.add(Switch.ofB(field));
                } else {
                    boolean added = false;
                    for (int i = 0; i < pieces.size(); i++) {
                        Switch<Method, Field> methodFieldSwitch = pieces.get(i);
                        int ind = methodFieldSwitch.containsA() ? methodFieldSwitch.getA().get().getAnnotation(Convert.class).value() : methodFieldSwitch.getB().get().getAnnotation(Convert.class).value();
                        if(ind > convert.value()) {
                            pieces.add(i, Switch.ofB(field));
                            added = true;
                            break;
                        }
                    }
                    if(!added) {
                        pieces.add(Switch.ofB(field));
                    }
                }
            }
        }

        for(Method method : type.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Convert.class) && !Modifier.isStatic(method.getModifiers())) {
                Convert convert = method.getAnnotation(Convert.class);
                if(pieces.isEmpty()) {
                    pieces.add(Switch.ofA(method));
                } else {
                    boolean added = false;
                    for (int i = 0; i < pieces.size(); i++) {
                        Switch<Method, Field> methodFieldSwitch = pieces.get(i);
                        int ind = methodFieldSwitch.containsA() ? methodFieldSwitch.getA().get().getAnnotation(Convert.class).value() : methodFieldSwitch.getB().get().getAnnotation(Convert.class).value();
                        if(ind > convert.value()) {
                            pieces.add(i, Switch.ofA(method));
                            added = true;
                            break;
                        }
                    }
                    if(!added) {
                        pieces.add(Switch.ofA(method));
                    }
                }
            }
        }

        for (int i = 0; i < pieces.size(); i++) {
            Switch<Method, Field> piece = pieces.get(i);
            int finalI = i;
            if(piece.containsA()) {
                Method method = piece.getA().get();
                Convert convert = method.getAnnotation(Convert.class);
                List<JLSCValueProperty> properties = new ArrayList<>();
                Property[] propertyTemplates = convert.properties();
                for (Property propertyTemplate : propertyTemplates) {
                    JLSCValueProperty property = new JLSCValueProperty(propertyTemplate.name());
                    for (String s : propertyTemplate.arguments()) {
                        property.getArguments().add(s);
                    }
                    properties.add(property);
                }
                list.add((t, a) -> {
                    boolean access = method.isAccessible();
                    try {
                        method.setAccessible(true);
                        a.add(method.invoke(t));
                        method.setAccessible(access);
                        a.addProperties(finalI, properties);
                    } catch (IllegalAccessException e) {
                        method.setAccessible(access);
                        throw new IllegalStateException("Unreachable code", e);
                    } catch (InvocationTargetException e) {
                        method.setAccessible(access);
                        throw new JLSCException("Error while invoking method of class " + type.getName(), e);
                    }
                });
            } else if (piece.containsB()) {
                Field field = piece.getB().get();
                Convert convert = field.getAnnotation(Convert.class);
                List<JLSCValueProperty> properties = new ArrayList<>();
                Property[] propertyTemplates = convert.properties();
                for (Property propertyTemplate : propertyTemplates) {
                    JLSCValueProperty property = new JLSCValueProperty(propertyTemplate.name());
                    for (String s : propertyTemplate.arguments()) {
                        property.getArguments().add(s);
                    }
                    properties.add(property);
                }
                list.add((t, a) -> {
                    boolean access = field.isAccessible();
                    try {
                        field.setAccessible(true);
                        a.add(field.get(t));
                        field.setAccessible(access);
                        a.addProperties(finalI, properties);
                    } catch (IllegalAccessException e) {
                        field.setAccessible(access);
                        throw new IllegalStateException("Unreachable code", e);
                    }
                });
            }
        }

        return list;
    }

    public static <T> DangerousFunction1<JLSCArray, T, JLSCException> read(Class<T> type) {
        Constructor<T> chosen = null;
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameters().length == 0 && chosen == null) {
                chosen = (Constructor<T>) constructor;
            } else if (constructor.isAnnotationPresent(ConversionConstructor.class)) {
                chosen = (Constructor<T>) constructor;
                break;
            }
        }

        if (chosen == null) {
            throw new IllegalArgumentException("No @SerializationConstructor or default constructor found in " + type.getName());
        }

        Class[] types = new Class[chosen.getParameters().length];
        int[] indices = new int[chosen.getParameters().length];

        for (int i = 0; i < chosen.getParameters().length; i++) {
            Parameter parameter = chosen.getParameters()[i];
            types[i] = parameter.getType();
            indices[i] = parameter.getAnnotation(Index.class).value();
        }

        List<Pair<Integer, Field>> reflective = Stream.of(type.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Convert.class) && !Modifier.isStatic(f.getModifiers()) && f.getAnnotation(Convert.class).reflect()).map(f -> Pair.of(f.getAnnotation(Convert.class).value(), f)).collect(Collectors.toList());

        Constructor<T> finalChosen = chosen;
        return a -> {
            Object[] params = new Object[indices.length];
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                params[i] = a.get(index).orElse(JLSCValue.of(null)).getAs(types[i], null);
            }
            boolean access = finalChosen.isAccessible();
            finalChosen.setAccessible(true);

            try {
                T val = finalChosen.newInstance(params);
                finalChosen.setAccessible(access);
                for(Pair<Integer, Field> reflect : reflective) {
                    int key = reflect.getA();
                    Field field = reflect.getB();
                    boolean faccess = field.isAccessible();
                    try {
                        field.setAccessible(true);
                        field.set(val, a.get(key).orElse(JLSCValue.of(null)).getAs(field.getType(), null));
                        field.setAccessible(faccess);
                    } catch (IllegalAccessException e) {
                        field.setAccessible(faccess);
                        throw e;
                    }
                }
                return val;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                finalChosen.setAccessible(access);
                throw new JLSCException("Error while instantiating object of class " + type.getName(), e);
            }
        };
    }

    public static <T> void validate(Class<T> type) {

    }

}
