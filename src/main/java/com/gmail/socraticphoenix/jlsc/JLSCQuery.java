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

import com.gmail.socraticphoenix.collect.Items;
import com.gmail.socraticphoenix.collect.coupling.Switch;
import com.gmail.socraticphoenix.parse.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class JLSCQuery {
    private List<Switch<String, Integer>> path;

    private JLSCQuery(List<Switch<String, Integer>> path) {
        this.path = path;
    }

    public static JLSCQuery of(Object... path) {
        List<Switch<String, Integer>> aPath = new ArrayList<>();
        for(Object o : path) {
            if(o instanceof Integer) {
                aPath.add(Switch.ofB((Integer) o));
            } else if (o instanceof String) {
                aPath.add(Switch.ofA((String) o));
            }
        }
        return new JLSCQuery(aPath);
    }

    public JLSCQuery append(JLSCQuery other) {
        List<Switch<String, Integer>> path = new ArrayList<>();
        path.addAll(this.path);
        path.addAll(other.path);
        return new JLSCQuery(path);
    }

    public JLSCQuery prepend(JLSCQuery other) {
        List<Switch<String, Integer>> path = new ArrayList<>();
        path.addAll(other.path);
        path.addAll(this.path);
        return new JLSCQuery(path);
    }

    public JLSCQuery append(String s) {
        List<Switch<String, Integer>> path = new ArrayList<>();
        path.addAll(this.path);
        path.add(Switch.ofA(s));
        return new JLSCQuery(path);
    }

    public JLSCQuery append(int i) {
        List<Switch<String, Integer>> path = new ArrayList<>();
        path.addAll(this.path);
        path.add(Switch.ofB(i));
        return new JLSCQuery(path);
    }

    public JLSCQuery prepend(String s) {
        List<Switch<String, Integer>> path = new ArrayList<>();
        path.add(Switch.ofA(s));
        path.addAll(this.path);
        return new JLSCQuery(path);
    }

    public JLSCQuery prepend(int i) {
        List<Switch<String, Integer>> path = new ArrayList<>();
        path.add(Switch.ofB(i));
        path.addAll(this.path);
        return new JLSCQuery(path);
    }

    public int length() {
        return this.path.size();
    }

    public JLSCQuery subQuery(int start, int end) {
        return new JLSCQuery(Items.looseClone(this.path).subList(start, end));
    }

    public List<Object> getPath() {
        return this.path.stream().map(s -> s.containsA() ? s.getA().get() : s.getB().get()).collect(Collectors.toList());
    }

    public List<Switch<String, Integer>> getRawPath() {
        return Items.looseClone(this.path);
    }

    public Stack<Switch<String, Integer>> makeStack() {
        Stack<Switch<String, Integer>> stack = new Stack<>();
        for (int i = this.path.size() - 1; i >= 0; i--) {
            stack.push(this.path.get(i));
        }
        return stack;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JLSCQuery[");
        for (int i = 0; i < this.path.size(); i++) {
            Switch<String, Integer> val = this.path.get(i);
            if(val.containsA()) {
                builder.append("\"").append(Strings.escape(val.getA().get())).append("\"");
            } else {
                builder.append(val.getB().get());
            }

            if(i < this.path.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

}
