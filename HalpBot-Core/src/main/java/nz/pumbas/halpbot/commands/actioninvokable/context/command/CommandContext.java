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

package nz.pumbas.halpbot.commands.actioninvokable.context.command;

import java.util.List;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.commands.Content;
import nz.pumbas.halpbot.commands.actioninvokable.context.TokenActionContext;

public interface CommandContext extends TokenActionContext, DisplayableResult
{
    /**
     * @return The {@link String alias} for this command.
     */
    List<String> aliases();

    default String aliasesString() {
        return String.join(" | ", this.aliases());
    }

    /**
     * @return The {@link String description} if present, otherwise null
     */
    String description();

    /**
     * @return The {@link String usage} for this command
     */
    String usage();

    boolean preserveWhitespace();

    Content content();
}
