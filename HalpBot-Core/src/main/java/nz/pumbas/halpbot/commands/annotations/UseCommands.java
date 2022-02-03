package nz.pumbas.halpbot.commands.annotations;

import org.dockbox.hartshorn.core.annotations.activate.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.common.UseDefault;
import nz.pumbas.halpbot.converters.UseConverters;

@UseDefault
@UseConverters
@ServiceActivator(scanPackages = "nz.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCommands
{
}
