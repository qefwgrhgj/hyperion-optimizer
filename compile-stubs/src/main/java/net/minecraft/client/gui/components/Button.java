package net.minecraft.client.gui.components;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class Button implements GuiEventListener {
    public boolean active = true;

    public static Builder builder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public void setMessage(Component message) {}

    public interface OnPress {
        void onPress(Button button);
    }

    public static class Builder {
        public Builder(Component msg, OnPress onPress) {}
        public Builder pos(int x, int y) { return this; }
        public Builder size(int width, int height) { return this; }
        public Builder bounds(int x, int y, int width, int height) { return this; }
        public Builder tooltip(Tooltip tooltip) { return this; }
        public Button build() { return new Button(); }
    }
}
