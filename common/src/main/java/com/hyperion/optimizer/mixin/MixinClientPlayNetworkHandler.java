package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.network.PacketFlushConsolidator;

public class MixinClientPlayNetworkHandler {
    public static boolean recordPacket(Object channelKey, int maxBatchSize) {
        PacketFlushConsolidator consolidator = HyperionEngine.getInstance().getNetworkConsolidator();
        if (consolidator != null && consolidator.isEnabled()) {
            consolidator.incrementPending(channelKey);
            return consolidator.shouldConsolidateFlush(channelKey, maxBatchSize);
        }
        return false;
    }

    public static void onPlayerRespawn() {
        HyperionEngine.getInstance().onPlayerRespawnOrTeleport();
    }
}
