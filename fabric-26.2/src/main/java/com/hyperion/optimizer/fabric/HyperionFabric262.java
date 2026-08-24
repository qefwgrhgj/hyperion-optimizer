package com.hyperion.optimizer.fabric;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import net.fabricmc.api.ModInitializer;

public class HyperionFabric262 implements ModInitializer {
    @Override
    public void onInitialize() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Engine on Fabric 26.2");
    }
}
