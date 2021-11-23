package nz.pumbas.halpbot.commands.servicescanners;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServiceOrder;
import org.dockbox.hartshorn.core.services.ServiceProcessor;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.ReflectiveCommand;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;

public class CommandServiceScanner implements ServiceProcessor<UseCommands>
{
    @Inject
    private CommandAdapter commandAdapter;

    @Override
    public ServiceOrder order() {
        return ServiceOrder.LATE;
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return !type.methods(Command.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        T instance = context.get(type);

        int messageCommands = 0;
        int slashCommands = 0;
        int reflectiveCommands = 0;

        for (MethodContext<?, T> methodContext : type.methods(Command.class)) {
            if (methodContext.annotation(SlashCommand.class).present()) {
                slashCommands++;
                this.commandAdapter.registerSlashCommand(instance, methodContext);
            }
            else if (methodContext.annotation(ReflectiveCommand.class).present()) {
                reflectiveCommands++;
                this.commandAdapter.registerReflectiveCommand(methodContext);
            }
            else {
                messageCommands++;
                this.commandAdapter.registerMessageCommand(instance, methodContext);
            }
        }

        context.log().info("Commands found in %s - Message: %d; Slash: %d; Reflective: %d"
                .formatted(type.qualifiedName(), messageCommands, slashCommands, reflectiveCommands));
    }
}
