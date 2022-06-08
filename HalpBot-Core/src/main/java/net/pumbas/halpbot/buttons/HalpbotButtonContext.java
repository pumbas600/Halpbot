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

import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.objects.AsyncDuration;

import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.inject.binding.ComponentBinding;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@ComponentBinding(ButtonContext.class)
@AllArgsConstructor(onConstructor_ = @Bound)
public class HalpbotButtonContext implements ButtonContext
{
    private final String id;
    private final boolean isEphemeral;
    private final Duration displayDuration;
    private final ActionInvokable<ButtonInvocationContext> actionInvokable;
    private final Object[] passedParameters;
    private final List<ParsingToken> nonCommandParameterTokens;
    private int remainingUses;
    private final AsyncDuration removeAfter;
    @Nullable
    private final AfterRemovalFunction afterRemoval;

    @Override
    public void deductUse() {
        this.remainingUses--;
    }
}
