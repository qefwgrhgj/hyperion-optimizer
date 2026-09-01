package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.network.PacketFlushConsolidator;

@Mixin(targets = {"net.minecraft.client.multiplayer.ClientPacketListener", "net.minecraft.class_634", "net.minecraft.client.network.play.ClientPlayNetHandler"})
@Environment(EnvType.CLIENT)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "handleChunkData", at = @At("HEAD"))
    private void onHandleChunkData(CallbackInfo ci) {
        onPlayerRespawn();
    }

    @Unique
    public static boolean recordPacket(Object channelKey, int maxBatchSize) {
        PacketFlushConsolidator consolidator = HyperionEngine.getInstance().getNetworkConsolidator();
        if (consolidator != null && consolidator.isEnabled()) {
            consolidator.incrementPending(channelKey);
            return consolidator.shouldConsolidateFlush(channelKey, maxBatchSize);
        }
        return false;
    }

    @Unique
    public static void onPlayerRespawn() {
        HyperionEngine.getInstance().onPlayerRespawnOrTeleport();
    }
}
