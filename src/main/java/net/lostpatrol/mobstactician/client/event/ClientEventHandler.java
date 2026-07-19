package net.lostpatrol.mobstactician.client.event;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.client.render.entity.EnhancedIronGolemRenderer;
import net.lostpatrol.mobstactician.client.render.entity.EnhancedPhantomRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

@Mod(value = MobsTactician.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MobsTactician.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    public static final Logger logger = MobsTactician.LOGGER;
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.PHANTOM, EnhancedPhantomRenderer::new);
        event.registerEntityRenderer(EntityType.IRON_GOLEM, EnhancedIronGolemRenderer::new);
    }

//    @SubscribeEvent
//    public static void addLayers(EntityRenderersEvent.AddLayers event) {
//        // Add a layer to every single entity type.
//        for (EntityType<?> entityType : event.getEntityTypes()) {
//            // Get our renderer.
//            EntityRenderer<?, ?> renderer = event.getRenderer(entityType);
//            // We check if our render layer is supported by the renderer.
//            // If you want a more general-purpose render layer, you will need to work with wildcard generics.
//            if (renderer instanceof PhantomRenderer phantomRenderer) {
//                // Add the layer to the renderer. Like above, construct a new MyRenderLayer.
//                // The EntityModelSet can be retrieved from the event through #getEntityModels.
//                phantomRenderer.addLayer(new PhantomWeaponLayer(renderer, event.getEntityModels()));
//            }
//        }
//    }
}
