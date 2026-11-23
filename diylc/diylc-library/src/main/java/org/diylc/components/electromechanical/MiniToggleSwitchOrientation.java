package org.diylc.components.electromechanical;

public enum MiniToggleSwitchOrientation {

    VERTICAL("Vertical"),
    HORIZONTAL("Horizontal"),
    STACKED_VERTICAL("Stacked-Vertical"),
    STACKED_HORIZONTAL("Stacked-Horizontal");

    private final String label;

    MiniToggleSwitchOrientation(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}