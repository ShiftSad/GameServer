package dev.shiftsad.core.modules;

import org.jetbrains.annotations.NotNull;

public interface Module {
    void initialize();
    void stop();

    @NotNull BootPriority getBootPriority();
    @NotNull String getName();
}
