package com.hyperion.optimizer.forge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

@Mod("hyperion_optimizer")
public class HyperionForge1165 {
    public static final String MOD_ID = "hyperion_optimizer";

    public HyperionForge1165() {
        HyperionConfig config = HyperionConfigStorage.loadOrCreate();
        HyperionEngine.getInstance().initialize(config);
        try {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            if (bus != null) {
                bus.addListener(this::commonSetup);
                try {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(this::clientSetup));
                } catch (Throwable t) {
                    if (!HyperionEngine.isDedicatedServer()) {
                        bus.addListener(this::clientSetup);
                    }
                }
            }
        } catch (Throwable ignored) {}
        System.out.println("[Hyperion] Initialized Hyperion Multi-Core Engine on Forge 1.16.5");
    }

    public void commonSetup(FMLCommonSetupEvent event) {}
    public void clientSetup(FMLClientSetupEvent event) {}
}
