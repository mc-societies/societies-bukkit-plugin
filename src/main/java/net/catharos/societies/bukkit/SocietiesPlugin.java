package net.catharos.societies.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

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
                libraries = "http://dev.bukkit.org/media/files/831/853/societies-bukkit-plugin-libraries.jar";
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

        loader = new SocietiesLoader(this);
        loader.onEnable();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return loader.onCommand(sender, command, label, args);
    }


    @Override
    public void onDisable() {
        loader.onDisable();
    }
}
