package com.hyperion.optimizer.neoforge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod("hyperion_optimizer")
public class HyperionNeoForge120 {
    public static final String MOD_ID = "hyperion_optimizer";

    public HyperionNeoForge120() {
        this(null);
    }

    public HyperionNeoForge120(IEventBus modEventBus) {
        HyperionConfig config = HyperionConfigStorage.loadOrCreate();
        HyperionEngine.getInstance().initialize(config);
        if (modEventBus != null) {
            modEventBus.addListener(this::commonSetup);
            if (!HyperionEngine.isDedicatedServer()) {
                modEventBus.addListener(this::clientSetup);
            }
        }
        System.out.println("[Hyperion] Initialized Hyperion Multi-Core Engine on NeoForge 1.20.4");
    }

    public void commonSetup(FMLCommonSetupEvent event) {}
    public void clientSetup(FMLClientSetupEvent event) {}
}
