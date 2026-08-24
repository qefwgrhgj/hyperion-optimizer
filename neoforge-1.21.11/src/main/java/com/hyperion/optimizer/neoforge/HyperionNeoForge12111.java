package com.hyperion.optimizer.neoforge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;

public class HyperionNeoForge12111 {
    public static final String MOD_ID = "hyperion";

    public HyperionNeoForge12111() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Engine on NeoForge 1.21.11");
    }
}
