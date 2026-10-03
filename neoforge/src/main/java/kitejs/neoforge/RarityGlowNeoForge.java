package kitejs.neoforge;

import kitejs.RarityGlow;
import kitejs.config.RarityGlowConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = RarityGlow.MOD_ID, dist = Dist.CLIENT)
public class RarityGlowNeoForge {
    public RarityGlowNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        RarityGlow.LOGGER.info("Hello RarityGlow (NeoForge)!");

        RarityGlow.init();

        // NeoForge has no ModMenu: the config screen is offered through an extension point.
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (client, parent) -> AutoConfigClient.getConfigScreen(RarityGlowConfig.class, parent).get());

        NeoForge.EVENT_BUS.addListener(NeoForgeBeamRenderer::onSubmitCustomGeometry);
    }
}
