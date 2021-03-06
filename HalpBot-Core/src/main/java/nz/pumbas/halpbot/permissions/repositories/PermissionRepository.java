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

package nz.pumbas.halpbot.permissions.repositories;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.data.annotations.Query;
import org.dockbox.hartshorn.data.jpa.JpaRepository;

import java.util.List;
import java.util.Set;

@Service
public abstract class PermissionRepository implements JpaRepository<GuildPermission, GuildPermissionId>
{
    @Query("SELECT gp FROM GuildPermission gp WHERE gp.guildId = :guildId")
    public abstract List<GuildPermission> guildPermissions(long guildId);

    @Query("SELECT gp.permission FROM GuildPermission gp WHERE gp.guildId = :guildId AND gp.roleId IN :roleIds")
    public abstract Set<String> permissions(long guildId, Set<Long> roleIds);

    @Query("SELECT COUNT(*) FROM GuildPermissions gp WHERE gp.guildId = :guildId AND gp.roleId = :roleId")
    public abstract Long countPermissionsWithRole(long guildId, long roleId);

    @Query("SELECT COUNT(*) FROM GuildPermissions gp WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract Long countPermissions(long guildId, String permission);
}
