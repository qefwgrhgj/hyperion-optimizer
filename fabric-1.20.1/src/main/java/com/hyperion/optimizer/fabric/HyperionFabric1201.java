package com.hyperion.optimizer.fabric;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import net.fabricmc.api.ModInitializer;

public class HyperionFabric1201 implements ModInitializer {
    @Override
    public void onInitialize() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Multi-Core Engine on Fabric 1.20 / 1.20.1");
    }
}
