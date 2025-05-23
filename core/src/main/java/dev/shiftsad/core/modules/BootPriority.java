package dev.shiftsad.core.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BootPriority {
    /* Intended for modules absolutely necessary for other modules */
    CRITICAL(4),
    /* High priority modules */
    HIGHEST(3),
    /* Intended for most game features */
    NORMAL(2),
    /* Low priority modules */
    LOWEST(1),
    /* Will only load when called manually */
    NONE(0);

    private final int value;
}
