package org.societies.bukkit;

import com.google.inject.Inject;
import org.societies.api.economy.EconomyParticipant;
import org.societies.bridge.Player;
import org.societies.groups.Extensible;
import org.societies.groups.ExtensionFactory;
import org.societies.groups.ExtensionRoller;

import java.util.UUID;

/**
 * Represents a BukkitExtensionRoller
 */
class BukkitExtensionRoller implements ExtensionRoller {

    private final ExtensionFactory<BukkitSocietiesMember, UUID> bukkitFactory;

    @Inject
    private BukkitExtensionRoller(ExtensionFactory<BukkitSocietiesMember, UUID> bukkitFactory) {
        this.bukkitFactory = bukkitFactory;
    }

    @Override
    public void roll(Extensible extensible) {
        BukkitSocietiesMember bukkit = bukkitFactory.create(extensible.getUUID());
        extensible.add(Player.class, bukkit);
        extensible.add(EconomyParticipant.class, bukkit);
    }
}
