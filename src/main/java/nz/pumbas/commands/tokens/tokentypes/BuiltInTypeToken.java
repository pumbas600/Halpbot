package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.Utilities;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class BuiltInTypeToken implements ParsingToken
{
    protected boolean isOptional;
    protected Class<?> type;
    protected Object defaultValue;
    protected Annotation[] annotations;

    public BuiltInTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        this(isOptional, type, defaultValue, new Annotation[0]);
    }

    public BuiltInTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue, Annotation[] annotations) {
        this.isOptional = isOptional;
        if (!TokenManager.isBuiltInType(type))
            throw new IllegalArgumentException(
                    String.format("The type %s must be a built in type.", type));

        this.type = type;
        this.defaultValue = this.parseDefaultValue(defaultValue);
        this.annotations = annotations;
    }

    protected BuiltInTypeToken() { }

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    /**
     * Returns if the passed in @link InvocationTokenInfo invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *     The {@link InvocationTokenInfo invocation token} containing the invoking information
     *
     * @return If the {@link InvocationTokenInfo invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull InvocationTokenInfo invocationToken)
    {
        if (this.type.isEnum()) {
            return Utilities.isValidValue(this.type, invocationToken.getNext().toUpperCase());
        }

        return Reflect.matches(invocationToken.getNext(), this.type);
    }

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @Override
    public Annotation[] getAnnotations()
    {
        return this.annotations;
    }

    /**
     * @return The required {@link Class type} of this {@link ParsingToken}
     */
    @Override
    public Class<?> getType()
    {
        return this.type;
    }

    /**
     * Parses an {@link InvocationTokenInfo invocation token} to the type of the {@link ParsingToken}.
     *
     * @param invocationToken
     *     The {@link InvocationTokenInfo invocation token} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} parsing the {@link InvocationTokenInfo invocation token} to the correct type
     */
    @Override
    public Object parse(@NotNull InvocationTokenInfo invocationToken)
    {
        String token = invocationToken.getNext();
        if (this.type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>)this.type, token.toUpperCase());
        }
        return Reflect.parse(token, this.type);
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    public @Nullable Object getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String toString()
    {
        return String.format("BuiltInTypeToken{isOptional=%s, type=%s, defaultValue=%s}",
            this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}
