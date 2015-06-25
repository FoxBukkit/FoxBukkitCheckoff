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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class COCommand implements CommandExecutor {
    private final FoxBukkitCheckoff checkoff;

    public COCommand(FoxBukkitCheckoff checkoff) {
        this.checkoff = checkoff;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String cmdStr, String[] args) {
        Player ply = (Player)commandSender;

        String primaryArg;
        if(args.length < 1) {
            primaryArg = "toggle";
        } else {
            primaryArg = args[0].toLowerCase();
        }

        switch (primaryArg) {
            case "on":
                if(!checkoff.isDisplayingCO(ply))
                    checkoff.toggleDisplayCO(ply);
                break;
            case "off":
                if(checkoff.isDisplayingCO(ply))
                    checkoff.toggleDisplayCO(ply);
                break;
            case "toggle":
                checkoff.toggleDisplayCO(ply);
                break;
            default:
                boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
                String str = checkoff.playerNameToUUID.get(primaryArg);
                if(str == null) {
                    ply.sendMessage(checkoff.makeMessageBuilder().append("No idea who that is...").toString());
                    return true;
                }
                UUID uuid = UUID.fromString(str);
                Player targetPly = checkoff.getServer().getPlayer(uuid);
                if(targetPly != null && targetPly.isOnline() && !force) {
                    ply.sendMessage(checkoff.makeMessageBuilder().append("Player is online. Use with force (/co player force) to enforce removal").toString());
                    return true;
                }
                checkoff.removeCOPlayer(uuid);
                ply.sendMessage(checkoff.makeMessageBuilder().append("Player removed from CO").toString());
                break;
        }

        return true;
    }
}
