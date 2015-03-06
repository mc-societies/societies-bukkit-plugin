package org.societies.bukkit;

import com.google.inject.Inject;
import org.shank.service.AbstractService;
import org.shank.service.lifecycle.LifecycleContext;
import org.societies.bridge.Scheduler;

/**
 * Represents a SchedulerService
 */
public class SchedulerService extends AbstractService {

    private final Scheduler scheduler;
    private final SlowBuffTask buffTask;

    @Inject
    public SchedulerService(Scheduler scheduler, SlowBuffTask buffTask) {
        this.scheduler = scheduler;
        this.buffTask = buffTask;
    }

    @Override
    public void start(LifecycleContext context) throws Exception {
        scheduler.scheduleSyncRepeatingTask(buffTask, 20);
    }
}
