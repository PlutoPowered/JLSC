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

import com.gmail.socraticphoenix.jlsc.JLSCArray;
import com.gmail.socraticphoenix.jlsc.JLSCCompound;
import com.gmail.socraticphoenix.jlsc.JLSCQuery;
import com.gmail.socraticphoenix.jlsc.value.JLSCKeyValue;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.parse.ParseResult;
import com.gmail.socraticphoenix.parse.Strings;

import java.util.Optional;

public class JLSCVerifiers {

    public static JLSCVerifier and(JLSCVerifier... verifiers) {
        return value -> {
            ParseResult.Builder builder = ParseResult.builder();
            builder.message("All conditions met in AND").succesful(true);
            for (JLSCVerifier verifier : verifiers) {
                ParseResult result = verifier.verify(value);
                if (!result.isSuccesful()) {
                    builder.message("Failed condition in AND").succesful(false).node(result);
                } else {
                    builder.node(result);
                }
            }

            if (builder.isSuccesful()) {
                builder.modify(p -> p.withDebug(true));
            }

            return builder.build();
        };
    }

    public static JLSCVerifier or(JLSCVerifier... verifiers) {
        return value -> {
            ParseResult.Builder builder = ParseResult.builder();
            builder.message("No conditions met in OR").succesful(false);
            for (JLSCVerifier verifier : verifiers) {
                ParseResult result = verifier.verify(value);
                if (result.isSuccesful()) {
                    builder.message("Met at least one condition in OR").node(result).succesful(true);
                } else {
                    builder.node(result);
                }
            }

            if (builder.isSuccesful()) {
                builder.modify(p -> p.withDebug(!p.isSuccesful()));
            }

            return builder.build();
        };
    }

    public static JLSCVerifier array(JLSCVerifier elementVerifier) {
        return JLSCVerifiers.and(JLSCVerifiers.type(JLSCArray.class), value -> {
            JLSCArray array = value.getAsArray().get();
            ParseResult.Builder builder = ParseResult.builder();
            builder.message("All elements in array are valid").succesful(true);
            int i = 0;
            for (JLSCValue element : array.leaves(false)) {
                ParseResult result = elementVerifier.verify(element);
                if (!result.isSuccesful()) {
                    builder.message("At least one element in array is invalid").succesful(false).node(ParseResult.unSuccesful("Invalid value at index " + i, result));
                } else {
                    builder.node(ParseResult.debug("Valid value at index " + i, result));
                }

                i++;
            }

            return builder.build();
        });
    }

    public static JLSCVerifier compound(JLSCVerifier elementVerifier) {
        return JLSCVerifiers.and(JLSCVerifiers.type(JLSCCompound.class), value -> {
            JLSCCompound compound = value.getAsCompound().get();
            ParseResult.Builder builder = ParseResult.builder();
            builder.message("All elements in compound are valid").succesful(true);
            for (JLSCKeyValue keyValue : compound.entries()) {
                ParseResult result = elementVerifier.verify(keyValue.getValue());
                if (!result.isSuccesful()) {
                    builder.message("At least one element in compound is invalid").succesful(false).node(ParseResult.unSuccesful("Invalid value at key \"" + Strings.escape(keyValue.getKey()) + "\""));
                } else {
                    builder.node(ParseResult.debug("Valid value at key \"" + Strings.escape(keyValue.getKey()) + "\""));
                }
            }

            return builder.build();
        });
    }

    public static JLSCVerifier is(Object target) {
        return value -> {
            if (target == null && value.isNull()) {
                return ParseResult.succesful("Value is null");
            } else if (target == null && !value.isNull()) {
                return ParseResult.unSuccesful("Value is not null");
            } else {
                Optional val = value.getAs(target.getClass());
                if (val.isPresent() && val.get().equals(target)) {
                    return ParseResult.succesful("Value is " + Strings.escape(String.valueOf(target)));
                } else {
                    return ParseResult.unSuccesful("Value is not " + Strings.escape(String.valueOf(target)));
                }
            }
        };
    }

    public static JLSCVerifier is(Object... possible) {
        JLSCVerifier[] verifiers = new JLSCVerifier[possible.length];
        for (int i = 0; i < possible.length; i++) {
            verifiers[i] = JLSCVerifiers.is(possible[i]);
        }
        return JLSCVerifiers.or(verifiers);
    }

    public static JLSCVerifier enumValue(Class<? extends Enum> enumType) {
        Enum[] elements = enumType.getEnumConstants();
        JLSCVerifier[] verifiers = new JLSCVerifier[elements.length];
        for (int i = 0; i < elements.length; i++) {
            verifiers[i] = JLSCVerifiers.is(elements[i].name());
        }
        return JLSCVerifiers.or(verifiers);
    }

