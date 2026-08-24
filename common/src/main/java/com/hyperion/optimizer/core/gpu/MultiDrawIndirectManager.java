package com.hyperion.optimizer.core.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MultiDrawIndirectManager {
    // Structure of DrawElementsIndirectCommand:
    // uint count;
    // uint instanceCount;
    // uint firstIndex;
    // int  baseVertex;
    // uint baseInstance;
    private static final int COMMAND_SIZE_BYTES = 20;

    private final int maxBatchSize;
    private final ByteBuffer indirectCommandBuffer;
    private int activeCommandCount = 0;
    private boolean isFinished = false;

    public MultiDrawIndirectManager(int maxBatchSize) {
        this.maxBatchSize = Math.max(1, maxBatchSize);
        this.indirectCommandBuffer = ByteBuffer.allocateDirect(this.maxBatchSize * COMMAND_SIZE_BYTES)
                                               .order(ByteOrder.nativeOrder());
    }

    public synchronized void beginBatch() {
        this.indirectCommandBuffer.clear();
        this.activeCommandCount = 0;
        this.isFinished = false;
    }

    // Fix P2-2: Thread-safe indirect draw command recording across concurrent chunk mesh builder threads
    public synchronized boolean recordDrawCommand(int indexCount, int instanceCount, int firstIndex, int baseVertex, int baseInstance) {
        if (isFinished || activeCommandCount >= maxBatchSize) {
            return false;
        }

        indirectCommandBuffer.putInt(indexCount);
        indirectCommandBuffer.putInt(instanceCount);
        indirectCommandBuffer.putInt(firstIndex);
        indirectCommandBuffer.putInt(baseVertex);
        indirectCommandBuffer.putInt(baseInstance);

        activeCommandCount++;
        return true;
    }

    // Fix P1-2: Idempotent finishBatch to prevent double-flip buffer clearing during multi-pass rendering
    public synchronized ByteBuffer finishBatch() {
        if (!isFinished) {
            indirectCommandBuffer.flip();
            isFinished = true;
        }
        return indirectCommandBuffer.asReadOnlyBuffer();
    }

    public synchronized int getActiveCommandCount() {
        return activeCommandCount;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }
}
