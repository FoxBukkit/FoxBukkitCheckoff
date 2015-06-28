package com.foxelbox.foxbukkit.checkoff;

import org.bukkit.command.CommandSender;

public interface LBAdapterBase {
    boolean isChangesListEmptyFor(CommandSender commandSender, String... args);
}
