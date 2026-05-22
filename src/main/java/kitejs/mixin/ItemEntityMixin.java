package kitejs.mixin;

import kitejs.RarityGlow;
import kitejs.utils.ItemRarityHelper;
import kitejs.utils.ParticleGenerator;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void rarityglow$spawnBeamParticles(CallbackInfo ci) {
        var config = RarityGlow.CONFIG;

        var entity = (ItemEntity) (Object) this;
        if (!ItemRarityHelper.isBeamEnabled(entity.getItem())) return;

        int color = ItemRarityHelper.getBeamColor(entity.getItem());
        if (color == 0) return;

        ParticleGenerator.generateParticles(
                entity, color,
                config.beam.particleCount,
                config.beam.beamOffset,
                config.beam.beamHeight
        );
    }
}
