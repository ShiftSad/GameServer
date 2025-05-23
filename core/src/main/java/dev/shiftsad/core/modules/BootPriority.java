package dev.shiftsad.core.modules;

import lombok.Getter;

@Getter
public enum BootPriority {
    HIGHEST(3),
    NORMAL(2),
    LOWEST(1);

    BootPriority(int priority) {}
}
