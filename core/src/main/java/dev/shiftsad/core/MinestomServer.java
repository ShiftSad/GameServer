package dev.shiftsad.core;

import dev.shiftsad.core.exceptions.ModuleDependencyException;
import dev.shiftsad.core.modules.DependsOn;
import dev.shiftsad.core.modules.Inject;
import dev.shiftsad.core.modules.Module;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
@Getter
public class MinestomServer {
    private final @NotNull String game;
    private final @NotNull String name;
    private final @NotNull UUID uuid;
    private final int port;

    private static Logger logger;
    private final List<Module> modules;
    private final Map<Class<? extends Module>, Module> moduleMap;
    private final Set<Module> loadedModules;

    protected MinestomServer(
            @NotNull String game,
            @NotNull String name,
            @NotNull UUID uuid,
            int port,
            @NotNull List<Module> modules
    ) {
        this.game = game;
        this.name = name;
        this.uuid = uuid;
        this.port = port;
        this.modules = modules;
        this.moduleMap = new HashMap<>();
        this.loadedModules = new HashSet<>();

        // Build module map for quick lookup
        for (Module module : modules) {
            moduleMap.put(module.getClass(), module);
        }

        logger = Logger.getLogger(name);
    }

    public void start() {
        var server = MinecraftServer.init();
        loadModulesWithDependencies();
        server.start("localhost", port);
    }

    private void loadModulesWithDependencies() {
        List<Module> modulesToLoad = modules.stream()
                .filter(module -> module.getBootPriority().getValue() > 0)
                .collect(Collectors.toList());

        List<Module> loadOrder = resolveDependencies(modulesToLoad);

        for (Module module : loadOrder) {
            loadModule(module);
        }
    }

    private List<Module> resolveDependencies(List<Module> modulesToLoad) {
        List<Module> resolved = new ArrayList<>();
        Set<Module> resolving = new HashSet<>();
        Set<Module> unresolved = new HashSet<>(modulesToLoad);

        for (Module module : modulesToLoad) {
            resolveDependency(module, resolved, resolving, unresolved);
        }

        return resolved.stream()
                .sorted(Comparator.comparingInt((Module a) -> a.getBootPriority().getValue()).reversed())
                .collect(Collectors.toList());
    }

    private void resolveDependency(
            Module module,
            List<Module> resolved,
            Set<Module> resolving,
            Set<Module> unresolved
    ) {
        if (resolved.contains(module)) {
            return;
        }

        if (resolving.contains(module)) {
            throw new ModuleDependencyException(
                    "Circular dependency detected involving module: " +
                            module.getName()
            );
        }

        resolving.add(module);

        // Get dependencies from annotation
        DependsOn dependsOn = module.getClass().getAnnotation(DependsOn.class);
        if (dependsOn != null) {
            for (Class<? extends Module> depClass : dependsOn.value()) {
                Module dependency = findModuleByClass(depClass);
                if (dependency == null) {
                    throw new ModuleDependencyException(
                            "Module " + module.getName() +
                                    " depends on " + depClass.getSimpleName() +
                                    " but it's not registered"
                    );
                }

                if (unresolved.contains(dependency)) {
                    resolveDependency(dependency, resolved, resolving, unresolved);
                }
            }
        }

        resolving.remove(module);
        unresolved.remove(module);
        resolved.add(module);
    }

    private void loadModule(Module module) {
        if (loadedModules.contains(module)) {
            return; // Already loaded
        }

        logger.info("Loading module " + module.getName());
        var time = System.currentTimeMillis();

        injectDependencies(module);
        module.initialize();

        loadedModules.add(module);
        logger.info(
                "Loaded module " + module.getName() +
                        " in " + (System.currentTimeMillis() - time) + " ms"
        );
    }

    private void injectDependencies(Module module) {
        Class<?> clazz = module.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    injectField(module, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void injectField(Module module, Field field) {
        try {
            field.setAccessible(true);

            if (field.get(module) != null) {
                return;
            }

            Class<?> fieldType = field.getType();

            Module dependency = null;
            if (Module.class.isAssignableFrom(fieldType)) {
                @SuppressWarnings("unchecked")
                Class<? extends Module> moduleClass =
                        (Class<? extends Module>) fieldType;
                dependency = findModuleByClass(moduleClass);
            }

            if (dependency == null) {
                throw new ModuleDependencyException(
                        "Cannot inject dependency of type " +
                                fieldType.getSimpleName() +
                                " into module " + module.getName() +
                                ". No matching module found."
                );
            }

            if (!loadedModules.contains(dependency)) {
                loadModule(dependency);
            }

            field.set(module, dependency);
            logger.info(
                    "Injected " + dependency.getName() +
                            " into " + module.getName()
            );

        } catch (IllegalAccessException e) {
            throw new ModuleDependencyException(
                    "Failed to inject dependency into field " +
                            field.getName() + " of module " + module.getName(),
                    e
            );
        }
    }

    public @Nullable Module findModule(Class<? extends Module> moduleClass) {
        return findModuleByClass(moduleClass);
    }

    private @Nullable Module findModuleByClass(Class<? extends Module> moduleClass) {
        Module exactMatch = moduleMap.get(moduleClass);
        if (exactMatch != null) {
            return exactMatch;
        }

        return modules.stream()
                .filter(module -> moduleClass.isAssignableFrom(module.getClass()))
                .findFirst()
                .orElse(null);
    }

    public void loadModule(Class<? extends Module> moduleClass) {
        Module module = findModuleByClass(moduleClass);
        if (module == null) {
            throw new ModuleDependencyException(
                    "Module " + moduleClass.getSimpleName() + " not found"
            );
        }

        if (module.getBootPriority().getValue() == 0) {
            loadModule(module);
        }
    }

    public boolean isModuleLoaded(Class<? extends Module> moduleClass) {
        Module module = findModuleByClass(moduleClass);
        return module != null && loadedModules.contains(module);
    }

    public static class Builder {
        private String game;
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

        public Builder game(String game) {
            this.game = game;
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
            if (game == null) throw new NullPointerException("game is null");
            if (name == null) throw new NullPointerException("name is null");
            if (port <= 0) throw new IllegalArgumentException("port must be greater than 0");
            if (uuid == null) this.uuid = UUID.randomUUID();
            return new MinestomServer(game, name, uuid, port, modules);
        }
    }
}
