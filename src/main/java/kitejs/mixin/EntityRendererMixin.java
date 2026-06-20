package kitejs.mixin;

import kitejs.RarityGlow;
import kitejs.utils.ItemRarityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void rarityglow$afterExtract(
            T entity,
            EntityRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        if (entity instanceof ItemEntity itemEntity) {
            // Distance check first — cheaper than color lookup
            var camera = Minecraft.getInstance().gameRenderer.mainCamera();
            double maxDist = RarityGlow.CONFIG.beam.maxRenderDistance;
            double dx = entity.getX() - camera.position().x;
            double dy = entity.getY() - camera.position().y;
            double dz = entity.getZ() - camera.position().z;
            if (dx * dx + dy * dy + dz * dz > maxDist * maxDist) return;

            int color = ItemRarityHelper.getGlowColorIfEnabled(itemEntity.getItem());
            if (color == 0) return;

            state.outlineColor = color;
        }
    }
}
