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
package com.gmail.socraticphoenix.jlsc.value.processors;

import com.gmail.socraticphoenix.jlsc.value.JLSCDualProcessor;
import com.gmail.socraticphoenix.jlsc.value.JLSCValue;
import com.gmail.socraticphoenix.mirror.Reflections;
import com.gmail.socraticphoenix.parse.Strings;
import com.gmail.socraticphoenix.pio.Bytes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

public interface JLSCPrimitiveProcessors {

    JLSCDualProcessor BYTE = new SimpleDualProcessor(JLSCPrimitiveProcessors::isByte,
            s -> (JLSCPrimitiveProcessors.isByte(s) ? Optional.of(JLSCValue.of(Byte.parseByte(s))) : Optional.empty()),
            c -> c == Byte.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= Byte.BYTES ? Optional.of(JLSCValue.of(b.get())) : Optional.empty()),
            v -> Byte.BYTES,
            (v, b) -> {
                if (v.directCast(Byte.class).isPresent()) {
                    b.put(v.directCast(Byte.class).get());
                }
            },
            "byte");

    JLSCDualProcessor SHORT = new SimpleDualProcessor(JLSCPrimitiveProcessors::isShort,
            s -> (JLSCPrimitiveProcessors.isShort(s) ? Optional.of(JLSCValue.of(Short.parseShort(s))) : Optional.empty()),
            c -> c == Short.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= Short.BYTES ? Optional.of(JLSCValue.of(b.getShort())) : Optional.empty()),
            v -> Short.BYTES,
            (v, b) -> {
                if (v.directCast(Short.class).isPresent()) {
                    b.putShort(v.directCast(Short.class).get());
                }
            },
            "short");

    JLSCDualProcessor CHARACTER = new SimpleDualProcessor(
            s -> {
                if (s.length() == 1) {
                    return true;
                } else if (s.length() == 3 && s.startsWith("'") && s.endsWith("'")) {
                    return true;
                } else {
                    String descaped = Strings.deEscape(s);
                    if (descaped.length() == 1) {
                        return true;
                    } else if (descaped.length() == 3 && s.startsWith("'") && s.endsWith("'")) {
                        return true;
                    }
                }
                return false;
            },
            s -> {
                if (s.length() == 1) {
                    return Optional.of(JLSCValue.of(s.charAt(0)));
                } else if (s.length() == 3 && s.startsWith("'") && s.endsWith("'")) {
                    return Optional.of(JLSCValue.of(s.charAt(1)));
                } else {
                    String descaped = Strings.deEscape(s);
                    if (descaped.length() == 1) {
                        return Optional.of(JLSCValue.of(descaped.charAt(0)));
                    } else if (descaped.length() == 3) {
                        return Optional.of(JLSCValue.of(descaped.charAt(1)));
                    }
                }
                return Optional.empty();
            },
            c -> c == Character.class,
            v -> Optional.of("'" + Strings.escape(v.rawToString()) + "'"),
            b -> (b.remaining() >= Character.BYTES ? Optional.of(JLSCValue.of(b.getChar())) : Optional.empty()),
            v -> Character.BYTES,
            (v, b) -> {
                if (v.directCast(Character.class).isPresent()) {
                    b.putChar(v.directCast(Character.class).get());
                }
            },
            "char");

    JLSCDualProcessor INTEGER = new SimpleDualProcessor(JLSCPrimitiveProcessors::isInteger,
            s -> (JLSCPrimitiveProcessors.isInteger(s) ? Optional.of(JLSCValue.of(Integer.parseInt(s))) : Optional.empty()),
            c -> c == Integer.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= Integer.BYTES ? Optional.of(JLSCValue.of(b.getInt())) : Optional.empty()),
            v -> Integer.BYTES,
            (v, b) -> {
                if (v.directCast(Integer.class).isPresent()) {
                    b.putInt(v.directCast(Integer.class).get());
                }
            },
            "int");

    JLSCDualProcessor LONG = new SimpleDualProcessor(JLSCPrimitiveProcessors::isLong,
            s -> (JLSCPrimitiveProcessors.isLong(s) ? Optional.of(JLSCValue.of(Long.parseLong(s))) : Optional.empty()),
            c -> c == Long.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= Long.BYTES ? Optional.of(JLSCValue.of(b.getLong())) : Optional.empty()),
            v -> Long.BYTES,
            (v, b) -> {
                if (v.directCast(Long.class).isPresent()) {
                    b.putLong(v.directCast(Long.class).get());
                }
            },
            "long");

    JLSCDualProcessor FLOAT = new SimpleDualProcessor(JLSCPrimitiveProcessors::isFloat,
            s -> (JLSCPrimitiveProcessors.isFloat(s) ? Optional.of(JLSCValue.of(Float.parseFloat(s))) : Optional.empty()),
            c -> c == Float.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= Float.BYTES ? Optional.of(JLSCValue.of(b.getFloat())) : Optional.empty()),
            v -> Float.BYTES,
            (v, b) -> {
                if (v.directCast(Float.class).isPresent()) {
                    b.putFloat(v.directCast(Float.class).get());
                }
            },
            "float");

    JLSCDualProcessor DOUBLE = new SimpleDualProcessor(JLSCPrimitiveProcessors::isDouble,
            s -> (JLSCPrimitiveProcessors.isDouble(s) ? Optional.of(JLSCValue.of(Double.parseDouble(s))) : Optional.empty()),
            c -> c == Double.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= Double.BYTES ? Optional.of(JLSCValue.of(b.getDouble())) : Optional.empty()),
            v -> Double.BYTES,
            (v, b) -> {
                if (v.directCast(Double.class).isPresent()) {
                    b.putDouble(v.directCast(Double.class).get());
                }
            },
            "double");

    JLSCDualProcessor BOOLEAN = new SimpleDualProcessor(s -> s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"),
            s -> s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false") ? Optional.of(JLSCValue.of(s.equalsIgnoreCase("true"))) : Optional.empty(),
            c -> c == Boolean.class,
            v -> Optional.of(v.rawToString()),
            b -> (b.remaining() >= 1 ? Optional.of(JLSCValue.of(Reflections.deepCast(boolean.class, b.get()))) : Optional.empty()),
            v -> 1,
            (v, b) -> {
                if (v.directCast(Boolean.class).isPresent()) {
                    b.put(v.getAsByte((byte) 0));
                }
            },
            "boolean");

    JLSCDualProcessor STRING = new SimpleDualProcessor(s -> s.startsWith("\"") && s.endsWith("\""),
            s -> (s.startsWith("\"") && s.endsWith("\"") ? Optional.of(JLSCValue.of(Strings.deEscape(Strings.cutFirst(Strings.cutLast(s))))) : Optional.of(JLSCValue.of(s))),
            c -> c == String.class,
            v -> Optional.of("\"" + Strings.escape(v.rawToString()) + "\""),
            b -> Optional.of(JLSCValue.of(Bytes.readString(b))),
            v -> Bytes.length(v.rawToString()),
            (v, b) -> Bytes.writeString(b, v.rawToString()),
            "string");

    JLSCDualProcessor BIG_INT = new SimpleDualProcessor(JLSCPrimitiveProcessors::isBigInteger,
            s -> JLSCPrimitiveProcessors.isBigInteger(s) ? Optional.of(JLSCValue.of(new BigInteger(s))) : Optional.empty(),
            c -> c == BigInteger.class,
            v -> Optional.of(v.rawToString()),
            b -> Optional.of(JLSCValue.of(Bytes.readBigInt(b))),
            v -> Bytes.length(v.getAs(BigInteger.class).get()),
            (v, b) -> Bytes.writeBigInt(b, v.getAs(BigInteger.class).get()),
            "bigint");

    JLSCDualProcessor BIG_DECIMAL = new SimpleDualProcessor(JLSCPrimitiveProcessors::isBigDecimal,
            s -> JLSCPrimitiveProcessors.isBigDecimal(s) ? Optional.of(JLSCValue.of(new BigDecimal(s))) : Optional.empty(),
            c -> c == BigDecimal.class,
            v -> Optional.of(v.rawToString()),
            b -> Optional.of(JLSCValue.of(Bytes.readBigDecimal(b))),
            v -> Bytes.length(v.getAs(BigDecimal.class).get()),
            (v, b) -> Bytes.writeBigDecimal(b, v.getAs(BigDecimal.class).get()),
            "bigdecimal");

     static boolean isByte(String s) {
        return JLSCPrimitiveProcessors.isLong(s) && JLSCPrimitiveProcessors.isInRangeInclusive(Byte.MIN_VALUE, Long.parseLong(s), Byte.MAX_VALUE);
    }

     static boolean isShort(String s) {
        return JLSCPrimitiveProcessors.isLong(s) && JLSCPrimitiveProcessors.isInRangeInclusive(Short.MIN_VALUE, Long.parseLong(s), Short.MAX_VALUE);
    }

     static boolean isInteger(String s) {
        return JLSCPrimitiveProcessors.isLong(s) && JLSCPrimitiveProcessors.isInRangeInclusive(Integer.MIN_VALUE, Long.parseLong(s), Integer.MAX_VALUE);
    }

     static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

     static boolean isFloat(String s) {
        return JLSCPrimitiveProcessors.isDouble(s) && JLSCPrimitiveProcessors.isInRangeInclusive(-Float.MAX_VALUE, Double.parseDouble(s), Float.MAX_VALUE);
    }

     static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

     static boolean isBigInteger(String s) {
        try {
            new BigInteger(s);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }


     static boolean isBigDecimal(String s) {
        try {
            new BigDecimal(s);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

     static boolean isInteger(double a) {
        return a - ((int) a) == 0;
    }


     static boolean isInRangeInclusive(long less, long val, long greater) {
        return less <= val && val <= greater;
    }

     static boolean isInRangeInclusive(double less, double val, double greater) {
        return less <= val && val <= greater;
    }

}
