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

package net.pumbas.halpbot.converters.types;

import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.jetbrains.annotations.Nullable;

public class ArrayTypeContext extends TypeContext<Object>
{
    public static final ArrayTypeContext TYPE = new ArrayTypeContext();

    protected ArrayTypeContext() {
        super(Object.class);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null)
            return false;
        // Return true if this equals any array
        return o instanceof ArrayTypeContext || o instanceof TypeContext<?> typeContext && typeContext.type().isArray();
    }

    @Override
    public boolean childOf(TypeContext<?> type) {
        return type.isArray();
    }

    @Override
    public boolean childOf(@Nullable Class<?> to) {
        return to != null && to.isArray();
    }

    @Override
    public boolean parentOf(Class<?> to) {
        return to.isArray();
    }

    @Override
    public boolean parentOf(TypeContext<?> type) {
        return type.isArray();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public String toString() {
        return "TypeContext{*[]}";
    }
}
