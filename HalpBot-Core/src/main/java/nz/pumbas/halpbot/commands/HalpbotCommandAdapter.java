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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.AccessModifier;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;

import lombok.Setter;
import lombok.experimental.Accessors;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.annotations.CustomParameter;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContextFactory;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import nz.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContextFactory;
import nz.pumbas.halpbot.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.halpbot.commands.exceptions.MissingResourceException;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.Reflect;

@Singleton
@ComponentBinding(CommandAdapter.class)
@Accessors(chain = false)
public class HalpbotCommandAdapter implements CommandAdapter
{
    private final MultiMap<TypeContext<?>, CustomConstructorContext> customConstructors = new ArrayListMultiMap<>();
    private final Map<String, CommandContext> commands = new ConcurrentHashMap<>();
    private final Map<TypeContext<?>, MultiMap<String, CommandContext>> reflectiveCommands = new ConcurrentHashMap<>();

    private final Map<TypeContext<?>, String> typeAliases = new ConcurrentHashMap<>();
    private final Map<Long, String> guildPrefixes = new ConcurrentHashMap<>();

    @Setter
    @Getter
    private String defaultPrefix;
    @Setter
    @Getter
    private UsageBuilder usageBuilder;
    @Inject
    @Getter
    private ApplicationContext applicationContext;
    @Inject
    @Getter
    private ParameterAnnotationService parameterAnnotationService;
    @Inject
    @Getter
    private HalpbotCore halpbotCore;

    @Inject
    private CommandContextFactory commandContextFactory;
    @Inject
    private InvocationContextFactory invocationContextFactory;
    @Inject
    private CustomConstructorContextFactory customConstructorContextFactory;
    @Inject
    private TokenService tokenService;
    @Inject
    private DecoratorService decoratorService;

    //TODO: Setting the guild specific prefixes
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();
        String prefix = event.isFromType(ChannelType.TEXT)
            ? this.prefix(event.getGuild().getIdLong())
            : this.defaultPrefix;

        HalpbotEvent halpbotEvent = new MessageEvent(event);

