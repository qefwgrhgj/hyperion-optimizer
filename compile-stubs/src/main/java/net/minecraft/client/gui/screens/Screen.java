package net.minecraft.client.gui.screens;

public class Screen {
    public Screen parent;
    public Object minecraft;
    public int width;
    public int height;

    public Screen() {}
    public Screen(Object title) {}
    protected void init() {}
    public void clearWidgets() {}
    public void onClose() {}
    public void render(Object graphics, int mouseX, int mouseY, float delta) {}
    public void renderBackground(Object graphics) {}
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean isPauseScreen() { return false; }
    public <T> T addRenderableWidget(T widget) { return widget; }
}
