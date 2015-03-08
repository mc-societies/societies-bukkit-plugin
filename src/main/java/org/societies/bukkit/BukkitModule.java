package org.societies.bukkit;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import gnu.trove.set.hash.THashSet;
import net.catharos.lib.core.command.SystemSender;
import net.catharos.lib.core.command.sender.Sender;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.shank.service.AbstractServiceModule;
import org.societies.api.NameProvider;
import org.societies.api.PlayerResolver;
import org.societies.bridge.ReloadAction;
import org.societies.bridge.Scheduler;
import org.societies.bridge.World;
import org.societies.bridge.WorldResolver;
import org.societies.bridge.bukkit.BukkitMaterial;
import org.societies.bridge.bukkit.BukkitWorld;
import org.societies.bukkit.listener.ListenerService;
import org.societies.converter.ConverterModule;
import org.societies.converter.ConverterService;
import org.societies.groups.ExtensionFactory;
import org.societies.groups.ExtensionRoller;
import org.societies.groups.member.Member;

import java.util.Collection;
import java.util.UUID;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * Represents a BukkitModule
 */
public class BukkitModule extends AbstractServiceModule {

    private final Server server;
    private final SocietiesPlugin plugin;
    private final Economy economy;

    public BukkitModule(Server server, SocietiesPlugin plugin, Economy economy) {
        this.server = server;
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    protected void configure() {
        bindService().to(ListenerService.class);

        bindService().to(SchedulerService.class);

        bind(Economy.class).toInstance(economy);
        bind(Server.class).toInstance(server);
        bind(Plugin.class).toInstance(plugin);
        bind(BukkitScheduler.class).toInstance(server.getScheduler());
        bind(ConsoleCommandSender.class).toInstance(server.getConsoleSender());
        bindNamed("default-world", World.class).toInstance(new BukkitWorld(server.getWorlds().get(0)));

        bind(WorldResolver.class).to(BukkitWorldResolver.class);


        bind(Scheduler.class).to(org.societies.bridge.bukkit.BukkitScheduler.class);

        Material[] values = Material.values();

        THashSet<org.societies.bridge.Material> materials = new THashSet<org.societies.bridge.Material>(values.length);

        for (Material value : values) {
            materials.add(new BukkitMaterial(value));
        }

        bind(NameProvider.class).to(BukkitNameProvider.class);

        bind(ReloadAction.class).toInstance(plugin);

        bind(PlayerResolver.class).to(BukkitPlayerResolver.class);

        bind(SystemSender.class).to(BukkitSystemSender.class);

        bind(new TypeLiteral<Collection<org.societies.bridge.Material>>() {}).toInstance(materials);

        install(new ConverterModule(server));
        bindService().to(ConverterService.class);

        bind(ClassLoader.class).toInstance(plugin.getPluginClassLoader());

        Multibinder<ExtensionRoller<Member>> extensions = newSetBinder(
                binder(),
                new TypeLiteral<ExtensionRoller<Member>>() {}
        );

        install(new FactoryModuleBuilder()
                .implement(Sender.class, BukkitSocietiesMember.class)
                .build(new TypeLiteral<ExtensionFactory<Sender, UUID>>() {}));

        install(new FactoryModuleBuilder()
                .implement(BukkitSocietiesMember.class, BukkitSocietiesMember.class)
                .build(new TypeLiteral<ExtensionFactory<BukkitSocietiesMember, UUID>>() {}));

        extensions.addBinding().to(BridgeExtensionRoller.class);
    }
}
