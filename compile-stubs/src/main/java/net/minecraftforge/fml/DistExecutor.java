package net.minecraftforge.fml;
import net.minecraftforge.api.distmarker.Dist;
import java.util.function.Supplier;
public class DistExecutor {
    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        try {
            if (toRun != null && toRun.get() != null) {
                toRun.get().run();
            }
        } catch (Throwable ignored) {}
    }
}
