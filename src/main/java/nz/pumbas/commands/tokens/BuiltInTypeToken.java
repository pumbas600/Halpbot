package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.utilities.Utilities;
import org.jetbrains.annotations.Nullable;

public class BuiltInTypeToken implements ParsingToken
{
    protected boolean isOptional;
    protected Class<?> type;
    protected Object defaultValue;

    public BuiltInTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        this.isOptional = isOptional;
        if (!TokenManager.isBuiltInType(type))
            throw new IllegalArgumentException(
                    String.format("The type %s must be a built in type.", type));

        this.type = type;
        this.defaultValue = this.parseDefaultValue(defaultValue);
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
     * Returns if the passed in {@link String invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *      An individual element in the invocation of an {@link nz.pumbas.commands.annotations.Command}.
     *
     * @return If the {@link String invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull String invocationToken)
    {
        if (this.type.isEnum()) {
            return Utilities.isValidValue(this.type, invocationToken.toUpperCase());
        }

        return invocationToken.matches(TokenManager.TypeParsers.get(this.type).getKey());
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
     * Parses an {@link String invocation token} to the type of the {@link ParsingToken}.
     *
     * @param invocationToken
     *      The {@link String} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} of the {@link String invocation token} parsed to the correct type
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object parse(@NotNull String invocationToken)
    {
        if (this.type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>)this.type, invocationToken.toUpperCase());
        }
        return TokenManager.TypeParsers.get(this.type)
            .getValue()
            .apply(invocationToken);
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
        return String.format("BuiltInTypeToken{isOptional=%s, type=%s defaultValue=%s}",
            this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}
