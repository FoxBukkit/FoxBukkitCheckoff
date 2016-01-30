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

import com.foxelbox.foxbukkit.chat.MessageHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

public class COCommand implements CommandExecutor {
    private final FoxBukkitCheckoff checkoff;

    private String getButtonsForPlayer(UUID uuid, String playerName) {
        String buttons = MessageHelper.button("/lb player " + playerName + " sum blocks", "lb", "blue", true) + " "
                + MessageHelper.button("/lb player " + playerName + " chestaccess coords", "chest", "blue", true);
        if (checkoff.isPlayerOnline(uuid)) {
            buttons = MessageHelper.button("/at 0 /vanish on;/tp -sn \"" + playerName + '"', "tp", "blue", true) + " " + buttons;
        } else {
            buttons = MessageHelper.button("/co " + playerName, "x", "red", true) + " " + buttons;
        }
        buttons += " " + MessageHelper.button("/settag \"" + playerName + "\" $4* ", "TDRed", "blue", true)
                + " " + MessageHelper.button("/settag \"" + playerName + "\" $c* ", "TLRed", "blue", true)
                + " " + MessageHelper.button("/settag \"" + playerName + "\" $a* ", "TGreen", "blue", true)
                + " " + MessageHelper.button("/settag \"" + playerName + "\" none", "TNone", "blue", true);
        return buttons;
    }

    public COCommand(FoxBukkitCheckoff checkoff) {
        this.checkoff = checkoff;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, Command command, String cmdStr, String[] args) {
        final Player ply = (Player)commandSender;

        String primaryArg;
        if(args.length < 1) {
            primaryArg = "toggle";
        } else {
            primaryArg = args[0].toLowerCase();
        }

        switch (primaryArg) {
            case "on":
                if (!checkoff.isDisplayingCO(ply))
                    checkoff.toggleDisplayCO(ply);
                break;
            case "off":
                if (checkoff.isDisplayingCO(ply))
                    checkoff.toggleDisplayCO(ply);
                break;
            case "list":
                for (UUID uuid : checkoff.checkOffPlayers) {
                    final String playerName = checkoff.playerUUIDToName.get(uuid.toString());
                    checkoff.sendXML(ply, checkoff.makeMessageBuilder()
                            .append(checkoff.isPlayerOnline(uuid) ? "\u00a72" : "\u00a74")
                            .append(playerName)
                            .append(' ')
                            .append(getButtonsForPlayer(uuid, playerName))
                            .toString()
                    );
                }
                break;
            case "reload":
                checkoff.reload();
                break;
            case "empty":
                if(checkoff.logBlock == null) {
                    checkoff.sendXML(ply, checkoff.makeMessageBuilder().append("No LogBlock!").toString());
                    return true;
                }

                final HashSet<UUID> offlinePlayerUUIDs = new HashSet<>();
                for (UUID playerName : checkoff.checkOffPlayers) {
                    if (!checkoff.isPlayerOnline(playerName)) {
                        offlinePlayerUUIDs.add(playerName);
                    }
                }
                new Thread() {
                    public void run() {
                        final Iterator<UUID> it = offlinePlayerUUIDs.iterator();
                        while (it.hasNext()) {
                            final UUID uuid = it.next();
                            final String name = checkoff.playerUUIDToName.get(uuid.toString());
                            if (!checkoff.logBlock.isChangesListEmptyFor(ply, "player", name, "world", "world") || !checkoff.logBlock.isChangesListEmptyFor(ply, "player", name, "world", "world", "chestaccess")) {
                                it.remove();
                            }
                        }
                        if(!offlinePlayerUUIDs.isEmpty()) {
                            checkoff.getServer().getScheduler().scheduleSyncDelayedTask(checkoff, new Runnable() {
                                @Override
                                public void run() {
                                    for (final UUID uuid : offlinePlayerUUIDs) {
                                        checkoff.removeCOPlayer(uuid);
                                    }
                                    checkoff.sendXML(ply, checkoff.makeMessageBuilder().append("Removed empty changelists").toString());
                                }
                            });
                        } else {
                            checkoff.sendXML(ply, checkoff.makeMessageBuilder().append("No empty changelists").toString());
                        }
                    }
                }.start();
                break;
            case "toggle":
                checkoff.toggleDisplayCO(ply);
                break;
            default:
                boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
                String str = checkoff.playerNameToUUID.get(primaryArg);
                if (str == null) {
                    checkoff.sendXML(ply, checkoff.makeMessageBuilder().append("No idea who that is...").toString());
                    return true;
                }
                UUID uuid = UUID.fromString(str);
                Player targetPly = checkoff.getServer().getPlayer(uuid);
                if (targetPly != null && targetPly.isOnline() && !force) {
                    checkoff.sendXML(ply, checkoff.makeMessageBuilder().append("Player is online. Use with force (/co player force) to enforce removal").toString());
                    return true;
                }
                checkoff.removeCOPlayer(uuid);
                checkoff.sendXML(ply, checkoff.makeMessageBuilder().append("Player removed from CO").toString());
                break;
        }

        return true;
    }
}
