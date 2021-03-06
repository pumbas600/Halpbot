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

package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.common.HalpbotBuilder;

// Indicates that the bot is a singleton and allows this class to be scanned for commands, etc
@Service

// Indicates that this class is an application activator
@Activator

// Activates the CommandAdapter ServicePreProcessors which scan for commands, converters, etc
@UseCommands
public class DemoBot
{
    public static void main(String[] args) {
        /*
         * This starts up Halpbot and automatically begins to scan for actions
         */
        HalpbotBuilder.create(DemoBot.class, args)
                .build(token -> JDABuilder.createDefault(token)
                        .setActivity(Activity.of(ActivityType.LISTENING, "to how cool Halpbot is!")));
    }
}
