package org.societies.bukkit.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.shank.config.ConfigSetting;
import org.shank.logging.InjectLogger;
import org.societies.bridge.Location;
import org.societies.groups.group.Group;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;
import org.societies.groups.setting.Setting;

/**
 * Represents a SpawnListener
 */
class SpawnListener implements Listener {

    private final boolean respawnHome;
    private final MemberProvider memberProvider;
    private final ListeningExecutorService service;
    private final Setting<Location> homeSetting;

    @InjectLogger
    private Logger logger;

    @Inject
    public SpawnListener(@ConfigSetting("home.replace-spawn") boolean respawnHome,
                         MemberProvider memberProvider,
                         ListeningExecutorService service,
                         @Named("home") Setting<Location> homeSetting) {

        this.respawnHome = respawnHome;
        this.memberProvider = memberProvider;
        this.service = service;
        this.homeSetting = homeSetting;
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (respawnHome) {
            final Player player = event.getPlayer();


            ListenableFuture<?> future = service.submit(new Runnable() {
                @Override
                public void run() {
                    Member result = memberProvider.getMember(player.getUniqueId());

                    Group group = result.getGroup();
                    if (group == null) {
                        return;
                    }

                    Location location = group.get(homeSetting);


                    if (location != null) {
                        result.get(org.societies.bridge.Player.class).teleport(location);
                    }
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
    }
}
