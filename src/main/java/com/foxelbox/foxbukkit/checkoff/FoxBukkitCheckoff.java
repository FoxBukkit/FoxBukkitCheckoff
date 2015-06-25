/**
 * This file is part of FoxBukkitScoreboard.
 *
 * FoxBukkitScoreboard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitScoreboard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitScoreboard.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.checkoff;

import com.foxelbox.dependencies.config.Configuration;
import com.foxelbox.dependencies.redis.CacheMap;
import com.foxelbox.dependencies.redis.RedisManager;
import com.foxelbox.dependencies.threading.SimpleThreadCreator;
import com.foxelbox.foxbukkit.permissions.FoxBukkitPermissionHandler;
import com.foxelbox.foxbukkit.permissions.FoxBukkitPermissions;
import com.foxelbox.foxbukkit.scoreboard.FoxBukkitScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class FoxBukkitCheckoff extends JavaPlugin implements Listener {
    private FoxBukkitPermissions permissions;
    private FoxBukkitPermissionHandler permissionHandler;
    private FoxBukkitScoreboard scoreboardPlugin;

    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        permissions = (FoxBukkitPermissions)getServer().getPluginManager().getPlugin("FoxBukkitPermissions");
        permissionHandler = permissions.getHandler();

        scoreboardPlugin = (FoxBukkitScoreboard)getServer().getPluginManager().getPlugin("FoxBukkitScoreboard");
        scoreboard = scoreboardPlugin.createScoreboard();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

    }
}
