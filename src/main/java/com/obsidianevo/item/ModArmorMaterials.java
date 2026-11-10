package com.obsidianevo.item;

import com.obsidianevo.ObsidianEvoMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ObsidianEvoMod.MOD_ID);

    public static final RegistryObject<ArmorMaterial> OBSIDIAN_EVO_ARMOR = ARMOR_MATERIALS.register("mi_armadura",
            () -> new ArmorMaterial(
                    // Aquí establecemos 100,000 de durabilidad base multiplicada para cada ranura de equipo
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 100000);
                        map.put(ArmorItem.Type.LEGGINGS, 100000);
                        map.put(ArmorItem.Type.CHESTPLATE, 100000);
                        map.put(ArmorItem.Type.HELMET, 100000);
                    }),
                    // Defensa equilibrada (ej. 3 para botas, 6 para piernas, 8 para pechera, 3 para casco -> en total da una buena defensa base)
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.LEGGINGS, 1);
                        map.put(ArmorItem.Type.CHESTPLATE, 1);
                        map.put(ArmorItem.Type.HELMET, 1);
                    }),
                    15, // Nivel de encantabilidad (Enchantability)
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.OBSIDIAN),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ObsidianEvoMod.MOD_ID, "mi_armadura"))),
                    0.0f, // Dureza de armadura (Armor Toughness)
                    0.0f  // Resistencia a la temperatura/knockback
            ));

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
