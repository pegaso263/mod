package com.obsidianevo.item;

import com.obsidianevo.ObsidianEvoMod;
import com.obsidianevo.item.custom.EvolvingArmorItem;
import com.obsidianevo.item.custom.EvolvingSwordItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ObsidianEvoMod.MOD_ID);

    public static final RegistryObject<Item> ESPADA_EVOLUTIVA = ITEMS.register("espada_evolutiva",
            () -> new EvolvingSwordItem(ModToolTiers.OBSIDIAN_EVO_TIER,
                    new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(ModToolTiers.OBSIDIAN_EVO_TIER, 0, -2.4f))));

    public static final RegistryObject<Item> CASCO_EVOLUTIVO = ITEMS.register("casco_evolutivo",
            () -> new EvolvingArmorItem(ModArmorMaterials.OBSIDIAN_EVO_ARMOR.getHolder().orElseThrow(),
                    ArmorItem.Type.HELMET, new Item.Properties().fireResistant().durability(ArmorItem.Type.HELMET.getDurability(37))));

    public static final RegistryObject<Item> PETO_EVOLUTIVO = ITEMS.register("peto_evolutivo",
            () -> new EvolvingArmorItem(ModArmorMaterials.OBSIDIAN_EVO_ARMOR.getHolder().orElseThrow(),
                    ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));

    public static final RegistryObject<Item> GREBAS_EVOLUTIVAS = ITEMS.register("grebas_evolutivas",
            () -> new EvolvingArmorItem(ModArmorMaterials.OBSIDIAN_EVO_ARMOR.getHolder().orElseThrow(),
                    ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(37))));

    public static final RegistryObject<Item> BOTAS_EVOLUTIVAS = ITEMS.register("botas_evolutivas",
            () -> new EvolvingArmorItem(ModArmorMaterials.OBSIDIAN_EVO_ARMOR.getHolder().orElseThrow(),
                    ArmorItem.Type.BOOTS, new Item.Properties().fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
