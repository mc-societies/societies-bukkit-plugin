package org.societies.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Represents a SocietiesPlugin
 */
public class SocietiesPlugin extends JavaPlugin {

    private SocietiesLoader loader;

    @Override
    public void onEnable() {
        loader = new SocietiesLoader(getClassLoader(), this);
        loader.onEnable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return loader != null && loader.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (loader == null) {
            return null;
        }
        return loader.onTabComplete(sender, command, alias, args);
    }

    @Override
    public void onDisable() {
        if (loader == null) {
            return;
        }
        loader.onDisable();
    }
}
