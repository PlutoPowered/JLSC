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

import com.gmail.socraticphoenix.parse.ParserData;

import java.util.function.Predicate;

public interface JLSCSyntax {

    JLSCSyntax DEFAULT = new Default();

    Predicate<Character> compoundValueEnd();

    Predicate<Character> arrayValueEnd();

    Predicate<Character> keyValueDelimiter();

    Predicate<Character> commentBegin();

    Predicate<Character> commentEnd();

    Predicate<Character> compoundBegin();

    Predicate<Character> compoundEnd();

    Predicate<Character> arrayBegin();

    Predicate<Character> arrayEnd();

    Predicate<Character> propertyStart();

    Predicate<Character> propertyArgsStart();

    Predicate<Character> propertyArgsDelimiter();

    Predicate<Character> propertyArgsEnd();

    Predicate<Character> propertyEnd();

    Predicate<Character> ignore();

    ParserData data();

    default Predicate<Character> nonConsumableDelimiter() {
        return this.compoundBegin().or(this.arrayBegin());
    }

    class Default implements JLSCSyntax {

        @Override
        public Predicate<Character> compoundValueEnd() {
            return c -> c == ';' || c == '\n' || c == '\r' || c == ',';
        }

        @Override
        public Predicate<Character> arrayValueEnd() {
            return c -> c == ';' || c == '\n' || c == '\r' || c == ',';
        }

        @Override
        public Predicate<Character> keyValueDelimiter() {
            return c -> c == '=' || c == '>' || c == ':';
        }

        @Override
        public Predicate<Character> commentBegin() {
            return c -> c == '#' || c == '/' || c == '$';
        }

        @Override
        public Predicate<Character> commentEnd() {
            return c -> c == '\n' || c == '\r' || c == '#';
        }

        @Override
        public Predicate<Character> compoundBegin() {
            return c -> c == '{';
        }

        @Override
        public Predicate<Character> compoundEnd() {
            return c -> c == '}';
        }

        @Override
        public Predicate<Character> arrayBegin() {
            return c -> c == '[';
        }

        @Override
        public Predicate<Character> arrayEnd() {
            return c -> c == ']';
        }

        @Override
        public Predicate<Character> propertyStart() {
            return c -> c == '@';
        }

        @Override
        public Predicate<Character> propertyArgsStart() {
            return c -> c == '(';
        }

        @Override
        public Predicate<Character> propertyArgsDelimiter() {
            return c -> c == ',';
        }

        @Override
        public Predicate<Character> propertyArgsEnd() {
            return c -> c == ')';
        }

        @Override
        public Predicate<Character> propertyEnd() {
            return c -> c == ':';
        }

        @Override
        public Predicate<Character> ignore() {
            return c -> c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\b';
        }

        @Override
        public ParserData data() {
            return new ParserData()
                    .escapeChar('\\')
                    .brackets('(', ')')
                    .brackets('[', ']')
                    .brackets('{', '}')
                    .quote('"');
        }
    }

}
