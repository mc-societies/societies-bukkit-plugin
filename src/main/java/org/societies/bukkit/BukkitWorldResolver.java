package org.societies.bukkit;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.bukkit.Server;
import org.societies.bridge.World;
import org.societies.bridge.WorldResolver;
import org.societies.bridge.bukkit.BukkitWorld;

/**
 * Represents a BukkitWorldProvider
 */
class BukkitWorldResolver implements WorldResolver {

    private final Server server;
    private final World defaultWorld;

    @Inject
    public BukkitWorldResolver(Server server, @Named("default-world") World defaultWorld) {
        this.server = server;
        this.defaultWorld = defaultWorld;
    }

    @Override
    public World getWorld(String name) {
        return new BukkitWorld(server.getWorld(name));
    }

    @Override
    public World getDefaultWorld() {
        return defaultWorld;
    }
}
