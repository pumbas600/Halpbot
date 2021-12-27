package nz.pumbas.halpbot.commands.customconstructors;


import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@Binds(CustomConstructorContext.class)
public record HalpbotCustomConstructorContext(String usage,
                                              ActionInvokable<CommandInvocationContext> actionInvokable,
                                              Set<TypeContext<?>> reflections,
                                              List<Token> tokens)
    implements CustomConstructorContext
{
    @Bound
    public HalpbotCustomConstructorContext {}
}
