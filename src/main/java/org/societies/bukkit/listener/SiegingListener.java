package org.societies.bukkit.listener;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.societies.api.sieging.ActionValidator;
import org.societies.api.sieging.Besieger;
import org.societies.api.sieging.BesiegerProvider;
import org.societies.bridge.bukkit.BukkitWorld;

/**
 * Represents a SiegingListener
 */
class SiegingListener implements Listener {

    private final ActionValidator actionValidator;
    private final BesiegerProvider besiegerProvider;

    @Inject
    public SiegingListener(ActionValidator actionValidator, BesiegerProvider besiegerProvider) {
        this.actionValidator = actionValidator;
        this.besiegerProvider = besiegerProvider;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Besieger besieger = besiegerProvider.getBesieger(event.getPlayer().getUniqueId());


        if (!actionValidator.canDestroy(besieger, BukkitWorld.toLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }

    }
}
