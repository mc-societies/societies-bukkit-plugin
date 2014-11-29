package net.catharos.societies.bukkit;

import com.google.inject.Inject;
import net.catharos.bridge.Materials;
import net.catharos.groups.*;
import net.catharos.groups.setting.subject.DefaultSubject;
import net.catharos.lib.core.i18n.Dictionary;
import net.catharos.societies.member.locale.LocaleProvider;
import net.milkbowl.vault.economy.Economy;

import java.util.UUID;

/**
 * Represents a BukkitMemberFactory
 */
public class BukkitMemberFactory implements MemberFactory {
    private final LocaleProvider localeProvider;
    private final Dictionary<String> directory;
    private final Economy economy;
    private final Materials materials;
    private final DefaultMember.Statics statics;

    @Inject
    public BukkitMemberFactory(LocaleProvider localeProvider, Dictionary<String> directory, Economy economy, Materials materials, DefaultMember.Statics statics) {
        this.localeProvider = localeProvider;
        this.directory = directory;
        this.economy = economy;
        this.materials = materials;
        this.statics = statics;
    }

    @Override
    public Member create(UUID uuid) {
        BukkitSocietyMember bukkit = new BukkitSocietyMember(localeProvider, directory, economy, materials, uuid);

        DefaultMember member = new DefaultMember(uuid, new DefaultSubject(uuid), new DefaultParticipant(bukkit), bukkit);

        member.setMemberHeart(new DefaultMemberHeart(statics, member));

        return member;
    }
}
