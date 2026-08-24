package com.hyperion.optimizer.mixin;

import com.hyperion.optimizer.HyperionEngine;
import com.hyperion.optimizer.core.audio.AsyncAudioEngine;

public class MixinSoundEngine {
    public static void submitAsyncAudioTask(Runnable audioTask) {
        AsyncAudioEngine engine = HyperionEngine.getInstance().getAudioEngine();
        if (engine != null && engine.isEnabled()) {
            engine.dispatchAudioCalculation(audioTask);
        } else {
            audioTask.run();
        }
    }
}
