package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecorator;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.commands.actioninvokable.CommandInvokable;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.decorators.log.Log;
import nz.pumbas.halpbot.decorators.log.LogDecorator;
import nz.pumbas.halpbot.permissions.Permission;
import nz.pumbas.halpbot.permissions.PermissionDecorator;
import nz.pumbas.halpbot.utilities.Duration;


@Cooldown(duration = @Duration(10))
@Permission("halpbot.example.class")
@UseCommands
@Service
@HartshornTest
public class DecoratorServiceTests
{
    @Inject private CommandAdapter commandAdapter;
    @Inject private DecoratorService decoratorService;

    @Log
    @Cooldown
    @Permission("halpbot.example.action")
    @Command(alias = "decoratedCommandTest")
    public void decoratedCommandTestMethod() {
    }

    @Command(alias = "undecoratedCommandTest")
    public void undecoratedCommandTestMethod() {
    }

    @Test
    public void usesActionDecoratorsUnlessMergeSpecifiesOtherwise() {
        CommandContext commandContext = this.commandAdapter.commandContext("decoratedCommandTest");
        Assertions.assertNotNull(commandContext);

        ActionInvokable<?> actionInvokable = commandContext.actionInvokable();
        Assertions.assertEquals(4, this.decoratorService.depth(actionInvokable));
        Assertions.assertInstanceOf(PermissionDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(PermissionDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CooldownDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(LogDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CommandInvokable.class, actionInvokable);

        Assertions.assertEquals(actionInvokable, this.decoratorService.root(commandContext.actionInvokable()));
    }

    @Test
    public void usesClassDecorators() {
        CommandContext commandContext = this.commandAdapter.commandContext("undecoratedCommandTest");
        Assertions.assertNotNull(commandContext);

        ActionInvokable<?> actionInvokable = commandContext.actionInvokable();
        Assertions.assertEquals(2, this.decoratorService.depth(actionInvokable));
        Assertions.assertInstanceOf(PermissionDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CooldownDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CommandInvokable.class, actionInvokable);

        Assertions.assertEquals(actionInvokable, this.decoratorService.root(commandContext.actionInvokable()));
    }
}