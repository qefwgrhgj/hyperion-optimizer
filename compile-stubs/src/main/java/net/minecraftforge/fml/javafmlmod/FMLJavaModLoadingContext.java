package net.minecraftforge.fml.javafmlmod;
import net.minecraftforge.eventbus.api.IEventBus;
public class FMLJavaModLoadingContext {
    private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();
    public static FMLJavaModLoadingContext get() { return INSTANCE; }
    public IEventBus getModEventBus() { return null; }
}
