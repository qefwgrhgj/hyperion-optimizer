package com.hyperion.optimizer.fabric;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class HyperionFabric1214 implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "hyperion-optimizer";

    @Override
    public void onInitialize() {
        HyperionConfig config = HyperionConfigStorage.loadOrCreate();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Multi-Core Engine on Fabric 1.21.4");
    }

    @Override
    public void onInitializeClient() {
        HyperionConfig config = HyperionConfigStorage.loadOrCreate();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Client & Keybindings on Fabric 1.21.4");
    }
}
