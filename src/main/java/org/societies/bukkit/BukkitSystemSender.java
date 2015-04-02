package org.societies.bukkit;

import com.google.inject.Inject;
import order.SystemSender;
import org.bukkit.command.ConsoleCommandSender;
import org.societies.bridge.ChatColor;
import org.societies.groups.dictionary.Dictionary;

import java.text.MessageFormat;

/**
 * Represents a BukkitSender
 */
class BukkitSystemSender extends SystemSender {

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
