package com.hyperion.optimizer.neoforge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;

public class HyperionNeoForge120 {
    public static final String MOD_ID = "hyperion";

    public HyperionNeoForge120() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Engine on NeoForge 1.20.4");
    }
}
