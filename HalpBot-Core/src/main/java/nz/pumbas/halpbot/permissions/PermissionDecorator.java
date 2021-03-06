/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.utilities.Require;

@ComponentBinding(PermissionDecorator.class)
public class PermissionDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C> implements Enableable
{
    @Inject
    @Getter
    private PermissionService permissionService;
    @Getter
    private final Set<String> customPermissions = new HashSet<>();
    @Getter
    private final Set<Permission> userPermissions = new HashSet<>();
    @Getter
    private final Set<Permission> selfPermissions = new HashSet<>();
    @Getter
    private final Require require;
    @Getter
    private final BiPredicate<Guild, Member> hasPermissions;

    @Bound
    public PermissionDecorator(ActionInvokable<C> actionInvokable, Permissions permissions) {
        super(actionInvokable);
        this.customPermissions.addAll(Set.of(permissions.permissions()));
        this.userPermissions.addAll(Set.of(permissions.user()));
        this.selfPermissions.addAll(Set.of(permissions.self()));
        this.require = permissions.merger();
        this.hasPermissions = switch (this.require) {
            case ALL -> this::all;
            case ANY -> this::any;
        };
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        HalpbotEvent event = invocationContext.halpbotEvent();
        Guild guild = event.guild();
        Member member = event.member();

        if (guild == null || member == null)
            return Exceptional.of(new ExplainedException("This cannot be used in a private message!"));
        if (!this.botHasPermissions(guild))
            return Exceptional.of(new ExplainedException("The bot doesn't have sufficient permissions to execute this action"));
        if (this.hasPermissions(guild, member))
            return super.invoke(invocationContext);
        return Exceptional.of(new ExplainedException("You do not have permission to use this command"));
    }

    protected boolean botHasPermissions(Guild guild) {
        return guild.getSelfMember().hasPermission(this.selfPermissions);
    }

    protected boolean hasPermissions(Guild guild, Member member) {
        // The bot owner has permission to use any command
        return this.permissionService.isOwner(member) || this.hasPermissions.test(guild, member);
    }

    protected boolean any(Guild guild, Member member) {
        for (Permission permission : this.userPermissions()) {
            if (member.hasPermission(permission))
                return true;
        }
        for (String permission : this.customPermissions()) {
            if (this.permissionService().hasPermission(guild, member, permission))
                return true;
        }
        return false;
    }

    protected boolean all(Guild guild, Member member) {
        return member.hasPermission(this.userPermissions()) &&
            this.permissionService().hasPermissions(guild, member, this.customPermissions());
    }

    @Override
    public void enable() {
        this.permissionService.addPermissions(this.customPermissions);
    }
}
