package org.societies.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.societies.bukkit.util.DependencyLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

/**
 * Represents a SocietiesPlugin
 */
public class SocietiesPlugin extends JavaPlugin {

    private SocietiesLoader loader;

    @Override
    public void onEnable() {
        getLogger().info("Loading libraries...");
        File destination = new File(getDataFolder(), "libraries");

        try {

            String libraries = System.getProperty("societies-libraries-url");

            if (libraries == null) {
                libraries = "http://dev.bukkit.org/media/files/836/988/societies-bukkit-plugin-libraries.jar";
            }

            new DependencyLoader(getLogger()).loadDependencies(
                    new URL(libraries),
                    destination
            );
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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
