package com.hyperion.optimizer.forge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;

public class HyperionForge119 {
    public static final String MOD_ID = "hyperion";

    public HyperionForge119() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Engine on Forge 1.19.4");
    }
}
