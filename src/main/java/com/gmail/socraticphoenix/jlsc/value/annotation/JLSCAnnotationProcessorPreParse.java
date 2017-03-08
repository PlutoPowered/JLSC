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

import com.gmail.socraticphoenix.jlsc.registry.JLSCPreParse;
import com.gmail.socraticphoenix.jlsc.registry.JLSCRegistry;
import com.gmail.socraticphoenix.mirror.Reflections;

import java.util.Optional;

public class JLSCAnnotationProcessorPreParse implements JLSCPreParse {

    @Override
    public boolean affects(String val) {
        Optional<Class> classOptional = this.isolate(val);
        return classOptional.isPresent() && classOptional.get().isAnnotationPresent(Convertible.class);
    }

    @Override
    public void apply(String val) {
        JLSCRegistry.register(JLSCAnnotationProcessorGenerator.generate(this.isolate(val).get()));
    }

    private Optional<Class> isolate(String val) {
        StringBuilder builder = new StringBuilder();
        boolean dot = false;
        for(char c : val.toCharArray()) {
            if(c == '(') {
                break;
            } else {
                if(c == '.') {
                    if(dot) {
                        return Optional.empty();
                    } else {
                        dot = true;
                    }
                } else if (!Character.isJavaIdentifierPart(c) && c != ' ') {
                    return Optional.empty();
                }
                builder.append(c);
            }
        }

        return Reflections.resolveClass(builder.toString().trim());
    }

}
