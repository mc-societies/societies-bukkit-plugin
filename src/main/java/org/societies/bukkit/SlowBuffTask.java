package org.societies.bukkit;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.societies.api.sieging.City;
import org.societies.api.sieging.Siege;
import org.societies.api.sieging.SiegeController;
import org.societies.bridge.Location;
import org.societies.bridge.bukkit.BukkitWorld;

/**
 * Represents a SlowBuffTask
 */
public class SlowBuffTask implements Runnable {

    private final SiegeController siegeController;

    @Inject
    public SlowBuffTask(SiegeController siegeController) {
        this.siegeController = siegeController;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = BukkitWorld.toLocation(player.getLocation());

            applyBuff(player, location, siegeController.getSiege(location));

            applyBuff(player, location, siegeController.getSiegeByInitiatedLocation(location));
        }
    }

    public void applyBuff(Player player, Location location, Optional<Siege> optional) {
        if (optional.isPresent()) {
            Siege siege = optional.get();

            City city = siege.getCity();
            if (city.getLocation().distance(location) < 5) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 5, 4));
            }
        }
    }
}
