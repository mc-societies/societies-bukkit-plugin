package org.societies.bukkit;

import com.google.inject.TypeLiteral;
import gnu.trove.set.hash.THashSet;
import net.catharos.lib.core.command.SystemSender;
import net.catharos.lib.shank.service.AbstractServiceModule;
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
import org.societies.bukkit.listener.ListenerService;
import org.societies.converter.ConverterModule;
import org.societies.groups.member.MemberFactory;

import java.util.Collection;

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

        bind(MemberFactory.class).to(BukkitMemberFactory.class);


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
    }
}