        if (message.startsWith(prefix)) {
            message = message.substring(prefix.length()).stripLeading();

            String[] splitText = message.split("\\s", 2);
            String alias = splitText[0];
            String content = (2 == splitText.length) ? splitText[1] : "";

            Exceptional<CommandContext> eCommandContext = this.commandContextSafely(alias);
            if (eCommandContext.present()) {
                CommandContext commandContext = eCommandContext.get();
                if (commandContext.content() != Content.RAW) {
                    String tempContent = commandContext.content().parse(event);
                    int startIndex = tempContent.indexOf(alias);
                    if (startIndex != -1) {
                        content = tempContent.substring(startIndex + alias.length());
                    }
                }

                if (!commandContext.preserveWhitespace())
                    content = content.replaceAll("\\s+", " ");

                Exceptional<Object> result = this.handleCommandInvocation(halpbotEvent, commandContext, content);

                if (result.present())
                    this.displayResult(halpbotEvent, commandContext, result.get());
                else if (result.caught()) {
                    //this.applicationContext.log().error("Caught the error: ", result.error());
                    this.handleException(halpbotEvent, result.error());
                }
            } else this.halpbotCore.displayConfiguration()
                .displayTemporary(halpbotEvent,
                    "The command **" + alias + "** doesn't seem to exist, you may want to check your spelling",
                    30);
        }
    }

    private Exceptional<Object> handleCommandInvocation(HalpbotEvent event,
                                                        CommandContext commandContext,
                                                        String content) {
        CommandInvocationContext invocationContext = this.invocationContextFactory.command(content, event);
        return commandContext.invoke(invocationContext);
    }

    @Override
    @Nullable
    public CommandContext commandContext(String alias) {
        return this.commands.get(alias.toLowerCase(Locale.ROOT));
    }

    @Override
    public Collection<CommandContext> reflectiveCommandContext(TypeContext<?> targetType,
                                                               String methodName,
                                                               Set<TypeContext<?>> reflections) {
        if (!this.reflectiveCommands.containsKey(targetType))
            return Collections.emptyList();

        return this.reflectiveCommands.get(targetType).get(methodName.toLowerCase(Locale.ROOT))
            .stream()
            .filter(commandContext -> commandContext.executable() instanceof MethodContext methodContext
                && reflections.contains(methodContext.parent()))
            .toList();
    }

    @Override
    public <T> void registerMessageCommand(T instance, MethodContext<?, T> methodContext) {
        if (!methodContext.isPublic()) {
            this.applicationContext.log().warn("The command method %s must be public if its annotated with @Command"
                .formatted(methodContext.qualifiedName()));
            return;
        }

        if (!this.parameterAnnotationsAreValid(methodContext))
            return;

        Command command = methodContext.annotation(Command.class).get();
        List<String> aliases = this.aliases(command, methodContext);
        CommandContext commandContext = this.createCommand(
            aliases,
            command,
            methodContext,
            new HalpbotCommandInvokable(instance, methodContext));

        for (String alias : aliases) {
            if (this.commands.containsKey(alias)) {
                this.applicationContext.log().warn(
                    "The alias '%s' is already being used by the command '%s'. The command %s will not be registered under this alias"
                        .formatted(alias, this.commands.get(alias).toString(), commandContext.toString()));
                continue;
            }

            this.commands.put(alias, commandContext);
        }
    }

    @Override
    public <T> void registerSlashCommand(T instance, MethodContext<?, T> methodContext) {
        //TODO: Slash Commands
    }

    @Override
    public void registerReflectiveCommand(MethodContext<?, ?> methodContext) {
        if (!methodContext.isPublic() && !methodContext.has(AccessModifier.STATIC)) {
            this.applicationContext.log().warn(
                "The reflective method %s should be public and static if its annotated with @Reflective"
                    .formatted(methodContext.qualifiedName()));
            return;
        }

        if (methodContext.returnType().isVoid()) {
            this.applicationContext.log().warn(
                "The reflective method %s cannot return void if it is annotated with @Reflective"
                    .formatted(methodContext.qualifiedName()));
            return;
        }

        if (!this.parameterAnnotationsAreValid(methodContext)) return;

        Command command = methodContext.annotation(Command.class).get();
        List<String> aliases = this.aliases(command, methodContext);
        CommandContext commandContext = this.createCommand(
            aliases,
            command,
            methodContext,
            new HalpbotCommandInvokable(null, methodContext));

        TypeContext<?> returnType = methodContext.genericReturnType();
        if (!this.reflectiveCommands.containsKey(returnType))
            this.reflectiveCommands.put(returnType, new ArrayListMultiMap<>());

        MultiMap<String, CommandContext> aliasMappings = this.reflectiveCommands.get(returnType);

        for (String alias : aliases) {
            aliasMappings.put(alias.toLowerCase(Locale.ROOT), commandContext);
        }
    }

    @Override
    public String prefix(long guildId) {
        return this.guildPrefixes.getOrDefault(guildId, this.defaultPrefix);
    }

    private List<String> aliases(Command command, MethodContext<?, ?> methodContext) {
        List<String> aliases = HartshornUtils.asList(command.alias())
            .stream()
            .map(alias -> alias.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        // If an alias hasn't been specified, use the method name
        if (aliases.isEmpty())
            aliases.add(methodContext.name().toLowerCase(Locale.ROOT));
        return aliases;
    }

    private String usage(String usage, ExecutableElementContext<?, ?> executable) {
        if (!usage.isBlank())
            return usage;
        else return this.usageBuilder.buildUsage(this.applicationContext, executable);
    }

    private Set<TypeContext<?>> reflections(Class<?>[] reflections) {
        return Stream.of(reflections).map(TypeContext::of).collect(Collectors.toSet());
    }

    private <T> CommandContext createCommand(List<String> aliases,
                                             Command command,
                                             MethodContext<?, T> methodContext,
                                             ActionInvokable<CommandInvocationContext> actionInvokable) {
        TypeContext<T> parent = methodContext.parent();
        Set<TypeContext<?>> reflections = this.reflections(command.reflections());

        if (parent.annotation(Command.class).present()) {
            Command sharedProperties = parent.annotation(Command.class).get();
            reflections.addAll(this.reflections(sharedProperties.reflections()));
        }

        return this.commandContextFactory.create(
            aliases,
            command.description(),
            this.usage(command.usage(), methodContext),
            this.decoratorService.decorate(actionInvokable),
            this.tokenService.tokens(methodContext),
            reflections,
            HalpbotUtils.asDuration(command.display()),
            command.isEphemeral(),
            command.preserveWhitespace(),
            command.content()
        );
    }

    @Override
    public Collection<CustomConstructorContext> customConstructors(TypeContext<?> typeContext) {
        if (!this.customConstructors.containsKey(typeContext))
            throw new MissingResourceException(
                "There is no custom constructor registered for the type %s".formatted(typeContext.qualifiedName()));
        return this.customConstructors.get(typeContext);

    }

    @Override
    public void registerCustomConstructors(TypeContext<?> typeContext) {
        List<CustomConstructorContext> constructors = typeContext.constructors()
            .stream()
            .filter(constructor -> constructor.annotation(CustomConstructor.class).present())
            .map(constructor -> {
                CustomConstructor construction = constructor.annotation(CustomConstructor.class).get();
                List<Token> tokens = this.tokenService.tokens(constructor);

                return this.customConstructorContextFactory.create(
                    this.usage(construction.usage(), constructor),
                    this.decoratorService.decorate(new HalpbotCommandInvokable(null, constructor)),
                    this.reflections(construction.reflections()),
                    tokens);
            })
            .collect(Collectors.toList());

        if (constructors.isEmpty())
            throw new IllegalCustomParameterException(
                "The custom class %s, must define a constructor annotated with @ParameterConstructor"
                    .formatted(typeContext.qualifiedName()));

        this.applicationContext.log().info("Registered %d custom constructors found in %s"
            .formatted(constructors.size(), typeContext.qualifiedName()));
        this.customConstructors.putAll(typeContext, constructors);
    }

    @Override
    public Map<String, CommandContext> commands() {
        return Collections.unmodifiableMap(this.commands);
    }

    @Override
    public String typeAlias(TypeContext<?> typeContext) {
        if (!this.typeAliases.containsKey(typeContext)) {
            String alias;
            if (typeContext.annotation(CustomParameter.class).present())
                alias = typeContext.annotation(CustomParameter.class).get().identifier();
            else if (typeContext.isArray())
                alias = this.typeAlias(typeContext.elementType().get()) + "[]";
            else if (typeContext.isPrimitive())
                alias = Reflect.wrapPrimative(typeContext).name();
            else
                alias = typeContext.name();
            this.typeAliases.put(typeContext, alias);
        }

        return this.typeAliases.get(typeContext);
    }
}
