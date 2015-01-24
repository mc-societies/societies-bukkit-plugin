package org.societies.bukkit.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.shank.logging.InjectLogger;
import org.societies.groups.cache.GroupCache;
import org.societies.groups.cache.MemberCache;
import org.societies.groups.group.Group;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;

import javax.annotation.Nullable;

/**
 * Represents a JoinListener
 */
public class JoinListener implements Listener {

    private final MemberProvider memberProvider;
    private final MemberCache memberCache;
    private final GroupCache groupCache;

    @InjectLogger
    private Logger logger;

    @Inject
    public JoinListener(MemberProvider memberProvider, MemberCache memberCache, GroupCache groupCache) {
        this.memberProvider = memberProvider;
        this.memberCache = memberCache;
        this.groupCache = groupCache;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerLoginEvent event) {
        ListenableFuture<Member> future = memberProvider.getMember(event.getPlayer().getUniqueId());

        Futures.addCallback(future, new FutureCallback<Member>() {
            @Override
            public void onSuccess(@Nullable Member result) {
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
        Member member = memberCache.clear(event.getPlayer().getUniqueId());

        if (member != null) {

            member.activate();

            Group group = member.getGroup();
            if (group != null) {
                groupCache.clear(member, group);
            }
        }
    }
}
