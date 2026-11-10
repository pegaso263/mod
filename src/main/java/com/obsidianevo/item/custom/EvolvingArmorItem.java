package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class EvolvingArmorItem extends ArmorItem {
    public static final String DAMAGE_ABSORBED_KEY = "DamageAbsorbed";
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("obsidianevo", "extra_health_bonus");

    public EvolvingArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties.durability(100000));
    }

    public static float getDamageAbsorbed(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getFloat(DAMAGE_ABSORBED_KEY);
    }

    public static void addDamageAbsorbed(ItemStack stack, float amount) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            float current = tag.getFloat(DAMAGE_ABSORBED_KEY);
            tag.putFloat(DAMAGE_ABSORBED_KEY, current + amount);
        });
    }

    public static float getPieceMitigationBonus(ItemStack stack) {
        return getDamageAbsorbed(stack) * 0.01f;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof Player player) {
            handleArmorFeatures(player);
        }
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
        if (!level.isClientSide()) {
            handleArmorFeatures(player);
        }
    }

    private void handleArmorFeatures(Player player) {
        // Ejecutamos la lógica una sola vez por tick desde el Casco
        if (this.getType() == Type.HELMET) {
            if (hasFullSet(player)) {
                applySetEffects(player);
                applyHealthAndFlight(player);
            } else {
                removeHealthAndFlight(player);
            }
        }
    }

    private boolean hasFullSet(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        return head.getItem() instanceof EvolvingArmorItem &&
               chest.getItem() instanceof EvolvingArmorItem &&
               legs.getItem() instanceof EvolvingArmorItem &&
               feet.getItem() instanceof EvolvingArmorItem;
    }

    private void applySetEffects(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        float mitigation = getPieceMitigationBonus(chest);

        int amplifier = 0;
        if (mitigation >= 80.0f) {
            amplifier = 3;
        } else if (mitigation >= 40.0f) {
            amplifier = 2;
        } else if (mitigation >= 11.0f) {
            amplifier = 1;
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, Math.min(amplifier, 1), false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, amplifier, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, false, false, true));

        MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
        if (regen == null || regen.getDuration() <= 20 || regen.getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, amplifier, false, false, true));
        }
    }

    private void applyHealthAndFlight(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        float absorbed = getDamageAbsorbed(chest);
        float mitigation = getPieceMitigationBonus(chest);

        // 1. Salud extra: 1 corazón (2 puntos de vida) por cada 20 de daño absorbido
        // Hasta 20 corazones extra (40 puntos de vida adicionales -> total 40 corazones con los 10 base)
        int extraHearts = (int) (absorbed / 20.0f);
        double bonusHealth = Math.min(40.0, extraHearts * 2.0);

        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (bonusHealth > 0) {
                healthAttr.addTransientModifier(new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        bonusHealth,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        // 2. Vuelo: Al superar el 50% de mitigación
        if (mitigation >= 50.0f) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (!player.isCreative() && !player.isSpectator()) {
            if (player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    private void removeHealthAndFlight(Player player) {
        // Remover corazones extra si no tiene el set puesto
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_MODIFIER_ID) != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        // Desactivar vuelo si se quita la armadura
        if (!player.isCreative() && !player.isSpectator() && player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        float absorbed = getDamageAbsorbed(pStack);
        float mitigation = getPieceMitigationBonus(pStack);

        pTooltipComponents.add(Component.literal("Daño Absorbido: " + String.format("%.1f", absorbed)).withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(Component.literal("Mitigación de Pieza: +" + String.format("%.2f", mitigation) + "%").withStyle(ChatFormatting.GREEN));
        
        int extraHearts = Math.min(20, (int) (absorbed / 20.0f));
        pTooltipComponents.add(Component.literal("Vitalidad Extra: +" + extraHearts + " Corazones").withStyle(ChatFormatting.RED));

        if (mitigation >= 50.0f) {
            pTooltipComponents.add(Component.literal("Habilidad Desbloqueada: Vuelo Activo").withStyle(ChatFormatting.GOLD));
        }

        if (this.getType() == Type.BOOTS) {
            pTooltipComponents.add(Component.literal("Pasiva: Inmunidad al daño de caída.").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        int tier = 1;
        if (mitigation >= 80.0f) tier = 4;
        else if (mitigation >= 40.0f) tier = 3;
        else if (mitigation >= 11.0f) tier = 2;

        pTooltipComponents.add(Component.literal("Bonus de Set (Nivel " + tier + "): Resistencia, Velocidad y Regeneración").withStyle(ChatFormatting.YELLOW));

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
