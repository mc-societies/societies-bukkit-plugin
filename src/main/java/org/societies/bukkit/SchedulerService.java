package org.societies.bukkit;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.shank.service.AbstractService;
import org.shank.service.lifecycle.LifecycleContext;
import org.societies.bridge.Scheduler;

/**
 * Represents a SchedulerService
 */
public class SchedulerService extends AbstractService {

    private final Scheduler scheduler;
    private final Config config;

    @Inject
    public SchedulerService(Scheduler scheduler, Config config) {
        this.scheduler = scheduler;
        this.config = config;
    }

    @Override
    public void start(LifecycleContext context) throws Exception {
        if (config.getBoolean("city.enable")) {
            scheduler.scheduleSyncRepeatingTask(context.get(SlowBuffTask.class), 20);
        }
    }
}
