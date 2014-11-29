package net.catharos.societies.bukkit.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.catharos.bridge.Location;
import net.catharos.groups.Group;
import net.catharos.groups.Member;
import net.catharos.groups.MemberProvider;
import net.catharos.groups.setting.Setting;
import net.catharos.lib.shank.logging.InjectLogger;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Represents a SpawnListener
 */
public class SpawnListener implements Listener {

    private final boolean respawnHome;
    private final MemberProvider memberProvider;
    private final Setting<Location> homeSetting;

    @InjectLogger
    private Logger logger;


    @Inject
    public SpawnListener(@Named("society.home.replace-spawn") boolean respawnHome,
                         MemberProvider memberProvider,
                         @Named("home") Setting<Location> homeSetting) {

        this.respawnHome = respawnHome;
        this.memberProvider = memberProvider;
        this.homeSetting = homeSetting;
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (respawnHome) {
            Player player = event.getPlayer();

            ListenableFuture<Member> member = memberProvider.getMember(player.getUniqueId());

            Futures.addCallback(member, new FutureCallback<Member>() {
                @Override
                public void onSuccess(Member result) {
                    Group group = result.getGroup();
                    if (group == null) {
                        return;
                    }

                    Location location = group.get(homeSetting);


                    if (location != null) {
                        result.get(net.catharos.bridge.Player.class).teleport(location);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.catching(t);
                }
            });
        }
    }
}
