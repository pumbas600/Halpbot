/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Unmodifiable;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.DefaultConverters;
import nz.pumbas.halpbot.converters.types.ArrayTypeContext;

@UseCommands
@HartshornTest
@Activator(scanPackages = "nz.pumbas.halpbot")
public class ConverterTests
{
    @InjectTest
    public void injectionTest(ApplicationContext applicationContext) {
        Assertions.assertNotNull(applicationContext);
    }

    @Test
    public void arrayTypeContextEqualityTests() {
        Assertions.assertEquals(ArrayTypeContext.TYPE, ArrayTypeContext.TYPE);
        Assertions.assertEquals(ArrayTypeContext.TYPE, TypeContext.of(Object[].class));
        Assertions.assertEquals(ArrayTypeContext.TYPE, TypeContext.of(Integer[].class));
    }

    @InjectTest
    public void retrievingArrayConverterTest(ConverterHandler converterHandler) {
        Converter<Object[]> arrayConverter = converterHandler.from(Object[].class);

        Assertions.assertEquals(DefaultConverters.ARRAY_CONVERTER, arrayConverter);
    }

    @InjectTest
    public void retrievingIntegerConverterTest(ConverterHandler converterHandler) {
        Converter<Integer> integerConverter = converterHandler.from(Integer.class);

        Assertions.assertEquals(DefaultConverters.INTEGER_CONVERTER, integerConverter);
    }

    @InjectTest
    public void retrievingListConverterTest(ConverterHandler converterHandler) {
        Converter<List> listConverter = converterHandler.from(List.class);

        Assertions.assertEquals(DefaultConverters.LIST_CONVERTER, listConverter);
    }

    @InjectTest
    public void retrievingSetConverterTest(ConverterHandler converterHandler) {
        Converter<Set> setConverter = converterHandler.from(Set.class);

        Assertions.assertEquals(DefaultConverters.SET_CONVERTER, setConverter);
    }

    @InjectTest
    public void retrievingUnmodifiableListConverterTest(ConverterHandler converterHandler) {
        Converter<List> listConverter = converterHandler.from(TypeContext.of(List.class), TypeContext.of(Unmodifiable.class));

        Assertions.assertEquals(DefaultConverters.UNMODIFIABLE_LIST_CONVERTER, listConverter);
    }

    @InjectTest
    public void retrievingRemainingStringsConverterTest(ConverterHandler converterHandler) {
        Converter<String> remainingStringsConverter = converterHandler
                .from(TypeContext.of(String.class), TypeContext.of(Remaining.class));

        Assertions.assertEquals(DefaultConverters.REMAINING_STRINGS_CONVERTER, remainingStringsConverter);
    }

    @InjectTest
    public void parsingRemainingStringsTest(ApplicationContext applicationContext, ConverterHandler converterHandler) {
        InvocationContext invocationContext = new InvocationContext(applicationContext, "This is a test sentence.");

        Converter<String> converter = converterHandler
                .from(TypeContext.of(String.class), TypeContext.of(Remaining.class));

        Exceptional<String> sentence = converter.apply(invocationContext);

        Assertions.assertTrue(sentence.present());
        Assertions.assertEquals("This is a test sentence.", sentence.get());
    }

    @InjectTest
    public void parsingArrayTest(ApplicationContext applicationContext, ConverterHandler converterHandler) {
        InvocationContext invocationContext = new InvocationContext(applicationContext, "[5 1 3 12 20]");
        Converter<Integer[]> converter = converterHandler.from(Integer[].class);

        Integer[] array = converter.apply(invocationContext).get();

        Assertions.assertEquals(5,  array[0]);
        Assertions.assertEquals(1,  array[1]);
        Assertions.assertEquals(3,  array[2]);
        Assertions.assertEquals(12, array[3]);
        Assertions.assertEquals(20, array[4]);
    }
}
