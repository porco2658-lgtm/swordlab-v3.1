package com.swordlab.module;

public enum Category {
    SWORD("Sword"),
    CRYSTAL("Crystal"),
    VISUALS("Visuals");

    public final String label;
    Category(String label) { this.label = label; }
}
