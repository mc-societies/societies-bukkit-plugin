package org.societies.bukkit.listener;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.shank.logging.InjectLogger;
import org.shank.service.AbstractService;
import org.shank.service.lifecycle.LifecycleContext;
import org.societies.groups.event.EventController;

/**
 * Represents a ListenerService
 */
public class ListenerService extends AbstractService {

    private final Server server;
    private final Plugin plugin;

    @InjectLogger
    private Logger logger;

    private final EventController eventController;

    @Inject
    public ListenerService(Server server, Plugin plugin, EventController eventController) {
        this.server = server;
        this.plugin = plugin;
        this.eventController = eventController;
    }

    @Override
    public void start(LifecycleContext context) throws Exception {
        logger.info("Register event listeners...");
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(context.get(ChatListener.class), plugin);
        pluginManager.registerEvents(context.get(DamageListener.class), plugin);
        pluginManager.registerEvents(context.get(SpawnListener.class), plugin);
        pluginManager.registerEvents(context.get(JoinListener.class), plugin);
        pluginManager.registerEvents(context.get(SiegingListener.class), plugin);

        eventController.subscribe(new TeamListener());
    }
}
