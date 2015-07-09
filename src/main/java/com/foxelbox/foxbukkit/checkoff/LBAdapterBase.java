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

import org.bukkit.command.CommandSender;

public interface LBAdapterBase {
    boolean isChangesListEmptyFor(CommandSender commandSender, String... args);
}
