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

import com.gmail.socraticphoenix.jlsc.JLSCException;
import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;
import com.gmail.socraticphoenix.jlsc.registry.JLSCRegistry;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.jlsc.value.JLSCProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.parse.CharacterStream;
import com.gmail.socraticphoenix.parse.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class JLSCTokenizer {
    private CharacterStream stream;
    private JLSCStyle style;
    private JLSCSyntax syntax;
    private int i;
    private Supplier<Map<String, JLSCKeyValue>> compoundConstructor;
    private Supplier<List<JLSCValue>> arrayConstructor;

    public JLSCTokenizer(String content, JLSCStyle style, JLSCSyntax syntax, Supplier<Map<String, JLSCKeyValue>> compoundConstructor, Supplier<List<JLSCValue>> arrayConstructor) {
        this.stream = new CharacterStream(content);
        this.i = -1;
        this.style = style;
        this.syntax = syntax;
        this.compoundConstructor = compoundConstructor;
        this.arrayConstructor = arrayConstructor;
    }

    public boolean hasNext() {
        this.stream.consumeAll(this.syntax.ignore());
        return this.stream.hasNext();
    }

    public JLSCValue arrayNext() throws JLSCException {
        i++;
        try {
            return this.nextValue(true);
        } catch (JLSCException e) {
            throw new JLSCException("Failed to read value at index " + i, e);
        }
    }

    public JLSCKeyValue compoundNext() throws JLSCException {
        List<String> comments = new ArrayList<>();
        while (this.stream.isNext(this.syntax.commentBegin())) {
            comments.add(this.nextComment());
        }
        this.stream.consumeAll(this.syntax.ignore());
        String key = this.stream.nextUntil(this.syntax.keyValueDelimiter().or(this.syntax.nonConsumableDelimiter()), Strings.javaEscapeFormat().quote('"')).trim();
        this.stream.consumeAll(this.syntax.keyValueDelimiter().or(this.syntax.ignore()));
        key = key.startsWith("\"") && key.endsWith("\"") ? Strings.deEscape(Strings.cutFirst(Strings.cutLast(key))) : key;
        this.stream.consumeAll(this.syntax.keyValueDelimiter());
        try {
            JLSCValue value = this.nextValue(false);
            JLSCKeyValue keyValue = new JLSCKeyValue(key, value);
            keyValue.getComments().addAll(comments);
            return keyValue;
        } catch (JLSCException e) {
            throw new JLSCException("Failed to read value at key \"" + Strings.escape(key) + "\"", e);
        }
    }

    private JLSCValue nextValue(boolean array) throws JLSCException {
        this.stream.consumeAll(this.syntax.ignore());
        List<JLSCValueProperty> properties = new ArrayList<>();
        while (this.stream.isNext(this.syntax.propertyStart())) {
            properties.add(this.nextProperty());
        }

        JLSCValueProperty typeSpecifier = null;
        for (int i = 0; i < properties.size(); i++) {
            if (JLSCRegistry.getProcessor(properties.get(i).getName()).isPresent()) {
                typeSpecifier = properties.remove(i);
                break;
            }
        }

        JLSCValue jlscValue;
        String value = this.stream.nextUntil(array ? this.syntax.arrayValueEnd() : this.syntax.compoundValueEnd(), this.syntax.data()).trim();
        this.stream.consumeAll((array ? this.syntax.arrayValueEnd() : this.syntax.compoundValueEnd()).or(this.syntax.ignore()));
        if (typeSpecifier != null) {
            JLSCProcessor processor = JLSCRegistry.getProcessor(typeSpecifier.getName()).get();
            if (processor.canRead(value, this.syntax, this.style)) {
                jlscValue = processor.read(value, this.syntax, this.style);
            } else {
                throw new JLSCException("\"" + Strings.escape(value) + "\" cannot be read by processor \"" + typeSpecifier.getName() + "\"");
            }
        } else if (!value.isEmpty() && this.syntax.compoundBegin().test(value.charAt(0)) && this.syntax.compoundEnd().test(value.charAt(value.length() - 1))) {
            jlscValue = JLSCValue.of(JLSCReadWriteUtil.readCompound(value, this.syntax, this.style, this.compoundConstructor, this.arrayConstructor));
        } else if (!value.isEmpty() && this.syntax.arrayBegin().test(value.charAt(0)) && this.syntax.arrayEnd().test(value.charAt(value.length() - 1))) {
            jlscValue = JLSCValue.of(JLSCReadWriteUtil.readArray(value, this.syntax, this.style, this.compoundConstructor, this.arrayConstructor));
        } else {
            Optional<JLSCProcessor> processor = JLSCRegistry.getProcessorFor(value, syntax, style);
            if (processor.isPresent()) {
                jlscValue = processor.get().read(value, this.syntax, this.style);
            } else {
                jlscValue = JLSCRegistry.DEFAULT_PROCESSOR.read(value, this.syntax, this.style);
            }
        }
        jlscValue.getProperties().addAll(properties);
        return jlscValue;
    }

    private JLSCValueProperty nextProperty() {
        this.stream.consumeAll(this.syntax.propertyStart().or(this.syntax.ignore()));

        String name = this.stream.nextUntil(this.syntax.propertyArgsStart().or(this.syntax.propertyEnd()));
        this.stream.consumeAll(this.syntax.ignore());
        JLSCValueProperty property = new JLSCValueProperty(name);
        if (this.stream.isNext(this.syntax.propertyArgsStart())) {
            String arg;
            while (!this.stream.isNext(this.syntax.propertyArgsEnd())) {
                this.stream.consumeAll(this.syntax.ignore());
                arg = this.stream.nextUntil(this.syntax.propertyArgsEnd().or(this.syntax.propertyArgsDelimiter()), this.syntax.data()).trim();
                this.stream.consumeAll(this.syntax.ignore().or(this.syntax.propertyArgsDelimiter()));
                arg = arg.startsWith("\"") && arg.endsWith("\"") ? Strings.deEscape(Strings.cutFirst(Strings.cutLast(arg))) : arg;
                property.getArguments().add(arg);
            }
            this.stream.consumeAll(this.syntax.ignore().or(this.syntax.propertyArgsEnd()));
        }
        this.stream.consumeAll(this.syntax.propertyEnd().or(this.syntax.ignore()));
        return property;
    }

    private String nextComment() {
        this.stream.consumeAll(this.syntax.commentBegin().or(this.syntax.ignore()));
        String comment = this.stream.nextUntil(this.syntax.commentEnd());
        this.stream.consumeAll(this.syntax.commentEnd().or(this.syntax.ignore()));
        return comment;
    }

}
