package com.foxelbox.foxbukkit.checkoff;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class LBAdapter implements LBAdapterBase {
    private final LogBlock logBlock;

    LBAdapter(Plugin logBlock) {
        this.logBlock = (LogBlock)logBlock;
    }

    public boolean isChangesListEmptyFor(CommandSender commandSender, String... args) {
        try {
            return logBlock.getBlockChanges(new QueryParams(logBlock, commandSender, Arrays.asList(args))).isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
