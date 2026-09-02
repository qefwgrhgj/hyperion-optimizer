package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;

public class Tooltip {
    public static Tooltip create(Component message) {
        return new Tooltip();
    }
}
