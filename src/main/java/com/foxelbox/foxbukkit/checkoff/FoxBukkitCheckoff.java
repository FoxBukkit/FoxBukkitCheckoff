/**
 * This file is part of FoxBukkitCheckoff.
 *
 * FoxBukkitCheckoff is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitCheckoff is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitCheckoff.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.checkoff;

import com.foxelbox.dependencies.config.Configuration;
import com.foxelbox.dependencies.redis.RedisManager;
import com.foxelbox.dependencies.threading.SimpleThreadCreator;
import com.foxelbox.foxbukkit.chat.FoxBukkitChat;
import com.foxelbox.foxbukkit.chat.Messages;
import com.foxelbox.foxbukkit.chat.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chat.json.MessageTarget;
import com.foxelbox.foxbukkit.chat.json.UserInfo;
import com.foxelbox.foxbukkit.permissions.FoxBukkitPermissionHandler;
import com.foxelbox.foxbukkit.permissions.FoxBukkitPermissions;
import com.foxelbox.foxbukkit.scoreboard.FoxBukkitScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FoxBukkitCheckoff extends JavaPlugin implements Listener {
    FoxBukkitPermissions permissions;
    FoxBukkitPermissionHandler permissionHandler;
    FoxBukkitScoreboard scoreboardPlugin;
    FoxBukkitChat fbChat;

    LBAdapterBase logBlock;

    Configuration configuration;
    RedisManager redisManager;

    Map<String,String> playerUUIDToName;
    Map<String,String> playerNameToUUID;

    Scoreboard scoreboard;

    Set<UUID> checkOffPlayers = new LinkedHashSet<>();

    void sendXML(Player source, String content) {
        ChatMessageOut chatMessageOut = new ChatMessageOut();
        chatMessageOut.server = null;
        chatMessageOut.from = new UserInfo(source.getUniqueId(), source.getName());
        chatMessageOut.context = UUID.randomUUID();
        chatMessageOut.finalizeContext = true;
        chatMessageOut.contents = content;
        chatMessageOut.to = new MessageTarget(Messages.TargetType.PLAYER, new String[] { source.getUniqueId().toString() });
        fbChat.chatQueueHandler.onMessage(chatMessageOut);
    }

    StringBuilder makeMessageBuilder() {
        return new StringBuilder("<color name=\"dark_purple\">[FBCO]</color> ");
    }

    /**
     * Adds a player to checkoff.
     *
     * @param player the player to add
     * @return true if the player wasn't already on checkoff
     */
    public boolean addCOPlayer(Player player) {
        return addCOPlayer(player.getUniqueId());
    }

    /**
     * Adds a player to checkoff.
     *
     * @param playerName the name of the player to add
     * @return true if the player wasn't already on checkoff
     */
    public boolean addCOPlayer(UUID playerName) {
        if(checkOffPlayers.contains(playerName)) {
            return false;
        }

        checkOffPlayers.add(playerName);
        saveCO();

        refreshCOPlayerOnlineState(playerName);

        return true;
    }

    /**
     * Removes a player from checkoff.
     *
     * @param player the player to remove
     * @return true if the player wasn't already on checkoff
     */
    public boolean removeCOPlayer(Player player) {
        return removeCOPlayer(player.getUniqueId());
    }

    /**
     * Removes a player from checkoff.
     *
     * @param playerName the name of the player to remove
     * @return true if the player wasn't already on checkoff
     */
    public boolean removeCOPlayer(UUID playerName) {
        if (!checkOffPlayers.contains(playerName)) {
            return false;
        }

        checkOffPlayers.remove(playerName);
        saveCO();

        scoreboard.resetScores(getOfflinePlayer(playerName, true));
        scoreboard.resetScores(getOfflinePlayer(playerName, false));

        return true;
    }

    private void saveCO() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(new File(getDataFolder(), "coplayers.txt")));
            UUID[] plys = checkOffPlayers.toArray(new UUID[checkOffPlayers.size()]);
            for(UUID ply : plys) {
                writer.println(ply);
            }
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        if (objective != null) {
            objective.unregister();
        }
        objective = scoreboard.registerNewObjective("checkoff", DUMMY_CRITERION);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Checkov");
        loadCO();
    }

    private void loadCO() {
        checkOffPlayers.clear();

        try {
            final File file = new File(getDataFolder(), "coplayers.txt");
            if (!file.exists()) {
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null) {
                final UUID playerName = UUID.fromString(line);
                checkOffPlayers.add(playerName);
                refreshCOPlayerOnlineState(playerName);
            }
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static final String DUMMY_CRITERION = "dummy";
    private Objective objective = null;

    // CO online status update

    public boolean isPlayerOnline(UUID uuid) {
        Player ply = getServer().getPlayer(uuid);
        return ply != null && ply.isOnline();
    }

    /**
     * Refreshes the player's online status if they're on checkoff.
     *
     * @param playerName the name of the player to refresh
     */
    public void refreshCOPlayerOnlineState(UUID playerName) {
        setCOPlayerOnlineState(playerName, isPlayerOnline(playerName));
    }

    /**
     * Sets the player's online status if they're on checkoff.
     *
     * @param playerName the name of the player to refresh
     * @param online the new online status
     */
    public void setCOPlayerOnlineState(UUID playerName, boolean online) {
        if(!checkOffPlayers.contains(playerName)) {
            return;
        }

        scoreboard.resetScores(getOfflinePlayer(playerName, !online));
        final Score score = objective.getScore(getOfflinePlayer(playerName, online));
        if (online) {
            score.setScore(1);
            score.setScore(0);
        }  else {
            score.setScore(1);
        }
    }

    // CO display

    /**
     * Toggle checkoff display for the specified player
     *
     * @param player the player to toggle for.
     * @return new state
     */
    public boolean toggleDisplayCO(Player player) {
        if (isDisplayingCO(player)) {
            scoreboardPlugin.setPlayerScoreboard(player, null);
            return false;
        }
        else {
            scoreboardPlugin.setPlayerScoreboard(player, scoreboard);
            return true;
        }
    }

    /**
     * Return whether checkoff is display for the specified player
     *
     * @param player the player to query.
     * @return current state
     */
    public boolean isDisplayingCO(Player player) {
        return player.getScoreboard() == scoreboard;
    }

    private String trimLength(String str, int length) {
        if(str.length() <= length) {
            return str;
        }
        return str.substring(0, length);
    }

    private String getOfflinePlayer(UUID uuid, boolean online) {
        String playerName = playerUUIDToName.get(uuid.toString());
        return trimLength((online ? "\u00a72" : "\u00a7c") + playerName, 16);
    }

    private void plyRankSetting(UUID ply, String rank) {
        if(permissionHandler.getImmunityLevel(rank) <= 0) {
            addCOPlayer(ply);
        } else {
            removeCOPlayer(ply);
        }
    }

    @Override
    public void onEnable() {
        configuration = new Configuration(getDataFolder());
        redisManager = new RedisManager(new SimpleThreadCreator(), configuration);

        playerUUIDToName = redisManager.createCachedRedisMap("playerUUIDToName");
        playerNameToUUID = redisManager.createCachedRedisMap("playerNameToUUID");

        Plugin logBlock = getServer().getPluginManager().getPlugin("LogBlock");
        if(logBlock != null) {
            this.logBlock = new LBAdapter(logBlock);
        }
        fbChat = (FoxBukkitChat)getServer().getPluginManager().getPlugin("FoxBukkitChat");

        permissions = (FoxBukkitPermissions)getServer().getPluginManager().getPlugin("FoxBukkitPermissions");
        permissionHandler = permissions.getHandler();

        scoreboardPlugin = (FoxBukkitScoreboard)getServer().getPluginManager().getPlugin("FoxBukkitScoreboard");
        scoreboard = scoreboardPlugin.createScoreboard();

        reload();

        permissionHandler.addRankChangeHandler(new FoxBukkitPermissionHandler.OnRankChange() {
            @Override
            public void rankChanged(UUID uuid, String rank) {
                plyRankSetting(uuid, rank);
            }
        });

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getPluginCommand("co").setExecutor(new COCommand(this));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plyRankSetting(event.getPlayer().getUniqueId(), permissionHandler.getGroup(event.getPlayer()));
        setCOPlayerOnlineState(event.getPlayer().getUniqueId(), true);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        setCOPlayerOnlineState(event.getPlayer().getUniqueId(), false);
    }
}
