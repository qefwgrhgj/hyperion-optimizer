package com.hyperion.optimizer.fabric;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;
import com.hyperion.optimizer.api.HyperionConfigStorage;
import net.fabricmc.api.ModInitializer;

public class HyperionFabric12111 implements ModInitializer {
    @Override
    public void onInitialize() {
        HyperionConfig config = HyperionConfigStorage.loadOrCreate();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Engine on Fabric 1.21.11");
    }

    public void onInitializeClient() {
        HyperionConfig config = HyperionConfigStorage.loadOrCreate();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Client & Keybindings on Fabric 1.21.11");
    }
}