    public static JLSCVerifier exists(String key) {
        return JLSCVerifiers.and(JLSCVerifiers.type(JLSCCompound.class), value -> {
            if (value.getAsDataHolder().get().get(key).isPresent()) {
                return ParseResult.succesful("Value at path [\"" + Strings.escape(key) + "\"] exists");
            } else {
                return ParseResult.succesful("No value at path [\"" + Strings.escape(key) + "\"]");
            }
        });
    }

    public static JLSCVerifier exists(int key) {
        return JLSCVerifiers.and(JLSCVerifiers.type(JLSCArray.class), value -> {
            if (value.getAsDataHolder().get().get(key).isPresent()) {
                return ParseResult.succesful("Value at path [" + key + "] exists");
            } else {
                return ParseResult.succesful("No value at path [" + key + "]");
            }
        });
    }

    public static JLSCVerifier exists(JLSCQuery query) {
        return JLSCVerifiers.and(JLSCVerifiers.or(JLSCVerifiers.type(JLSCCompound.class), JLSCVerifiers.type(JLSCArray.class)), value -> {
            if (value.getAsDataHolder().get().get(query).isPresent()) {
                return ParseResult.succesful("Value at path " + query.toString() + " exists");
            } else {
                return ParseResult.succesful("No value at path " + query.toString());
            }
        });
    }

    public static JLSCVerifier exists() {
        return value -> ParseResult.succesful("Value exists");
    }

    public static JLSCVerifier range(byte a, byte b) {
        byte min = a < b ? a : b;
        byte max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Byte.class), value -> {
            byte m = value.getAsByte().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCVerifier range(char a, char b) {
        char min = a < b ? a : b;
        char max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Character.class), value -> {
            char m = value.getAsCharacter().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCVerifier range(short a, short b) {
        short min = a < b ? a : b;
        short max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Short.class), value -> {
            short m = value.getAsShort().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCVerifier range(int a, int b) {
        int min = a < b ? a : b;
        int max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Integer.class), value -> {
            int m = value.getAsInteger().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCVerifier range(long a, long b) {
        long min = a < b ? a : b;
        long max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Long.class), value -> {
            long m = value.getAsLong().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCVerifier range(float a, float b) {
        float min = a < b ? a : b;
        float max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Float.class), value -> {
            float m = value.getAsFloat().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCVerifier range(double a, double b) {
        double min = a < b ? a : b;
        double max = b < a ? a : b;
        return JLSCVerifiers.and(JLSCVerifiers.type(Double.class), value -> {
            double m = value.getAsDouble().get();
            if (min <= m && m <= max) {
                return ParseResult.succesful("Value in range [" + min + ", " + max + "]");
            } else {
                return ParseResult.unSuccesful("Value must be in range [" + min + ", " + max + "]");
            }
        });
    }

    public static JLSCSkeleton.Builder skeleton() {
        return JLSCSkeleton.builder();
    }

    public static JLSCVerifier nullOrType(Class type) {
        return value -> {
            if (value.isNull() || value.getAs(type).isPresent()) {
                return ParseResult.succesful("Value conforms to type " + type.getName());
            } else {
                return ParseResult.unSuccesful("Value must conform to type " + type.getName());
            }
        };
    }

    public static JLSCVerifier nullOrDeserializable(Class type) {
        return value -> {
            if (value.isNull() || value.deserialize(type).isPresent()) {
                return ParseResult.succesful("Value can be deserialized to type " + type.getName());
            } else {
                return ParseResult.unSuccesful("Value must be deserializable to type " + type.getName());
            }
        };
    }

    public static JLSCVerifier nullOrConvertible(Class type) {
        return JLSCVerifiers.or(JLSCVerifiers.nullOrType(type), JLSCVerifiers.nullOrDeserializable(type));
    }

    public static JLSCVerifier type(Class type) {
        return value -> {
            if (value.getAs(type).isPresent()) {
                return ParseResult.succesful("Value conforms to type " + type.getName());
            } else {
                return ParseResult.unSuccesful("Value must conform to type " + type.getName());
            }
        };
    }

    public static JLSCVerifier deserializable(Class type) {
        return value -> {
            if (value.deserialize(type).isPresent()) {
                return ParseResult.succesful("Value can be deserialized to type " + type.getName());
            } else {
                return ParseResult.unSuccesful("Value must be deserializable to type " + type.getName());
            }
        };
    }

    public static JLSCVerifier convertible(Class type) {
        return JLSCVerifiers.or(JLSCVerifiers.type(type), JLSCVerifiers.deserializable(type));
    }

}
