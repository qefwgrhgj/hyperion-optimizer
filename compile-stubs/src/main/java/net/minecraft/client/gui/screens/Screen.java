package net.minecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Screen {
    public Screen parent;
    protected Minecraft minecraft;
    public int width;
    public int height;

    public Screen() {}
    public Screen(Component title) {}
    protected void init() {}
    public void clearWidgets() {}
    public void onClose() {}
    public void render(Object graphics, int mouseX, int mouseY, float delta) {}
    public void renderBackground(Object graphics) {}
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean isPauseScreen() { return false; }
    public <T> T addRenderableWidget(T widget) { return widget; }
}
