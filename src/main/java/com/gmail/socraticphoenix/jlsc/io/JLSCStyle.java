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

import com.gmail.socraticphoenix.parse.Strings;

public interface JLSCStyle {

    JLSCStyle DEFAULT = new Default();

    JLSCStyle CODE = new Code();

    String delimiter(int indent);

    String compoundValueDelimiter(int indent);

    String arrayValueDelimiter(int indent);

    String beginArray(int indent);

    String endArray(int indent);

    String beginCompound(int indent);

    String endCompound(int indent);

    String beginComment(int indent);

    String endComment(int indent);

    String beginProperty(int indent);

    String beginPropertyArgs(int indent);

    String propertyArgsDelimiter(int indent);

    String endPropertyArgs(int indent);

    String endProperty(int indent);

    String preArrayVal(int indent);

    String preKey(int indent);

    boolean doLastCompoundValue();

    boolean doLastArrayValue();

    class Default implements JLSCStyle {

        @Override
        public String delimiter(int indent) {
            return "= ";
        }

        @Override
        public String compoundValueDelimiter(int indent) {
            return System.lineSeparator();
        }

        @Override
        public String arrayValueDelimiter(int indent) {
            return "," + System.lineSeparator();
        }

        @Override
        public String beginArray(int indent) {
            return "[" + System.lineSeparator();
        }

        @Override
        public String endArray(int indent) {
            return System.lineSeparator() + Strings.indent(indent) + "]";
        }

        @Override
        public String beginCompound(int indent) {
            return "{" + System.lineSeparator();
        }

        @Override
        public String endCompound(int indent) {
            return System.lineSeparator() + Strings.indent(indent) + "}";
        }

        @Override
        public String beginComment(int indent) {
            return Strings.indent(indent) + "#";
        }

        @Override
        public String endComment(int indent) {
            return System.lineSeparator();
        }

        @Override
        public String beginProperty(int indent) {
            return "@";
        }

        @Override
        public String beginPropertyArgs(int indent) {
            return "(";
        }

        @Override
        public String propertyArgsDelimiter(int indent) {
            return ", ";
        }

        @Override
        public String endPropertyArgs(int indent) {
            return ")";
        }

        @Override
        public String endProperty(int indent) {
            return ":";
        }

        @Override
        public String preArrayVal(int indent) {
            return Strings.indent(indent);
        }

        @Override
        public String preKey(int indent) {
            return Strings.indent(indent);
        }

        @Override
        public boolean doLastCompoundValue() {
            return false;
        }

        @Override
        public boolean doLastArrayValue() {
            return false;
        }

    }

    class Code implements JLSCStyle {

        @Override
        public String delimiter(int indent) {
            return " >> ";
        }

        @Override
        public String compoundValueDelimiter(int indent) {
            return ";" + System.lineSeparator();
        }

        @Override
        public String arrayValueDelimiter(int indent) {
            return "," + System.lineSeparator();
        }

        @Override
        public String beginArray(int indent) {
            return "[" + System.lineSeparator();
        }

        @Override
        public String endArray(int indent) {
            return System.lineSeparator() + Strings.indent(indent) + "]";
        }

        @Override
        public String beginCompound(int indent) {
            return "{" + System.lineSeparator();
        }

        @Override
        public String endCompound(int indent) {
            return System.lineSeparator() + Strings.indent(indent) + "}";
        }

        @Override
        public String beginComment(int indent) {
            return Strings.indent(indent) + "//";
        }

        @Override
        public String endComment(int indent) {
            return System.lineSeparator();
        }

        @Override
        public String beginProperty(int indent) {
            return "@";
        }

        @Override
        public String beginPropertyArgs(int indent) {
            return "(";
        }

        @Override
        public String propertyArgsDelimiter(int indent) {
            return ", ";
        }

        @Override
        public String endPropertyArgs(int indent) {
            return ")";
        }

        @Override
        public String endProperty(int indent) {
            return ":";
        }

        @Override
        public String preArrayVal(int indent) {
            return Strings.indent(indent);
        }

        @Override
        public String preKey(int indent) {
            return Strings.indent(indent);
        }

        @Override
        public boolean doLastCompoundValue() {
            return true;
        }

        @Override
        public boolean doLastArrayValue() {
            return false;
        }

    }

}
