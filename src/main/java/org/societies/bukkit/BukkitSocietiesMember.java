package org.societies.bukkit;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.catharos.lib.core.command.Command;
import net.catharos.lib.core.command.sender.Sender;
import net.catharos.lib.core.i18n.Dictionary;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.societies.api.economy.EconomyParticipant;
import org.societies.api.economy.EconomyResponse;
import org.societies.bridge.*;
import org.societies.bridge.bukkit.BukkitInventory;
import org.societies.bridge.bukkit.BukkitItemStack;
import org.societies.bridge.bukkit.BukkitWorld;
import org.societies.member.locale.LocaleProvider;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Represents a SocietyMember
 */
public class BukkitSocietiesMember implements EconomyParticipant, org.societies.bridge.Player, Sender {

    private final LocaleProvider localeProvider;
    private final Dictionary<String> directory;

    private final Economy economy;
    private final UUID uuid;
    private final Plugin plugin;

    @Inject
    public BukkitSocietiesMember(@Assisted UUID uuid,
                                 LocaleProvider localeProvider,
                                 Dictionary<String> directory,
                                 Economy economy,
                                 Plugin plugin) {
        this.localeProvider = localeProvider;
        this.directory = directory;
        this.economy = economy;
        this.uuid = uuid;
        this.plugin = plugin;
    }

    @Override
    public void send(String message) {
        Player player = toPlayer();
        if (player == null) {
            return;
        }

        message = directory.getTranslation(message);

        if (message.length() > Short.MAX_VALUE) {
            player.sendMessage("Message way too long!");
            System.out.println(message);
            return;
        }

        player.sendMessage(message);
    }

    @Override
    @Nullable
    public String getName() {
        return getServer().getOfflinePlayer(uuid).getName();
    }

    @Override
    public boolean isAvailable() {
        return getServer().getPlayer(uuid) != null;
    }

    private Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public Locale getLocale() {
        return localeProvider.provide(this);
    }

    @Override
    public boolean hasPermission(String permission) {
        Player player = toPlayer();
        return player != null && player.hasPermission(permission);
    }

    @Override
    public void send(String message, Object... args) {
        Player player = toPlayer();
        if (player == null) {
            return;
        }

        player.sendMessage(directory.getTranslation(message, args));
    }

    @Override
    public void send(StringBuilder message) {
        send(message.toString());
    }

    @Override
    public boolean hasPermission(Command command) {
        Player player = toPlayer();

        return player != null && (command.getPermission() == null || player.hasPermission(command.getPermission()));
    }

    @Nullable
    public Player toPlayer() {
        return getServer().getPlayer(uuid);
    }

    public Player toPlayerNotNull() {
        Player player = getServer().getPlayer(uuid);
        if (player == null) {
            throw new RuntimeException("Player not available!");
        }
        return player;
    }

    public OfflinePlayer toOfflinePlayer() {
        OfflinePlayer player = getServer().getOfflinePlayer(uuid);
        if (player == null) {
            throw new RuntimeException("Player not available!");
        }
        return player;
    }

    @Override
    public EconomyResponse withdraw(final double amount) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, new Callable<EconomyResponse>() {
                @Override
                public EconomyResponse call() throws Exception {
                    net.milkbowl.vault.economy.EconomyResponse response = economy.withdrawPlayer(toPlayer(), amount);
                    return new EconomyResponse(response.amount, response.balance, response.transactionSuccess());
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, false);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, false);
        }
    }

    @Override
    public EconomyResponse deposit(final double amount) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, new Callable<EconomyResponse>() {
                @Override
                public EconomyResponse call() throws Exception {
                    net.milkbowl.vault.economy.EconomyResponse response = economy.depositPlayer(toPlayer(), amount);
                    return new EconomyResponse(response.amount, response.balance, response.transactionSuccess());
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, false);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, false);
        }
    }

    @Override
    public double getHealth() {
        Player player = toPlayer();
        if (player == null) {
            throw new RuntimeException("Player not online!");
        }

        return player.getHealth();
    }

    @Override
    public int getFoodLevel() {
        Player player = toPlayerNotNull();
        return player.getFoodLevel();
    }

    @Nullable
    @Override
    public Location getLocation() {
        Player player = toPlayerNotNull();
        return BukkitWorld.toLocation(player.getLocation());
    }

    @Override
    public World getWorld() {
        return new BukkitWorld(toPlayerNotNull().getWorld());
    }

    @Override
    public boolean teleport(Location location) {
        if (location instanceof Location.InvalidLocation) {
            throw new RuntimeException("Invalid location!");
        }
        return toPlayerNotNull().teleport(BukkitWorld.toBukkitLocation(getServer(), location));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendBlockChange(Location location, Material material, byte b) {
        toPlayerNotNull().sendBlockChange(BukkitWorld.toBukkitLocation(getServer(), location), BukkitItemStack
                .toBukkitMaterial(material), b);
    }

    @Override
    public Inventory getInventory() {
        return new BukkitInventory(toPlayerNotNull().getInventory());
    }
}
