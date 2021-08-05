package nz.pumbas.commands.tokens;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.exceptions.OutputException;
import nz.pumbas.commands.exceptions.TokenCommandException;
import nz.pumbas.commands.tokens.context.MethodContext;
import nz.pumbas.commands.tokens.tokentypes.Token;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.tokens.tokentypes.PlaceholderToken;
import nz.pumbas.utilities.Exceptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A container for the data of an {@link Token} command.
 */
public class TokenCommand implements CommandMethod
{

    private final @Nullable Object instance;
    private final @NotNull Executable executable;
    private final @NotNull List<Token> tokens;
    private final @NotNull String displayCommand;
    private final @NotNull String description;
    private final @NotNull String permission;
    private final @NotNull Set<Long> restrictedTo;
    private final @NotNull Set<Class<?>> reflections;

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable, @NotNull List<Token> tokens) {
        this(instance, executable, tokens, Collections.emptySet());
    }

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable,
                        @NotNull List<Token> tokens,
                        @NotNull Set<Class<?>> reflections) {
        this(instance, executable, tokens, "", "", "",
            Collections.emptySet(), reflections);
    }

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable,
                        @NotNull List<Token> tokens, @NotNull String displayCommand,
                        @NotNull String description, @NotNull String permission, @NotNull Set<Long> restrictedTo,
                        @NotNull Set<Class<?>> reflections)
    {
        this.instance = instance;
        this.executable = executable;
        this.tokens = tokens;
        this.displayCommand = displayCommand;
        this.description = description;
        this.permission = permission;
        this.restrictedTo = restrictedTo;
        this.reflections = reflections;
    }

    /**
     * @return The {@link Executable} for this {@link nz.pumbas.commands.annotations.Command}
     */
    public @NotNull Executable getExecutable() {
        return this.executable;
    }

    /**
     * @return An {@link List} of {@link Token command tokens} representing this {@link nz.pumbas.commands.annotations.Command}
     */
    public @NotNull List<Token> getCommandTokens() {
        return this.tokens;
    }

    /**
     * @return The an array of the {@link Class parameter types} of the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command}
     */
    public @NotNull Class<?>[] getParameterTypes() {
        return this.executable.getParameterTypes();
    }

    /**
     * @return The {@link String description}. Note if there is no description this will be empty
     */
    @Override
    public @NotNull String getDescription()
    {
        return this.description;
    }

    /**
     * @return The {@link String} representation of the command
     */
    @Override
    public @NotNull String getDisplayCommand()
    {
        return this.displayCommand;
    }

    /**
     * @return The {@link String permission} for this command. If there is no permission, this will an empty string
     */
    @Override
    public @NotNull String getPermission()
    {
        return this.permission;
    }

    /**
     * @return The {@link List<Long>} of user ids for who this command is restricted to
     */
    @Override
    public @NotNull Set<Long> getRestrictedTo()
    {
        return this.restrictedTo;
    }

    /**
     * @return The {@link Class classes} that can have static methods invoked from
     */
    @Override
    public @NotNull Set<Class<?>> getReflections()
    {
        return this.reflections;
    }

    /**
     * @return The {@link Object} instance for this {@link CommandMethod}
     */
    @Override
    public @Nullable Object getInstance()
    {
        return this.instance;
    }

    /**
     * Invokes the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command} with the specified arguments.
     *
     * @param parameters
     *      The {@link Object parameters} which will be used to invoke the {@link Executable} with
     *
     * @return An {@link Optional} containing the result of the {@link Executable} if there is one
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    public Exceptional<Object> invoke(@NotNull Object... parameters) throws OutputException
    {
        try {
            if (this.executable instanceof Method)
                return Exceptional.of(((Method) this.executable).invoke(this.instance, parameters));
            else if (this.executable instanceof Constructor<?>)
                return Exceptional.of(((Constructor<?>) this.executable).newInstance(parameters));
        }
        catch (java.lang.IllegalAccessException | InstantiationException e) {
            ErrorManager.handle(e, String.format("There was an error invoking the command method, %s",
                this.executable.getName()));
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof OutputException)
                throw (OutputException) e.getTargetException();
            else ErrorManager.handle(e, String.format("There was an error thrown within the command method, %s",
                this.executable.getName()));
        }

        return Exceptional.empty();
    }

    /**
     * parses the {@link MethodContext} and invokes the {@link Executable} for this
     * {@link nz.pumbas.commands.annotations.Command}. By default, it cannot have any tokens left over.
     *
     * @param ctx
     *      The {@link MethodContext}
     *
     * @return A {@link Exceptional} containing the returned value of the {@link Executable}
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    public Exceptional<Object> parse(@NotNull MethodContext ctx)
    {
        return this.parse(ctx, false);
    }

    /**
     * parses the {@link MethodContext} and invokes the {@link Executable} for this
     * {@link nz.pumbas.commands.annotations.Command}.
     *
     * @param ctx
     *      The {@link MethodContext}
     * @param canHaveTokensLeft
     *      If there can be tokens left over
     *
     * @return A {@link Exceptional} containing the returned value of the {@link Executable}
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    public Exceptional<Object> parse(@NotNull MethodContext ctx, boolean canHaveTokensLeft)
    {
        int currentIndex = ctx.getCurrentIndex();
        Exceptional<Object> result = this.parseParameters(ctx, canHaveTokensLeft)
            .flatMap((Function<Object[], Exceptional<Object>>) this::invoke);

        if (result.caught()) ctx.setCurrentIndex(currentIndex);
        return result;
    }

    /**
     * Parses the {@link MethodContext} into an array of {@link Object objects} which can be used to invoke this
     * {@link Executable}.
     *
     * @param ctx
     *      The {@link MethodContext}
     *
     * @return An {@link Exceptional} containing the parsed parameters
     */
    public Exceptional<Object[]> parseParameters(@NotNull MethodContext ctx, boolean canHaveTokensLeft)
    {
        Object[] parsedTokens = new Object[this.executable.getParameterCount()];

        int tokenIndex = 0;
        int parameterIndex = 0;
        Exceptional<Object[]> firstMismatch = Exceptional.empty();

        while (parameterIndex < parsedTokens.length) {
            if (tokenIndex >= this.tokens.size())
                return firstMismatch.isErrorAbsent()
                    ? Exceptional.of(new TokenCommandException("There appears to be too many parameters for this command"))
                    : firstMismatch;

            Token currentToken = this.tokens.get(tokenIndex++);
            Exceptional<Object[]> mismatchResult = Exceptional.empty();
            int currentIndex = ctx.getCurrentIndex();

            if (currentToken instanceof ParsingToken) {
                ParsingToken parsingToken = (ParsingToken) currentToken;
                ctx.update(parsingToken);

                Exceptional<Object> result = TokenManager.handleReflectionSyntax(ctx);
                if (result.present()) {
                    parsedTokens[parameterIndex++] = result.get();
                    continue;
                }
                else if (result.caught())
                    return result.map(o -> new Object[0]);
                else {
                    ctx.setCurrentIndex(currentIndex);

                    result = parsingToken.parser()
                        .mapper()
                        .apply(ctx)
                        .map(o -> o);

                    if (result.caught())
                        mismatchResult = result.map(o -> new Object[0]);
                    else {
                        if (!firstMismatch.isEmpty())
                            firstMismatch = Exceptional.empty();

                        parsedTokens[parameterIndex++] = result.orNull();
                        continue;
                    }
                }
            }
            else if (currentToken instanceof PlaceholderToken) {
                PlaceholderToken placeholderToken = (PlaceholderToken) currentToken;

                if (placeholderToken.matches(ctx)) {
                    if (!firstMismatch.isEmpty())
                        firstMismatch = Exceptional.empty();
                    continue;
                }
                mismatchResult = Exceptional.of(
                    new TokenCommandException("Expected the placeholder " + placeholderToken.placeHolder()));
            }


            if (firstMismatch.isEmpty())
                firstMismatch = mismatchResult;

            if (currentToken.isOptional()) {
                if (currentToken instanceof ParsingToken)
                    parsedTokens[parameterIndex++] = ((ParsingToken) currentToken).defaultValue();
            }
            else return firstMismatch;
        }

        if (ctx.hasNext() && !canHaveTokensLeft)
            return firstMismatch.caught()
                ? firstMismatch : Exceptional.of(
                    new TokenCommandException("There appears to be too many parameters for this command"));

        return Exceptional.of(parsedTokens);
    }
}
