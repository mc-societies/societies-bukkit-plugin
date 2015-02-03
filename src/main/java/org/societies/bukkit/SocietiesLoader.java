package org.societies.bukkit;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import net.catharos.lib.core.command.*;
import net.catharos.lib.core.command.sender.Sender;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.shank.logging.LoggingModule;
import org.shank.service.ServiceController;
import org.shank.service.ServiceModule;
import org.shank.service.lifecycle.Lifecycle;
import org.societies.SocietiesModule;
import org.societies.bridge.ReloadAction;
import org.societies.bukkit.economy.DummyEconomy;
import org.societies.groups.member.Member;
import org.societies.groups.member.MemberProvider;
import org.societies.util.LoggerWrapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Represents a Launcher
 */
public class SocietiesLoader implements Listener, ReloadAction {

    private Injector injector;

    private Commands<Sender> commands;
    private MemberProvider memberProvider;
    private ServiceController serviceController;
    private Sender systemSender;
    private Logger logger;

    private ListeningExecutorService service;

    private final ClassLoader classLoader;
    private final JavaPlugin plugin;

    public SocietiesLoader(ClassLoader classLoader, JavaPlugin plugin) {
        this.classLoader = classLoader;
        this.plugin = plugin;
    }

    public void onEnable() {
        logger = new LoggerWrapper(plugin.getLogger());

        Economy economy;

        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        } else {
            economy = new DummyEconomy();
            logger.info("You need to install Vault to use the economy features");
        }

        File dir = plugin.getDataFolder();

        logger.info("Reloading AK-47... Please wait patiently!");

        injector = Guice.createInjector(
                new ServiceModule(),
                new LoggingModule(logger),
                new SocietiesModule(dir, logger),
                new BukkitModule(plugin.getServer(), plugin, this, economy)
        );

        logger.info("Well done.");

        serviceController = injector.getInstance(ServiceController.class);

        serviceController.invoke(Lifecycle.INITIALISING);


        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        commands = injector.getInstance(Key.get(new TypeLiteral<Commands<Sender>>() {}));
        memberProvider = injector.getInstance(Key.get(new TypeLiteral<MemberProvider>() {}));
        systemSender = injector.getInstance(Key.get(Sender.class, Names.named("system-sender")));
        service = injector.getInstance(SocietiesModule.WORKER_EXECUTOR);


        serviceController.invoke(Lifecycle.STARTING);
    }

    void print() throws UnsupportedEncodingException {
        try {
            printMarkdownPermissions(new PrintStream(new FileOutputStream("permissions"), true, "UTF-8"));
            printMarkdownCommands(new PrintStream(new FileOutputStream("commands"), true, "UTF-8"));
            printPluginYMLPermissions(new PrintStream(new FileOutputStream("plugin"), true, "UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void printPluginYMLPermissions(final PrintStream stream) {
        commands.iterate(new FormatCommandIterator<Sender>("/", " - ", " [?]") {
            @Override
            public void iterate(net.catharos.lib.core.command.Command<Sender> command, String format) {
                if (command.getPermission() == null) {
                    return;
                }
                stream.println("  " + command.getPermission() + ":");
                stream.println("    description: " + "\"Allows you to use the command \'" + format + "\'\"");
            }
        }, true);
    }

    public void printMarkdownPermissions(final PrintStream stream) {
        commands.iterate(new FormatCommandIterator<Sender>("/", " - ", " [?]") {
            @Override
            public void iterate(net.catharos.lib.core.command.Command<Sender> command, String format) {
                if (command.getPermission() == null) {
                    return;
                }
                stream.println("|" + command.getPermission() + "|" + format + "|");
            }
        }, true);
    }

    public void printMarkdownCommands(final PrintStream stream) {
        commands.iterate(new FormatCommandIterator<Sender>("/", " - ", false, " [?]") {
            @Override
            public void iterate(net.catharos.lib.core.command.Command<Sender> command, String format) {
                if (command.getPermission() == null) {
                    return;
                }
                stream.println("|" + format + "|" + command.getDescription() + "|");
            }
        }, true);
    }

    public void onDisable() {
        if (injector == null) {
            return;
        }

        service.shutdown();

        try {

            service.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Nobody fucking cares!
            logger.catching(e);
        }

        serviceController.invoke(Lifecycle.STOPPING);

        logger.info("Engines and weapons unloaded and locked!");
    }


    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, String label, final String[] args) {

        if (injector == null) {
            sender.sendMessage("Societies failed to start somehow, sorry :/ Fuck the dev!!");
            return true;
        }

        if (sender instanceof Player) {

            ListenableFuture<?> future = service.submit(new Runnable() {
                @Override
                public void run() {
                    Member member = memberProvider.getMember(((Player) sender).getUniqueId());
                    commands.execute(member, command.getName(), args);

                }
            });

            Futures.addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object o) {

                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.catching(throwable);
                }
            });

        } else {
            commands.execute(systemSender, command.getName(), args);
        }


//        String[] commands = {
//                "/s create test tes",
//                "/s profile",
//                "/s lookup",
//                "/s profile -society tes",
//                "/s vitals",
//                "/s disprove tes",
//                "/s vitals",
//        };


        return true;
    }

    public List<String> onTabComplete(CommandSender sender, final org.bukkit.command.Command command, String alias, final String[] args) {
        if (sender instanceof Player) {

            if (args.length < 1) {
                return null;
            }

            String[] arguments = new String[args.length - 1];
            System.arraycopy(args, 0, arguments, 0, args.length - 1);

            // Using dummy sender
            CommandContext<Sender> ctx = commands.createContext(new SystemSender(), command.getName(), arguments);

            Command<Sender> groupCommand = ctx.getCommand();

            if (groupCommand instanceof GroupCommand) {

                List<String> output = new ArrayList<String>(((GroupCommand<Sender>) groupCommand).size());

                for (Command<Sender> cmd : ((GroupCommand<Sender>) groupCommand).getChildren()) {
                    if (cmd.getIdentifier().startsWith(args[args.length - 1])) {
                        output.add(cmd.getIdentifier());
                    }
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
