package com.vexvisuals;

public enum Category {
    HUD("HUD"),
    WORLD("Мир"),
    PLAYER("Игрок");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
