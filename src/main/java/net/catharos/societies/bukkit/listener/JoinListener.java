package net.catharos.societies.bukkit.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import net.catharos.groups.Group;
import net.catharos.groups.MemberProvider;
import net.catharos.lib.shank.logging.InjectLogger;
import net.catharos.societies.api.member.SocietyMember;
import net.catharos.societies.group.OnlineGroupCache;
import net.catharos.societies.member.OnlineMemberCache;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;

/**
 * Represents a JoinListener
 */
public class JoinListener implements Listener {

    private final MemberProvider<SocietyMember> memberProvider;
    private final OnlineMemberCache<SocietyMember> memberCache;
    private final OnlineGroupCache groupCache;

    @InjectLogger
    private Logger logger;

    @Inject
    public JoinListener(MemberProvider<SocietyMember> memberProvider, OnlineMemberCache<SocietyMember> memberCache, OnlineGroupCache groupCache) {
        this.memberProvider = memberProvider;
        this.memberCache = memberCache;
        this.groupCache = groupCache;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerLoginEvent event) {
        ListenableFuture<SocietyMember> future = memberProvider.getMember(event.getPlayer().getUniqueId());

        Futures.addCallback(future, new FutureCallback<SocietyMember>() {
            @Override
            public void onSuccess(@Nullable SocietyMember result) {
                if (result != null) {
                    result.activate();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                logger.catching(t);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SocietyMember member = memberCache.clear(event.getPlayer().getUniqueId());


        if (member != null) {

            member.activate();

            Group group = member.getGroup();
            if (group != null) {
                groupCache.clear(member, group);
            }
        }
    }
}
