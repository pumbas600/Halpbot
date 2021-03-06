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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import org.checkerframework.checker.units.qual.Time;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import nz.pumbas.halpbot.actions.cooldowns.Cooldown;
import nz.pumbas.halpbot.actions.cooldowns.CooldownType;
import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.buttons.ButtonAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.decorators.log.Log;
import nz.pumbas.halpbot.permissions.PermissionService;
import nz.pumbas.halpbot.utilities.Reflect;
import nz.pumbas.halpbot.utilities.Require;
import nz.pumbas.halpbot.triggers.Trigger;
import nz.pumbas.halpbot.utilities.Duration;

@Log
@Service
public class HalpbotCommands
{
    @Inject private PermissionService permissionService;
    @Inject private ApplicationContext applicationContext;

    @Command(alias = "source")
    public String source() {
        return "You can see the source code for me here: https://github.com/pumbas600/HalpBot :kissing_heart:";
    }

    @Command(alias = "invite", description = "Retrieves the invite for this discord bot")
    public String invite() {
        return "https://canary.discord.com/api/oauth2/authorize?client_id=819840092327772170&permissions=2147544128&scope=bot%20applications.commands";
    }

    @Command(alias = "suggestion")
    public String suggestion() {
        return "You can note issues and suggestions for me here: https://github.com/pumbas600/HalpBot/issues";
    }

    @Trigger(value = {"halpbot", "show", "easter egg"}, require = Require.ALL, display = @Duration(20))
    public String easterEgg() {
        return "Here's the easter egg! :sparkles:";
    }

    @Command
    public String author(User user) {
        return user.toString();
    }

    @Command
    public String hasPermission(@Nullable @Source Guild guild,
                                @Source Member author,
                                String permission,
                                @Unrequired @Nullable Member member)
    {
        if (guild == null)
            return "This cannot be done in a private message";
        if (member == null)
            member = author;
        return this.permissionService.hasPermission(guild, member, permission)
                ? "You have the permission!"
                : "You don't have the permission :(";
    }

    @Time
    @Command(description = "Tests dynamic buttons")
    public MessageAction dynamicDemo(MessageReceivedEvent event, ButtonAdapter buttonAdapter, @Remaining String suffix) {
        return event.getChannel()
                .sendMessage("This is a dynamic suffix adding button demo")
                .setActionRow(buttonAdapter.register(
                        Button.primary("halpbot:button:suffix", "Add suffix"), suffix));
    }

    @Time
    @ButtonAction(id = "halpbot:button:suffix", display = @Duration(10))
    public String suffix(@Source User user, String suffix) {
        return user.getName() + suffix;
    }

    @Cooldown(duration = @Duration(90), type = CooldownType.MEMBER)
    @Command(description = "Tests the cooldown decorators")
    public String cooldownMember() {
        return "This command is logged when its invoked and has a 90 second cooldown!";
    }

    @Cooldown(duration = @Duration(90), type = CooldownType.USER)
    @Command(description = "Tests the cooldown decorators")
    public String cooldownUser() {
        return "This command is logged when its invoked and has a 90 second cooldown!";
    }

    @Cooldown(duration = @Duration(90), type = CooldownType.GUILD)
    @Command(description = "Tests the cooldown decorators")
    public String cooldownGuild() {
        return "This command is logged when its invoked and has a 90 second cooldown!";
    }

    @Command(description = "Tests the @Log decorator")
    public String log() {
        return "This command is logged when it is invoked";
    }

    @Time
    @Command(description = "Tests the @Time decorator")
    public String time(int limit) {
        double sum = 0;
        // Some expensive action:
        for (int i = 0; i < limit; i++) {
            sum += Math.sqrt(i);
        }

        return "Action complete!";
    }

    @Command(alias = "centroid", description = "Finds the centroid defined by the specified shapes")
    public String centroid(@Implicit Shape[] shapes)
    {
        double sumAx = 0;
        double sumAy = 0;
        double totalA = 0;

        for (Shape shape : shapes) {
            sumAx += shape.getArea() * shape.getxPos();
            sumAy += shape.getArea() * shape.getyPos();
            totalA += shape.getArea();
        }

        return String.format("x: %.2f, y: %.2f", sumAx / totalA, sumAy / totalA);
    }
}
