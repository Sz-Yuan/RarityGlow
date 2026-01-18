package kitejs.mixin;

import kitejs.utils.ItemRarityHelper;
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
            float tickDelta,
            CallbackInfo ci
    ) {
        if (entity instanceof ItemEntity itemEntity) {
            var stack = itemEntity.getItem();
            if (ItemRarityHelper.shouldGlow(stack)) {
                state.outlineColor = ItemRarityHelper.getGlowColor(stack);
            }
        }
    }
}
