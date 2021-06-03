package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import nz.pumbas.commands.annotations.Command;

public class RegexCommand
{

    private final @NotNull Method method;
    private final @NotNull Object object;
    private final @NotNull Command commandAnotation;

    //Save this, as it will be repeatedly checked
    private final boolean hasCommand;
    private final String displayCommand;
    private final Pattern command;
    private final List<Constructor<?>> constructors;

    public RegexCommand(@NotNull Method method, @NotNull Object object, @NotNull Command commandAnnotation,
                        boolean hasCommand, String displayCommand, Pattern command, List<Constructor<?>> constructors)
    {
        this.method = method;
        this.object = object;
        this.commandAnotation = commandAnnotation;
        this.hasCommand = hasCommand;
        this.displayCommand = displayCommand;
        this.command = command;
        this.constructors = constructors;
    }

    public Optional<Object> invokeMethod(Object... args) throws InvocationTargetException
    {
        try {
            return Optional.ofNullable(this.method.invoke(this.object, args));
        } catch (java.lang.IllegalAccessException e) {
            ErrorManager.handle(e, String.format("There was an error invoking the command method, %s",
                this.method.getName()));
        }
        return Optional.empty();
    }

    public @NotNull Method getMethod()
    {
        return this.method;
    }

    public @NotNull String getDescription()
    {
        return this.commandAnotation.description();
    }

    public Pattern getCommand()
    {
        return this.command;
    }

    public String getDisplayCommand()
    {
        return this.displayCommand;
    }

    public boolean hasCommand()
    {
        return this.hasCommand;
    }

    public boolean hasParamaters()
    {
        return 0 != this.method.getParameterCount();
    }

    public boolean hasDescription()
    {
        return !this.getDescription().isEmpty();
    }

    public List<Constructor<?>> getConstructors()
    {
        return this.constructors;
    }
}
