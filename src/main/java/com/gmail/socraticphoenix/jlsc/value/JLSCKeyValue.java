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
package com.gmail.socraticphoenix.jlsc.value;

import com.gmail.socraticphoenix.jlsc.metadata.JLSCValueProperty;

import java.util.ArrayList;
import java.util.List;

public class JLSCKeyValue {
    private String key;
    private JLSCValue value;

    private List<String> comments;

    public JLSCKeyValue(String key, JLSCValue value) {
        this.key = key;
        this.value = value;
        this.comments = new ArrayList<>();
    }

    public void absorbMetadata(JLSCKeyValue other) {
        this.comments.clear();
        this.comments.addAll(other.comments);
        this.value.absorbMetadata(other.value);
    }

    public String getKey() {
        return this.key;
    }

    public JLSCValue getValue() {
        return this.value;
    }

    public List<String> getComments() {
        return this.comments;
    }

    public List<JLSCValueProperty> getProperties() {
        return this.value.getProperties();
    }

    public JLSCValueProperty getTypeSpecifier() {
        return this.value.getTypeSpecifier();
    }

}
