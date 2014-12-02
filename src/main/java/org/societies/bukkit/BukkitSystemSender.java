package org.societies.bukkit;

import com.google.inject.Inject;
import net.catharos.lib.core.command.SystemSender;
import net.catharos.lib.core.i18n.Dictionary;
import org.bukkit.command.ConsoleCommandSender;
import org.societies.bridge.ChatColor;

import java.text.MessageFormat;

/**
 * Represents a BukkitSender
 */
public class BukkitSystemSender extends SystemSender {

    private final ConsoleCommandSender sender;
    private final Dictionary<String> dictionary;

    @Inject
    public BukkitSystemSender(ConsoleCommandSender sender, Dictionary<String> dictionary) {
        this.dictionary = dictionary;
        this.sender = sender;
    }

    @Override
    public void send(String message) {
        sender.sendMessage(dictionary.getTranslation(message));
    }


    @Override
    public void send(String message, Object... args) {
        ChatColor.argumentColorReset(args);
        sender.sendMessage(MessageFormat.format(dictionary.getTranslation(message), args));
    }
}