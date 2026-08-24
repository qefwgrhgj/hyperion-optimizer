package com.hyperion.optimizer.neoforge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;

public class HyperionNeoForge261 {
    public static final String MOD_ID = "hyperion";

    public HyperionNeoForge261() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Multi-Core Engine on NeoForge 26.1");
    }
}
