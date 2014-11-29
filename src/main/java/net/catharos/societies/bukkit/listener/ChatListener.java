package net.catharos.societies.bukkit.listener;

import com.google.inject.Inject;
import net.catharos.groups.Group;
import net.catharos.groups.Member;
import net.catharos.groups.MemberProvider;
import net.catharos.lib.shank.config.ConfigSetting;
import net.catharos.lib.shank.logging.InjectLogger;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.ExecutionException;

/**
 * Represents a ChatListener
 */
public class ChatListener implements Listener {

    private final boolean integration;
    private final MemberProvider provider;

    @InjectLogger
    private Logger logger;

    @Inject
    public ChatListener(@ConfigSetting("chat.integration") boolean integration, MemberProvider provider) {
        this.integration = integration;
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!integration) {
            return;
        }

        Member member;
        try {
            member = provider.getMember(event.getPlayer().getUniqueId()).get();
        } catch (InterruptedException e) {
            logger.catching(e);
            return;
        } catch (ExecutionException e) {
            logger.catching(e);
            return;
        }

        String format = event.getFormat();
        Group group = member.getGroup();

        String tag = group == null ? "" : group.getTag();
        String name = group == null ? "" : group.getName();
        String uuid = group == null ? "" : group.getUUID().toString();

        format = format.replace("{group-tag}", tag);
        format = format.replace("{group-name}", name);
        format = format.replace("{group-uuid}", uuid);

        event.setFormat(format);
    }
}
