package net.catharos.societies.bukkit;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import gnu.trove.set.hash.THashSet;
import net.catharos.bridge.ReloadAction;
import net.catharos.bridge.Scheduler;
import net.catharos.bridge.World;
import net.catharos.bridge.WorldResolver;
import net.catharos.bridge.bukkit.BukkitMaterial;
import net.catharos.bridge.bukkit.BukkitWorld;
import net.catharos.groups.MemberFactory;
import net.catharos.lib.core.command.SystemSender;
import net.catharos.lib.shank.service.AbstractServiceModule;
import net.catharos.societies.api.NameProvider;
import net.catharos.societies.api.PlayerResolver;
import net.catharos.societies.api.member.SocietyMember;
import net.catharos.societies.bukkit.listener.ListenerService;
import net.catharos.societies.converter.ConverterModule;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;

/**
 * Represents a BukkitModule
 */
public class BukkitModule extends AbstractServiceModule {

    public static final Class<? extends SocietyMember> MEMBER_IMPLEMENTATION = BukkitSocietyMember.class;

    private final Server server;
    private final Plugin plugin;
    private final SocietiesLoader loader;
    private final Economy economy;

    public BukkitModule(Server server, Plugin plugin, SocietiesLoader loader, Economy economy) {
        this.server = server;
        this.plugin = plugin;
        this.loader = loader;
        this.economy = economy;
    }

    @Override
    protected void configure() {
        bindService().to(ListenerService.class);

        bind(Economy.class).toInstance(economy);
        bind(Server.class).toInstance(server);
        bind(Plugin.class).toInstance(plugin);
        bind(BukkitScheduler.class).toInstance(server.getScheduler());
        bind(ConsoleCommandSender.class).toInstance(server.getConsoleSender());
        bindNamed("default-world", World.class).toInstance(new BukkitWorld(server.getWorlds().get(0)));

        bind(WorldResolver.class).to(BukkitWorldResolver.class);


        bind(Scheduler.class).to(net.catharos.bridge.bukkit.BukkitScheduler.class);

        install(new FactoryModuleBuilder()
                .implement(SocietyMember.class, MEMBER_IMPLEMENTATION)
                .build(new TypeLiteral<MemberFactory<SocietyMember>>() {}));

        bind(SocietyMember.class).to(MEMBER_IMPLEMENTATION);


        Material[] values = Material.values();

        THashSet<net.catharos.bridge.Material> materials = new THashSet<net.catharos.bridge.Material>(values.length);

        for (Material value : values) {
            materials.add(new BukkitMaterial(value));
        }

        bind(NameProvider.class).to(BukkitNameProvider.class);

        bind(ReloadAction.class).toInstance(loader);

        bind(PlayerResolver.class).to(BukkitPlayerResolver.class);

        bind(SystemSender.class).to(BukkitSystemSender.class);

        bind(new TypeLiteral<Collection<net.catharos.bridge.Material>>() {}).toInstance(materials);

        install(new ConverterModule(server));
    }
}
