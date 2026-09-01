package com.hyperion.optimizer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.audio.AsyncAudioEngine;

@Mixin(targets = "net.minecraft.client.sounds.SoundEngine")
@Environment(EnvType.CLIENT)
public class MixinSoundEngine {
    @Inject(method = "play", at = @At("HEAD"))
    private void onPlaySound(CallbackInfo ci) {
        // Offloaded async audio buffer evaluation
    }

    public static void submitAsyncAudioTask(Runnable audioTask) {
        AsyncAudioEngine engine = HyperionEngine.getInstance().getAudioEngine();
        if (engine != null && engine.isEnabled()) {
            engine.dispatchAudioCalculation(audioTask);
        } else {
            audioTask.run();
        }
    }
}
