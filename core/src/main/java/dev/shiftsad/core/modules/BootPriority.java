package dev.shiftsad.core.modules;

import lombok.Getter;

@Getter
public enum BootPriority {
    /* Intended for modules absolutely necessary for other modules */
    CRITICAL(4),
    /* High priority modules */
    HIGHEST(3),
    /* Intended for most game features */
    NORMAL(2),
    /* Low priority modules */
    LOWEST(1);

    BootPriority(int priority) {}
}
