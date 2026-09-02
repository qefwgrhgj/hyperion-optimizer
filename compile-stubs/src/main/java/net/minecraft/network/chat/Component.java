package net.minecraft.network.chat;

public interface Component {
    static MutableComponent literal(String text) {
        return null;
    }
    static MutableComponent nullToEmpty(String text) {
        return null;
    }
    static MutableComponent translatable(String key) {
        return null;
    }
    default String getString() {
        return "";
    }
}
