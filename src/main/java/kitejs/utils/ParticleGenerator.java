package kitejs.utils;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public class ParticleGenerator {
    public static void generateParticles(ItemEntity entity, int color, int particleCount, double beamOffset, double beamHeight) {
        Level level = entity.level();

        var particle = new DustParticleOptions(color, 1.2f);

        for (int i = 0; i < particleCount; i++) {
            level.addParticle(
                    particle,
                    entity.getX(),
                    entity.getY() + beamOffset + (level.getRandom().nextDouble() * beamHeight),
                    entity.getZ(),
                    0.0, 0.0, 0.0
            );
        }
    }
}
