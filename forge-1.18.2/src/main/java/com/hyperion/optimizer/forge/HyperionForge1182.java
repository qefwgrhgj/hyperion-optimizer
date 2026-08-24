package com.hyperion.optimizer.forge;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.api.HyperionConfig;

public class HyperionForge1182 {
    public static final String MOD_ID = "hyperion";

    public HyperionForge1182() {
        HyperionConfig config = new HyperionConfig();
        HyperionEngine.getInstance().initialize(config);
        System.out.println("[Hyperion] Initialized Hyperion Multi-Core Engine on Forge 1.18 / 1.18.1 / 1.18.2");
    }
}
