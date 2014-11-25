package net.catharos.societies.bukkit;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import net.catharos.bridge.ReloadAction;
import net.catharos.groups.MemberProvider;
import net.catharos.lib.core.command.*;
import net.catharos.lib.core.command.sender.Sender;
import net.catharos.lib.shank.logging.LoggingModule;
import net.catharos.lib.shank.service.ServiceController;
import net.catharos.lib.shank.service.ServiceModule;
import net.catharos.lib.shank.service.lifecycle.Lifecycle;
import net.catharos.societies.SocietiesModule;
import net.catharos.societies.api.member.SocietyMember;
import net.catharos.societies.bukkit.util.LoggerWrapper;
import net.catharos.societies.economy.DummyEconomy;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Futures.addCallback;


/**
 * Represents a Launcher
 */
public class SocietiesLoader implements Listener, ReloadAction {

    private Injector injector;

    private Commands<Sender> commands;
    private MemberProvider<SocietyMember> memberProvider;
    private ServiceController serviceController;
    private Sender systemSender;
    private Logger logger;

    private final JavaPlugin plugin;

    public SocietiesLoader(JavaPlugin plugin) {this.plugin = plugin;}

    public void onEnable() {
        logger = new LoggerWrapper(plugin.getLogger());

        Economy economy;

        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        } else {
            economy = new DummyEconomy();
        }

        File dir = plugin.getDataFolder();

        plugin.getLogger().info("Reloading AK-47... Please wait patiently!");

        injector = Guice.createInjector(
                new ServiceModule(),
                new LoggingModule(logger),
                new SocietiesModule(dir, logger),
                new BukkitModule(plugin.getServer(), plugin, this, economy)
        );

        plugin.getLogger().info("Well done.");

        serviceController = injector.getInstance(ServiceController.class);

        serviceController.invoke(Lifecycle.INITIALISING);


        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        commands = injector.getInstance(Key.get(new TypeLiteral<Commands<Sender>>() {}));
        memberProvider = injector.getInstance(Key.get(new TypeLiteral<MemberProvider<SocietyMember>>() {}));
        systemSender = injector.getInstance(Key.get(Sender.class, Names.named("system-sender")));


        serviceController.invoke(Lifecycle.STARTING);

//        try {
//            printPermissions(new PrintStream(new FileOutputStream("fuck")));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public void printPermissions(final PrintStream stream) {
        commands.iterate(new FormatCommandIterator<Sender>("/", " - ") {
            @Override
            public void iterate(net.catharos.lib.core.command.Command<Sender> command, String format) {
                stream.println("   " + command.getPermission() + ": true");
                stream.println("     description: " + "Allows you to use the command \"" + format + "\"");
            }
        });
    }

    public void onDisable() {
        if (injector == null) {
            return;
        }

        ListeningExecutorService service = injector.getInstance(ListeningExecutorService.class);

        service.shutdown();

        try {

            service.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Nobody fucking cares!
            logger.catching(e);
        }

        serviceController.invoke(Lifecycle.STOPPING);

        plugin.getLogger().info("Engines and weapons unloaded and locked!");
    }


    public boolean onCommand(CommandSender sender, final org.bukkit.command.Command command, String label, final String[] args) {

        if (injector == null) {
            sender.sendMessage("Societies failed to start somehow, sorry :/ Fuck the dev!!");
            return true;
        }

        if (sender instanceof Player) {
            ListenableFuture<SocietyMember> future = memberProvider.getMember(((Player) sender).getUniqueId());

            addCallback(future, new FutureCallback<SocietyMember>() {
                @Override
                public void onSuccess(@Nullable SocietyMember result) {
                    commands.execute(result, command.getName(), args);
                }

                @Override
                public void onFailure(@NotNull Throwable t) {
                    logger.catching(t);
                }
            });


        } else {
            commands.execute(systemSender, command.getName(), args);
        }


        return true;
    }

    public List<String> onTabComplete(CommandSender sender, final org.bukkit.command.Command command, String alias, final String[] args) {
        if (sender instanceof Player) {

            // Using dummy sender
            CommandContext<Sender> ctx = commands.createContext(new SystemSender(), command.getName(), args);

            Command<Sender> groupCommand = ctx.getCommand();

            if (groupCommand instanceof GroupCommand) {

                List<String> output = new ArrayList<String>(((GroupCommand<Sender>) groupCommand).size());

                for (Command<Sender> cmd : ((GroupCommand<Sender>) groupCommand).getChildren()) {
                    output.add(cmd.getIdentifier());
                }

                return output;
            }
        }

        return null;
    }

    @Override
    public void reload() {
        onDisable();
        onEnable();
    }
}
