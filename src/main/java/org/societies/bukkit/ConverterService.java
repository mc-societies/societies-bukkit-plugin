package org.societies.bukkit;

import com.google.inject.Inject;
import net.catharos.lib.shank.service.AbstractService;
import net.catharos.lib.shank.service.lifecycle.LifecycleContext;
import org.societies.converter.Converter;

/**
 * Represents a ConverterService
 */
public class ConverterService extends AbstractService {

    private final Converter converter;

    @Inject
    public ConverterService(Converter converter) {this.converter = converter;}

    @Override
    public void init(LifecycleContext context) throws Exception {
        converter.convert();
    }
}
