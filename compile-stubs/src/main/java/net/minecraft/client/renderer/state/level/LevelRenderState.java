package net.minecraft.client.renderer.state.level;
import java.util.List;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
public class LevelRenderState {
    public CameraRenderState cameraRenderState;
    public List<EntityRenderState> entityRenderStates;
    public List<BlockEntityRenderState> blockEntityRenderStates;
}
