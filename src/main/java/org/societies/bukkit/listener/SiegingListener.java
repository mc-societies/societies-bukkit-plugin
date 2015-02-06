package org.societies.bukkit.listener;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.societies.api.sieging.ActionValidator;
import org.societies.api.sieging.Besieger;
import org.societies.bridge.bukkit.BukkitWorld;
import org.societies.groups.group.Group;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;

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
    public void onBlockBreak(BlockBreakEvent event) {
        Member member = memberProvider.getMember(event.getPlayer().getUniqueId());

        Group group = member.getGroup();

        Besieger besieger = group == null ? null : group.get(Besieger.class);

        if (!actionValidator.canDestroy(besieger, BukkitWorld.toLocation(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }

    }
}
