package com.obsidianevo.item;

import com.obsidianevo.ObsidianEvoMod;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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
                    // 1. EL MAPA DE USOS/DURABILIDAD (Aquí están los 100,000 para cada pieza)
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.LEGGINGS, 1);
                        map.put(ArmorItem.Type.CHESTPLATE, 1);
                        map.put(ArmorItem.Type.HELMET, 1);
                    }),
                    // 2. Encantabilidad (int)
                    15,
                    // 3. Sonido de equipamiento
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    // 4. Material de reparación
                    () -> Ingredient.of(Items.OBSIDIAN),
                    // 5. Capas de textura
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ObsidianEvoMod.MOD_ID, "mi_armadura"))),
                    // 6. Dureza de armadura (Armor Toughness)
                    3.0f,
                    // 7. Resistencia al empuje (Knockback Resistance)
                    0.1f
            ));

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
