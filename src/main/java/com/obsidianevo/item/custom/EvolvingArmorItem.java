package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

public class EvolvingArmorItem extends ArmorItem {
    public static final String DAMAGE_ABSORBED_KEY = "DamageAbsorbed";
    private static final Random RANDOM = new Random();

    public EvolvingArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    public static float getDamageAbsorbed(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getFloat(DAMAGE_ABSORBED_KEY);
    }

    public static void addDamageAbsorbed(ItemStack stack, float amount) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            float current = tag.getFloat(DAMAGE_ABSORBED_KEY);
            // Crecimiento casi infinito (sin tope artificial bajo)
            tag.putFloat(DAMAGE_ABSORBED_KEY, current + amount);
        });
    }

    // Cada punto de daño absorbido otorga 0.01% extra de mitigación por pieza (crecimiento continuo)
    public static float getPieceMitigationBonus(ItemStack stack) {
        return getDamageAbsorbed(stack) * 0.01f; 
    }

    // Dureza progresiva: Entre más daño absorbe, más irrompible se vuelve (hasta 98% de probabilidad de no gastar durabilidad)
    @Override
    public void setDamage(ItemStack stack, int damage) {
        int currentDamage = this.getDamage(stack);
        if (damage > currentDamage) {
            float absorbed = getDamageAbsorbed(stack);
            float saveChance = Math.min(0.98f, absorbed / 10000.0f); // Se vuelve casi indestructible a los 10k de daño absorbido
            if (RANDOM.nextFloat() < saveChance) {
                return;
            }
        }
        super.setDamage(stack, damage);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof Player player) {
            if (this.getType() == Type.HELMET && hasFullSet(player)) {
                applySetEffects(player);
            }
        }
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
        if (!level.isClientSide()) {
            if (this.getType() == Type.HELMET && hasFullSet(player)) {
                applySetEffects(player);
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
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, false, false, true));

        MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
        if (regen == null || regen.getDuration() <= 20) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false, true));
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        float absorbed = getDamageAbsorbed(pStack);
        float mitigation = getPieceMitigationBonus(pStack);
        int hardnessBonus = (int) (Math.min(1.0f, absorbed / 10000.0f) * 98);

        pTooltipComponents.add(Component.literal("Daño Absorbido: " + String.format("%.1f", absorbed)).withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(Component.literal("Mitigación de Pieza: +" + String.format("%.2f", mitigation) + "%").withStyle(ChatFormatting.GREEN));
        pTooltipComponents.add(Component.literal("Dureza Evolutiva: +" + hardnessBonus + "% resistencia al desgaste").withStyle(ChatFormatting.GOLD));
        if (this.getType() == Type.BOOTS) {
            pTooltipComponents.add(Component.literal("Pasiva: Inmunidad al daño de caída.").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        pTooltipComponents.add(Component.literal("Bonus de Set (4 pzas): Resistencia, Velocidad y Regeneración.").withStyle(ChatFormatting.YELLOW));

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
