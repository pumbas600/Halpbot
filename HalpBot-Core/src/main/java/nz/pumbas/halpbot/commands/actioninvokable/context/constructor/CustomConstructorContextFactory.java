package nz.pumbas.halpbot.commands.actioninvokable.context.constructor;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@Service
public interface CustomConstructorContextFactory
{
    @Factory
    CustomConstructorContext create(String usage,
                                    ActionInvokable<CommandInvocationContext> actionInvokable,
                                    Set<TypeContext<?>> reflections,
                                    List<Token> tokens);
}
