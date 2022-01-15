# Halpbot ![JDK16](https://img.shields.io/badge/JDK-16-orange)

Halpbot is a comprehensive [JDA](https://github.com/DV8FromTheWorld/JDA) utility framework built using [Hartshorn](https://github.com/GuusLieben/Hartshorn) that provides a unique, annotation based approach to handling actions. It's key purpose is to alleviate as much unnecessary boilerplate code while simultaneously being both intuitive and customisable. For more detailed information on the various features Halpbot has to offer, including a getting started tutorial for people new to Java, check out the [wiki](https://github.com/pumbas600/Halpbot/wiki).

## Why use Halpbot?

Halpbot is a feature rich library with support for message commands, triggers, buttons and decorators. Halpbot makes **virtually all** default implementations overridable if you desire. It's approach to handling actions is unlike any current JDA framework; in fact it more closely resembles the approach seen in [Discord.py](https://github.com/Rapptz/discord.py). Some examples of what Halpbot can do are shown below. Do note that these examples only cover a small fraction of the functionality Halpbot has to offer and I would highly recommend browsing the [wiki](https://github.com/pumbas600/Halpbot/wiki) to get a better appreciation for what's possible.

### 2.1 Commands

Commands in Halpbot can simply be created by annotating a method with `@Command`. The method name will automatically be used as the alias (Although additional aliases can be set within the annotation if desired using the `alias` field). Command methods **must** be public; A warning will be logged during startup if you try and register non-public command methods. In Command methods, the parameters will act as command parameters.


There are two types of command parameters:
1. [Source Parameters]() - These are either injected services or information extracted from the event.
2. [Command Parameters]() - These are non-source parameters which are expected to be specified when invoking the command. These are automatically parsed from the command.

If a parameter was expected but wasn't present or didn't match the expected format, then the command will not be invoked and a temporary message will be sent to the user with the error. By default, Halpbot has support for most common built-in types, however, it's easy to create your own [custom parameter converters]() if you want! Finally, the returned result of the method is then automatically displayed to the user. 

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    // E.g: $pong
    @Command(description = "Simple pong command")
    public String pong(MessageReceivedEvent event) { 
        return event.getAuthor().getAsMention();
    }

    // E.g: $add 2 4.3
    @Command(description = "Adds two numbers")
    public double add(double num1, double num2) { 
        return num1 + num2;
    }

    // E.g: $pick yes no maybe or $choose yes no maybe
    @Command(aliases = { "pick", "choose" }, description = "Randomly chooses one of the items")
    public String choose(@Implicit String[] choices) { 
        // Use of @Implicit means that it's not necessary to surround the choices with [...]
        return choices[(int)(Math.random() * choices.length)];
    }
}
```

> **NOTE:** As the class is annotated with `@Service`, the commands will be automatically registered during startup. 

> **NOTE:** Command methods **must** be public. 

By default, Halpbot supports a vast range of parameter types as described [here](https://github.com/pumbas600/HalpBot/wiki/Command-Arguments), however, it's possible to easily create custom parameter converters to add support for custom types or annotations.

### Using Buttons

Halpbot also provides an easy way of working with buttons by simply annotating the button callbacks with `@ButtonAction`. These button action methods can take any [source parameters](https://github.com/pumbas600/Halpbot/wiki/Command-Arguments#source-converters) as arguments (A source parameter is anything that can be extracted from the event or injected) and in any order. To reference a button callback, all that you need to do is set the id of the `Button` to match the `@ButtonAction`:

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Duration;
```

</p>
</details>


```java
@Service
public class ExampleCommands
{
    @Command(description = "Displays two test buttons")
    public void buttons(MessageReceivedEvent event) { // E.g: $buttons
        event.getChannel().sendMessage("Click on one of these buttons!")
                .setActionRow(
                        // When the button is clicked, the @ButtonAction with the matching id is invoked
                        Button.primary("halpbot.example.primary", "Primary button!"), 
                        Button.secondary("halpbot.example.secondary", "Secondary button!")
                ).queue();
    }

    @ButtonAction(id = "halpbot.example.primary")
    public String primary(ButtonClickEvent event) { // You can directly pass the event
        return "%s clicked the primary button!".formatted(event.getUser().getName());
    }
    
    // The display duration field specifies that the result should only be displayed for 20 seconds
    @ButtonAction(id = "halpbot.example.secondary", displayDuration = @Duration(20))
    public String secondary(@Source User user) { // Alternatively, you can retrieve fields from the event using @Source
        return "%s clicked the secondary button!".formatted(user.getName());
    }
}
```

> **NOTE:** Like with commands, as the class is annotated with `@Service`, the button actions are automatically registered during startup.

### Decorators

Halpbot comes with three built-in [decorators](https://github.com/pumbas600/Halpbot/wiki/Decorators), however, the two main ones are the [cooldown](https://github.com/pumbas600/Halpbot/wiki/Decorators#cooldown) and [permissions](https://github.com/pumbas600/Halpbot/wiki/Decorators#permissions) decorators. Decorators are annotations that can be added to actions (`@Command` or `@ButtonAction`) that modify how the method is called, or if it's even called at all. 

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
    
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import nz.pumbas.halpbot.actions.cooldowns.Cooldown;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Duration;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    private final Map<Long, Integer> bank = new HashMap<>();
    private final Random random = new Random();
    
    // Restrict it so that this user can only call the command once per hour
    @Cooldown(duration = @Duration(value = 1, unit = ChronoUnit.HOURS))
    @Command(description = "Adds a random amount between $0 and $500 to the users account")
    public String collect(@Source User user) {
        long userId = user.getIdLong();

        int amount = this.random.nextInt(500);
        this.bank.putIfAbsent(userId, 0);
        int newAmount = amount + this.bank.get(userId);
        this.bank.put(userId, newAmount);

        return "You collected %d. You now have %d in your account".formatted(amount, newAmount);
    }
}
```

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.permissions.Permissions;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    // E.g: $kick @pumbas600 or $kick @pumbas600 some reason
    // Requires the bot to have the KICK_MEMBERS permission.
    @Nullable
    @Permissions(self = Permission.KICK_MEMBERS)
    @Command(description = "Kicks a member from the guild")
    public String kick(MessageReceivedEvent event, Member member,
                       @Remaining @Unrequired("No reason specified") String reason)
    {
        if (!event.getGuild().getSelfMember().canInteract(member))
            return "Cannot kick member: %s, they are higher in the heirarchy than I am".formatted(member.getEffectiveName());

        event.getGuild().kick(member, reason)
                .queue((success) -> event.getChannel()
                        .sendMessage("Successfully kicked %s!".formatted(member.getEffectiveName()))
                        .queue());
        return null; // Don't respond via halpbot as we're queueing a response normally
    }
}
```

> **NOTE:** It's also possible to add permissions that the user must have, along with creating your own custom [permission suppliers](https://github.com/pumbas600/Halpbot/wiki/Permissions#permission-suppliers).

## Getting Started

There is currently not a version of Halpbot available on Maven as some work still needs to be done beforehand. If you desperately want to get started, you can manually build `Halpbot-Core` yourself. You'll also need to build the latest version of [Hartshorn](https://github.com/GuusLieben/Hartshorn) for `hartshorn-core`, `hartshorn-data` and `harshorn-configuration`.
