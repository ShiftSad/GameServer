package dev.shiftsad.core;

import dev.shiftsad.core.modules.Module;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@SuppressWarnings("UnusedReturnValue")
@Getter
public class MinestomServer {
    private final @NotNull String name;
    private final @NotNull UUID uuid;
    private final int port;

    private static Logger logger;
    private final List<Module> modules;

    protected MinestomServer(
            @NotNull String name,
            @NotNull UUID uuid,
            int port,
            @NotNull List<Module> modules
    ) {
        this.name = name;
        this.uuid = uuid;
        this.port = port;
        this.modules = modules;

        logger = Logger.getLogger(name);
    }

    public void start() {
        var server = MinecraftServer.init();

        modules.stream()
                .filter(it -> it.getBootPriority().getValue() == 0)
                .sorted(Comparator.comparingInt(a -> a.getBootPriority().getValue()))
                .forEach(module -> {
                    logger.info("Loading module " + module.getName());
                    var time = System.currentTimeMillis();
                    module.initialize();
                    logger.info("Loaded module " + module.getName() + " in " + (System.currentTimeMillis() - time) + " ms");
                });

        server.start("localhost", port);
    }

    public @Nullable Module findModule(Class<? extends Module> moduleClass) {
        return modules.stream().filter(module -> moduleClass.isAssignableFrom(module.getClass())).findFirst().orElse(null);
    }

    public static @NotNull Logger logger() {
        return logger;
    }

    // builder class
    public static class Builder {
        private String name;
        private UUID uuid;
        private int port;

        private final List<Module> modules = new ArrayList<>();

        public Builder module(Module module) {
            modules.add(module);
            return this;
        }

        public Builder modules(Module... modules) {
            for (Module module : modules) this.module(module);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public MinestomServer build() {
            if (name == null) throw new NullPointerException("name is null");
            if (uuid == null) throw new NullPointerException("uuid is null");
            if (port <= 0) throw new IllegalArgumentException("port must be greater than 0");
            return new MinestomServer(name, uuid, port, modules);
        }
    }
}
