package org.societies.bukkit;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import gnu.trove.set.hash.THashSet;
import net.catharos.lib.core.command.SystemSender;
import net.catharos.lib.core.command.sender.Sender;
import org.shank.service.AbstractServiceModule;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.societies.api.NameProvider;
import org.societies.api.PlayerResolver;
import org.societies.bridge.ReloadAction;
import org.societies.bridge.Scheduler;
import org.societies.bridge.World;
import org.societies.bridge.WorldResolver;
import org.societies.bridge.bukkit.BukkitMaterial;
import org.societies.bridge.bukkit.BukkitWorld;
import org.societies.bukkit.converter.ConverterService;
import org.societies.bukkit.listener.ListenerService;
import org.societies.converter.ConverterModule;
import org.societies.groups.ExtensionFactory;
import org.societies.groups.ExtensionRoller;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents a BukkitModule
 */
public class BukkitModule extends AbstractServiceModule {

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


        bind(Scheduler.class).to(org.societies.bridge.bukkit.BukkitScheduler.class);

        Material[] values = Material.values();

        THashSet<org.societies.bridge.Material> materials = new THashSet<org.societies.bridge.Material>(values.length);

        for (Material value : values) {
            materials.add(new BukkitMaterial(value));
        }

        bind(NameProvider.class).to(BukkitNameProvider.class);

        bind(ReloadAction.class).toInstance(loader);

        bind(PlayerResolver.class).to(BukkitPlayerResolver.class);

        bind(SystemSender.class).to(BukkitSystemSender.class);

        bind(new TypeLiteral<Collection<org.societies.bridge.Material>>() {}).toInstance(materials);

        install(new ConverterModule(server));
        bindService().to(ConverterService.class);

        bind(ClassLoader.class).toInstance(loader.getClassLoader());

        Multibinder<ExtensionRoller> extensions = Multibinder.newSetBinder(binder(), ExtensionRoller.class);

        install(new FactoryModuleBuilder()
                .implement(Sender.class, BukkitSocietiesMember.class)
                .build(new TypeLiteral<ExtensionFactory<Sender, UUID>>() {}));

        install(new FactoryModuleBuilder()
                .implement(BukkitSocietiesMember.class, BukkitSocietiesMember.class)
                .build(new TypeLiteral<ExtensionFactory<BukkitSocietiesMember, UUID>>() {}));

        extensions.addBinding().to(BukkitExtensionRoller.class);
    }
}
