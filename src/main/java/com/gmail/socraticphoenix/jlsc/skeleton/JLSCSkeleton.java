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
package com.gmail.socraticphoenix.jlsc.skeleton;

import com.gmail.socraticphoenix.jlsc.JLSCDataHolder;
import com.gmail.socraticphoenix.jlsc.JLSCQuery;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.parse.ParseResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class JLSCSkeleton implements JLSCVerifier {
    private Map<JLSCQuery, JLSCVerifier> verifiers;

    public JLSCSkeleton(Map<JLSCQuery, JLSCVerifier> verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    public ParseResult verify(JLSCValue value) {
        ParseResult.Builder builder = ParseResult.builder();
        JLSCDataHolder dataHolder = value.getAsDataHolder().get();
        builder.message("Skeleton successfully matched data holder").succesful(true);

        for(Map.Entry<JLSCQuery, JLSCVerifier> entry : this.verifiers.entrySet()) {
            Optional<JLSCValue> valueOptional = dataHolder.get(entry.getKey());
            if(valueOptional.isPresent()) {
                ParseResult result = entry.getValue().verify(valueOptional.get());
                if(!result.isSuccesful()) {
                    builder.succesful(false);
                    builder.message("Skeleton did not match data holder");
                    builder.node(ParseResult.unSuccesful("Invalid value at path \"" + entry.getKey().toString() + "\"", result));
                    break;
                } else {
                    builder.node(ParseResult.debug("Valid value at path \"" + entry.getKey().toString() + "\"", result));
                }
            } else {
                builder.succesful(false);
                builder.message("Skeleton did not match data holder");
                builder.node(ParseResult.unSuccesful("Expected value at path \"" + entry.getKey().toString() + "\""));
                break;
            }
        }

        return builder.build();
    }

    public static JLSCSkeleton.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JLSCQuery basePath;
        private Stack<Integer> lengths;
        private Map<JLSCQuery, JLSCVerifier> verifiers;

        public Builder() {
            this.basePath = null;
            this.verifiers = new HashMap<>();
            this.lengths = new Stack<>();
        }

        public Builder require(int key, JLSCVerifier verifier) {
            this.verifiers.put(this.basePath == null ? JLSCQuery.of(key) : this.basePath.append(key), verifier);
            return this;
        }

        public Builder require(String key, JLSCVerifier verifier) {
            this.verifiers.put(this.basePath == null ? JLSCQuery.of(key) : this.basePath.append(key), verifier);
            return this;
        }

        public Builder require(JLSCQuery key, JLSCVerifier verifier) {
            this.verifiers.put(this.basePath == null ? JLSCQuery.of(key) : this.basePath.append(key), verifier);
            return this;
        }

        public Builder require(int key) {
            return this.require(key, JLSCVerifiers.exists());
        }

        public Builder require(String key) {
            return this.require(key, JLSCVerifiers.exists());
        }

        public Builder require(JLSCQuery key) {
            return this.require(key, JLSCVerifiers.exists());
        }

        public Builder push(String piece) {
            this.basePath = basePath == null ? JLSCQuery.of(piece) : this.basePath.append(piece);
            this.lengths.push(1);
            return this;
        }

        public Builder push(int piece) {
            this.basePath = basePath == null ? JLSCQuery.of(piece) : this.basePath.append(piece);
            this.lengths.push(1);
            return this;
        }

        public Builder push(JLSCQuery piece) {
            this.basePath = basePath == null ? piece : this.basePath.append(piece);
            return this;
        }

        public Builder pop() {
            if(!this.lengths.isEmpty() && this.basePath != null) {
                int len = this.lengths.pop();
                if(len >= this.basePath.length()) {
                    this.basePath = null;
                } else {
                    this.basePath = this.basePath.subQuery(0, this.basePath.length() - len);
                }
            }
            return this;
        }

        public JLSCSkeleton build() {
            return new JLSCSkeleton(this.verifiers);
        }

        public Builder reset() {
            this.verifiers.clear();
            this.lengths.clear();
            this.basePath = null;
            return this;
        }

    }



}
