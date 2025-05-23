package dev.shiftsad.core.modules.impl;

import dev.shiftsad.core.modules.BootPriority;
import dev.shiftsad.core.modules.Module;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class RedisModule implements Module {

    private final ConsulConfigModule configModule;
    private RedisClient client;
    private RedisStringReactiveCommands<String, String> commands;

    @Override
    public void initialize() {
        String redisHost = configModule.getConfigValue("redis.host");
        client = RedisClient.create(redisHost);
        commands = client.connect().reactive();
    }

    @Override
    public void stop() {
        if (client != null) {
            client.shutdown();
            commands = null;
        }
    }

    @Override
    public @NotNull BootPriority getBootPriority() {
        return BootPriority.HIGHEST;
    }

    @Override
    public @NotNull String getName() {
        return "Redis";
    }
}
