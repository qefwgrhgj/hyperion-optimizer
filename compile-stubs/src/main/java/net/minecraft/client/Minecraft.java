package net.minecraft.client;

import net.minecraft.client.gui.screens.Screen;

public class Minecraft {
    public Screen screen;
    private static final Minecraft INSTANCE = new Minecraft();

    public static Minecraft getInstance() {
        return INSTANCE;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void execute(Runnable runnable) {
        if (runnable != null) runnable.run();
    }
}
