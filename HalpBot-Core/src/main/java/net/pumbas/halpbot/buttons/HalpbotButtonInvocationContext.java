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

package net.pumbas.halpbot.buttons;

import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.events.HalpbotEvent;

import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.inject.binding.ComponentBinding;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;

import jakarta.inject.Inject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = false)
@ComponentBinding(ButtonInvocationContext.class)
@RequiredArgsConstructor(onConstructor_ = @Bound)
public class HalpbotButtonInvocationContext implements ButtonInvocationContext
{
    @Inject
    private ApplicationContext applicationContext;
    @Setter
    private TypeContext<?> currentType;

    private final HalpbotEvent halpbotEvent;
    private final List<ParsingToken> nonCommandParameterTokens;
    private final Object[] passedParameters;
}
