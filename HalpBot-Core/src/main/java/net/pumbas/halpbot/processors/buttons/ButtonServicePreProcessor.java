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

package net.pumbas.halpbot.processors.buttons;

import net.pumbas.halpbot.buttons.ButtonHandler;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.processing.ServicePreProcessor;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;

public class ButtonServicePreProcessor implements ServicePreProcessor {

    @Override
    public Integer order() {
        return 2;
    }

    @Override
    public boolean preconditions(final ApplicationContext context, final Key<?> key) {
        return !key.type().methods(ButtonHandler.class).isEmpty();
    }

    @Override
    public <T> void process(final ApplicationContext context, final Key<T> key) {
        final TypeContext<T> type = key.type();

        final ButtonHandlerContext buttonHandlerContext = context.first(ButtonHandlerContext.class).get();
        final List<MethodContext<?, T>> buttonHandlers = type.methods(ButtonHandler.class);

        for (final MethodContext<?, T> buttonHandler : buttonHandlers) {
            if (!this.isValidButtonHandler(context, buttonHandler))
                continue;

            buttonHandlerContext.register(type, buttonHandler);
        }
    }

    private boolean isValidButtonHandler(final ApplicationContext context, final MethodContext<?, ?> buttonHandler) {
        if (!buttonHandler.isPublic()) {
            context.log().warn("The button handler %s must be public if its annotated with @ButtonHandler"
                .formatted(buttonHandler.qualifiedName()));
            return false;
        }
        return true;
    }
}