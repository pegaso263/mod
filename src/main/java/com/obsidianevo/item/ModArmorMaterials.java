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
import java.util.Map;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ObsidianEvoMod.MOD_ID);

    public static final RegistryObject<ArmorMaterial> OBSIDIAN_EVO_ARMOR = ARMOR_MATERIALS.register("mi_armadura",
            () -> new ArmorMaterial(
                    // 1. Mapa de durabilidad masiva (100,000 usos reales para cada pieza)
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 100000);
                        map.put(ArmorItem.Type.LEGGINGS, 100000);
                        map.put(ArmorItem.Type.CHESTPLATE, 100000);
                        map.put(ArmorItem.Type.HELMET, 100000);
                    }),
                    // 2. Mapa de defensa estándar por pieza (Botas: 3, Leggings: 6, Pechera: 8, Casco: 3)
                    Map.of(
                            ArmorItem.Type.BOOTS, 1,
                            ArmorItem.Type.LEGGINGS, 1,
                            ArmorItem.Type.CHESTPLATE, 1,
                            ArmorItem.Type.HELMET, 1
                    ),
                    // 3. Encantabilidad
                    15,
                    // 4. Sonido de equipamiento
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    // 5. Material de reparación (Obsidiana)
                    () -> Ingredient.of(Items.OBSIDIAN),
                    // 6. Capa de textura
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ObsidianEvoMod.MOD_ID, "mi_armadura"))),
                    // 7. Dureza de armadura (Armor Toughness)
                    3.0f,
                    // 8. Resistencia al empuje
                    0.1f
            ));

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
