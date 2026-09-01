package net.minecraft.client.gui.components;

public class Button {
    public boolean active = true;

    public static Builder builder(Object message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public void setMessage(Object message) {}

    public interface OnPress {
        void onPress(Button button);
    }

    public static class Builder {
        public Builder(Object msg, OnPress onPress) {}
        public Builder pos(int x, int y) { return this; }
        public Builder size(int width, int height) { return this; }
        public Builder bounds(int x, int y, int width, int height) { return this; }
        public Builder tooltip(Object tooltip) { return this; }
        public Button build() { return new Button(); }
    }
}
