package com.obsidianevo;

import com.obsidianevo.item.ModArmorMaterials;
import com.obsidianevo.item.ModItems;
import com.obsidianevo.item.custom.EvolvingSwordItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ObsidianEvoMod.MOD_ID)
public class ObsidianEvoMod {
    public static final String MOD_ID = "obsidian_evo";

    public ObsidianEvoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModArmorMaterials.register(modEventBus);
        ModItems.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.ESPADA_EVOLUTIVA);
            event.accept(ModItems.CASCO_EVOLUTIVO);
            event.accept(ModItems.PETO_EVOLUTIVO);
            event.accept(ModItems.GREBAS_EVOLUTIVAS);
            event.accept(ModItems.BOTAS_EVOLUTIVAS);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(
                    ModItems.ESPADA_EVOLUTIVA.get(),
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "stage"),
                    (stack, level, entity, seed) -> EvolvingSwordItem.getEvolutionStage(stack)
                );
            });
        }
    }
}
