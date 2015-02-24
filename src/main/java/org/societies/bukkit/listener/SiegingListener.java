package org.societies.bukkit.listener;

import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.societies.api.sieging.ActionValidator;
import org.societies.api.sieging.Besieger;
import org.societies.bridge.bukkit.BukkitWorld;
import org.societies.groups.group.Group;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;

import static org.societies.api.sieging.ActionValidator.Action;

/**
 * Represents a SiegingListener
 */
class SiegingListener implements Listener {

    private final ActionValidator actionValidator;
    private final MemberProvider memberProvider;

    @Inject
    public SiegingListener(ActionValidator actionValidator, MemberProvider memberProvider) {
        this.actionValidator = actionValidator;
        this.memberProvider = memberProvider;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        boolean can = can(Action.DESTROY, event.getPlayer(), block.getLocation());

        if (!can) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        boolean can = can(Action.BUILD, event.getPlayer(), block.getLocation());

        if (!can) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkBlockInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block != null) {
            boolean can = can(Action.INTERACT, event.getPlayer(), block.getLocation());

            if (!can) {
                event.setCancelled(true);
            }
        }
    }

    private boolean can(int action, Player player, Location location) {
        Member member = memberProvider.getMember(player.getUniqueId());

        Group group = member.getGroup();

        Besieger besieger = group == null ? null : group.get(Besieger.class);

        return actionValidator.can(action, besieger, BukkitWorld.toLocation(location));
    }
}
