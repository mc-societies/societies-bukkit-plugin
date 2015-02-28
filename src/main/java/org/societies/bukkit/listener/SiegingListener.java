package org.societies.bukkit.listener;

import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.societies.api.sieging.*;
import org.societies.bridge.bukkit.BukkitWorld;
import org.societies.groups.group.Group;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;

import java.util.Set;

import static org.societies.api.sieging.ActionValidator.Action;
import static org.societies.bridge.bukkit.BukkitWorld.toBukkitLocation;
import static org.societies.bridge.bukkit.BukkitWorld.toLocation;

/**
 * Represents a SiegingListener
 */
class SiegingListener implements Listener {

    private final ActionValidator actionValidator;
    private final MemberProvider memberProvider;
    private final CityProvider cityProvider;
    private final SiegeController siegeController;

    private final Server server;

    @Inject
    public SiegingListener(ActionValidator actionValidator, MemberProvider memberProvider, CityProvider cityProvider, SiegeController siegeController, Server server) {
        this.actionValidator = actionValidator;
        this.memberProvider = memberProvider;
        this.cityProvider = cityProvider;
        this.siegeController = siegeController;
        this.server = server;
    }

    private Besieger getBesieger(Player player) {
        Member member = memberProvider.getMember(player.getUniqueId());
        Group group = member.getGroup();

        return group == null ? null : group.get(Besieger.class);
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
        Besieger besieger = getBesieger(player);

        return actionValidator.can(action, besieger, toLocation(location));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void checkBindstoneBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        Besieger besieger = getBesieger(event.getPlayer());

        if (besieger == null) {
            return;
        }

        City city = cityProvider.getCity(toLocation(blockLocation));

        if (city == null) {
            return;
        }

        org.societies.bridge.Location bindstone = city.getLocation();

        if (bindstone.getRoundedX() == blockLocation.getBlockX()
                && bindstone.getRoundedY() == blockLocation.getBlockY()
                && bindstone.getRoundedZ() == blockLocation.getBlockZ()) {

            Set<Siege> sieges = siegeController.getSieges(city);

            for (Siege siege : sieges) {
                if (siege.isStarted() && siege.getBesieger().equals(besieger)) {
                    siegeController.stop(siege, besieger);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkSiegestoneBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        Besieger besieger = getBesieger(event.getPlayer());

        if (besieger == null) {
            return;
        }

        Siege siege = siegeController.getSiege(BukkitWorld.toLocation(blockLocation));

        if (!siege.getBesieger().equals(besieger)) {
            event.setCancelled(true);
            return;
        }

        org.societies.bridge.Location bindstone = siege.getLocationInitiated();

        if (bindstone.getRoundedX() == blockLocation.getBlockX()
                && bindstone.getRoundedY() == blockLocation.getBlockY()
                && bindstone.getRoundedZ() == blockLocation.getBlockZ()) {

            siegeController.stop(siege, siege.getCity().getOwner());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void modifyPlayerRespawn(PlayerRespawnEvent event) {
        Besieger besieger = getBesieger(event.getPlayer());

        if (besieger != null) {
            Siege siege = siegeController.getSiegeByAttacker(besieger);

            if (siege.isStarted()) {
                event.setRespawnLocation(toBukkitLocation(server, siege.getLocationInitiated()));
            }
        }
    }
}
