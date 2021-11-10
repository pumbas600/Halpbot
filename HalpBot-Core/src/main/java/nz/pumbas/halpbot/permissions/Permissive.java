package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Permissive
{
    default ApplicationContext applicationContext() {
        return null; //TODO: Implement in subclasses and remove default method
    }

    List<String> permissions();

    default boolean hasPermission(@NotNull User user) {
        return this.hasPermission(user.getIdLong());
    }

    default boolean hasPermission(long userId) {
        return this.permissions().isEmpty() ||
            this.applicationContext()
                .get(PermissionManager.class)
                .hasPermissions(userId, this.permissions());
    }
}