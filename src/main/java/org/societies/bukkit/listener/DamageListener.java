package org.societies.bukkit.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;
import org.shank.config.ConfigSetting;
import org.societies.groups.Relation;
import org.societies.groups.group.Group;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;
import org.societies.groups.setting.Setting;

import java.util.List;

/**
 * Represents a EntityListener
 */
class DamageListener implements Listener {

    private final MemberProvider provider;
    private final List<String> disabledWorlds;
    private final boolean globalFFForced;
    private final boolean saveCivilians;
    private final Setting<Boolean> personalFF;
    private final Setting<Boolean> groupFF;

    @Inject
    public DamageListener(MemberProvider provider,
                          @ConfigSetting("blacklisted-worlds") List<String> disabledWorlds,
                          @ConfigSetting("pvp.global-ff-forced") boolean globalFFForced,
                          @ConfigSetting("pvp.save-civilians") boolean saveCivilians,
                          @Named("personal-friendly-fire") Setting<Boolean> personalFF,
                          @Named("group-friendly-fire") Setting<Boolean> groupFF) {
        this.provider = provider;
        this.disabledWorlds = disabledWorlds;
        this.globalFFForced = globalFFForced;
        this.saveCivilians = saveCivilians;

        this.personalFF = personalFF;
        this.groupFF = groupFF;
    }

    @Nullable
    public Player findAttacker(EntityDamageByEntityEvent event) {
        Player attackerPlayer = null;
        Entity attackerEntity = event.getDamager();

        if (attackerEntity instanceof Player) {
            attackerPlayer = (Player) attackerEntity;
        } else if (attackerEntity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) attackerEntity).getShooter();

            if (shooter instanceof Player) {
                attackerPlayer = (Player) shooter;
            }
        }

        return attackerPlayer;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victimEntity = event.getEntity();

        if (!(victimEntity instanceof Player)) {
            return;
        }

        Player victimPlayer = (Player) victimEntity;

        if (disabledWorlds.contains(victimPlayer.getWorld())) {
            return;
        }


        Player attackerPlayer = findAttacker(event);

        if (attackerPlayer == null) {
            return;
        }

        Member attacker, victim;

        attacker = provider.getMember(attackerPlayer.getUniqueId());
        victim = provider.getMember(victimPlayer.getUniqueId());

        Group attackerGroup = attacker.getGroup();
        Group victimGroup = victim.getGroup();

        if (victimGroup != null && attackerGroup != null) {

            // ally clan, deny damage
            if (victimGroup.getRelation(attackerGroup).getType() == Relation.Type.ALLIED) {
                event.setCancelled(true);
            }

            // personal ff enabled, allow damage
            // skip if globalff is on
            // group ff enabled, allow damage

            boolean personalFF = victim.getBoolean(this.personalFF);
            boolean groupFF = victimGroup.getBoolean(this.groupFF);
            if (globalFFForced || personalFF || groupFF) {
                return;
            }

            // same clan, deny damage

            if (victimGroup.equals(attackerGroup)) {
                event.setCancelled(true);
                return;
            }

        } else {
            // not part of a clan - check if safeCivilians is set
            // ignore setting if he has a specific permissions
            if (saveCivilians &&
                    !victimPlayer.hasPermission("simpleclans.ignore-safe-civilians")) {
                event.setCancelled(true);
            }
        }

    }
}
