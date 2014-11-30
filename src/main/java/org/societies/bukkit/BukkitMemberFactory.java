package org.societies.bukkit;

import com.google.inject.Inject;
import net.catharos.lib.core.i18n.Dictionary;
import net.milkbowl.vault.economy.Economy;
import org.societies.api.economy.EconomyParticipant;
import org.societies.bridge.Materials;
import org.societies.bridge.Player;
import org.societies.groups.ExtensionFactory;
import org.societies.groups.member.*;
import org.societies.groups.request.DefaultParticipant;
import org.societies.groups.setting.subject.DefaultSubject;
import org.societies.member.locale.LocaleProvider;

import java.util.UUID;

/**
 * Represents a BukkitMemberFactory
 */
public class BukkitMemberFactory implements MemberFactory {
    private final LocaleProvider localeProvider;
    private final Dictionary<String> directory;
    private final Economy economy;
    private final Materials materials;
    private final DefaultMemberHeart.Statics statics;
    private final ExtensionFactory<MemberHeart, Member> extensionFactory;

    @Inject
    public BukkitMemberFactory(LocaleProvider localeProvider, Dictionary<String> directory, Economy economy, Materials materials, DefaultMemberHeart.Statics statics, ExtensionFactory<MemberHeart, Member> extensionFactory) {
        this.localeProvider = localeProvider;
        this.directory = directory;
        this.economy = economy;
        this.materials = materials;
        this.statics = statics;
        this.extensionFactory = extensionFactory;
    }

    @Override
    public Member create(final UUID uuid) {
        BukkitSocieties bukkit = new BukkitSocieties(localeProvider, directory, economy, materials, uuid);

        CompoundMember member = new CompoundMember(uuid, new DefaultSubject() {
            @Override
            public UUID getUUID() {
                return uuid;
            }
        }, new DefaultParticipant(bukkit), bukkit);

        member.setMemberHeart(extensionFactory.create(member));

        member.add(Player.class, bukkit);
        member.add(EconomyParticipant.class, bukkit);

        return member;
    }
}
