package org.societies.bukkit;

import com.google.inject.Inject;
import org.societies.api.economy.EconomyParticipant;
import org.societies.bridge.Player;
import org.societies.groups.ExtensionFactory;
import org.societies.groups.ExtensionRoller;
import org.societies.groups.member.Member;

import java.util.UUID;

/**
 * Represents a BukkitExtensionRoller
 */
class BridgeExtensionRoller implements ExtensionRoller<Member> {

    private final ExtensionFactory<BukkitSocietiesMember, UUID> bukkitFactory;

    @Inject
    private BridgeExtensionRoller(ExtensionFactory<BukkitSocietiesMember, UUID> bukkitFactory) {
        this.bukkitFactory = bukkitFactory;
    }

    @Override
    public void roll(Member extensible) {
        BukkitSocietiesMember bukkit = bukkitFactory.create(extensible.getUUID());
        extensible.add(Player.class, bukkit);
        extensible.add(EconomyParticipant.class, bukkit);
    }
}
