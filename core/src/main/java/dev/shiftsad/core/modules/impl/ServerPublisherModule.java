package dev.shiftsad.core.modules.impl;

import dev.shiftsad.core.modules.BootPriority;
import dev.shiftsad.core.modules.Module;
import org.jetbrains.annotations.NotNull;

public class ServerPublisherModule implements Module {

    private final ConsulConfigModule configModule;
    private final String name;
    private final String port;

    public ServerPublisherModule(
            ConsulConfigModule configModule,
            String name,
            String port
    ) {
        this.configModule = configModule;
        this.name = name;
        this.port = port;
    }

    @Override
    public void initialize() {
        if (!configModule.setConfigValue(name, port)) {
            throw new IllegalStateException("Failed to set server config in Consul");
        }
    }

    @Override
    public void stop() {
        if (!configModule.deleteConfigValue(name)) {
            throw new IllegalStateException("Failed to delete server config in Consul");
        }
    }

    @Override
    public @NotNull BootPriority getBootPriority() {
        return BootPriority.LOWEST;
    }

    @Override
    public @NotNull String getName() {
        return "ServerPublisher";
    }
}
