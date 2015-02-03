package org.societies.bukkit.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
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

/**
 * Represents a JoinListener
 */
public class JoinListener implements Listener {

    private final MemberProvider memberProvider;
    private final ListeningExecutorService service;
    private final MemberCache memberCache;
    private final GroupCache groupCache;

    @InjectLogger
    private Logger logger;

    @Inject
    public JoinListener(MemberProvider memberProvider, ListeningExecutorService service, MemberCache memberCache, GroupCache groupCache) {
        this.memberProvider = memberProvider;
        this.service = service;
        this.memberCache = memberCache;
        this.groupCache = groupCache;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerLoginEvent event) {
        ListenableFuture<?> future = service.submit(new Runnable() {
            @Override
            public void run() {
                Member member = memberProvider.getMember(event.getPlayer().getUniqueId());
                member.activate();
            }
        });

        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.catching(throwable);
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
