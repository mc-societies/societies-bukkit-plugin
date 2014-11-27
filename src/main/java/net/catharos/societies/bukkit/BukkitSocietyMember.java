package net.catharos.societies.bukkit;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import net.catharos.bridge.*;
import net.catharos.bridge.bukkit.BukkitInventory;
import net.catharos.bridge.bukkit.BukkitItemStack;
import net.catharos.bridge.bukkit.BukkitWorld;
import net.catharos.groups.DefaultMember;
import net.catharos.lib.core.command.Command;
import net.catharos.lib.core.command.sender.Sender;
import net.catharos.lib.core.command.sender.SenderHelper;
import net.catharos.lib.core.i18n.Dictionary;
import net.catharos.societies.api.member.SocietyMember;
import net.catharos.societies.member.locale.LocaleProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a SocietyMember
 */
public class BukkitSocietyMember extends DefaultMember implements SocietyMember {

    private final LocaleProvider localeProvider;
    private final Dictionary<String> directory;

    private final Economy economy;
    private final Materials materials;

    @AssistedInject
    public BukkitSocietyMember(Provider<UUID> uuid,
                               LocaleProvider localeProvider,
                               Dictionary<String> directory,
                               Economy economy,
                               Materials materials,
                               Statics statics) {
        this(uuid.get(), localeProvider, directory, economy, statics, materials);
    }

    @Inject
    public BukkitSocietyMember(@Assisted UUID uuid,
                               LocaleProvider localeProvider,
                               Dictionary<String> dictionary,
                               Economy economy,
                               Statics statics,
                               Materials materials) {
        super(uuid, statics);
        this.localeProvider = localeProvider;
        this.directory = dictionary;
        this.economy = economy;
        this.materials = materials;
    }

    @Override
    public void send(String message) {
        Player player = toPlayer();
        if (player == null) {
            return;
        }

        message = directory.getTranslation(message);

        player.sendMessage(message);
    }

    @Override
    @Nullable
    public String getName() {
        return getServer().getOfflinePlayer(getUUID()).getName();
    }

    @Override
    public boolean isAvailable() {
        return getServer().getPlayer(getUUID()) != null;
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
    public <S extends Sender, R> R as(Executor<S, R> executor, Class<S> clazz) {
        return SenderHelper.as(executor, clazz, this);
    }

    @Override
    public boolean hasPermission(Command command) {
        Player player = toPlayer();

        return player != null && (command.getPermission() == null || player.hasPermission(command.getPermission()));
    }

    @Nullable
    public Player toPlayer() {
        return getServer().getPlayer(getUUID());
    }

    public Player toPlayerNotNull() {
        Player player = getServer().getPlayer(getUUID());
        if (player == null) {
            throw new RuntimeException("Player not available!");
        }
        return player;
    }

    public OfflinePlayer toOfflinePlayer() {
        OfflinePlayer player = getServer().getOfflinePlayer(getUUID());
        if (player == null) {
            throw new RuntimeException("Player not available!");
        }
        return player;
    }

    @Override
    public EconomyResponse withdraw(double amount) {
        return economy.withdrawPlayer(toPlayer(), amount);
    }

    @Override
    public EconomyResponse deposit(double amount) {
        return economy.depositPlayer(toPlayer(), amount);
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
        return new BukkitInventory(materials, toPlayerNotNull().getInventory());
    }
}
