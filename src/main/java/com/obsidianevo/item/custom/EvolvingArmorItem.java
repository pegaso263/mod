package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
            tag.putFloat(DAMAGE_ABSORBED_KEY, current + amount);
        });
    }

    public static int getPieceLevel(ItemStack stack) {
        return Math.min(10, (int) (getDamageAbsorbed(stack) / 50.0f));
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide() && pEntity instanceof Player player && player.tickCount % 40 == 0) {
            if (hasFullSet(player)) {
                int totalLevels = getTotalSetLevels(player);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
                if (totalLevels >= 20) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
                }
                if (totalLevels >= 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false, true));
                }
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    private boolean hasFullSet(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!(armorStack.getItem() instanceof EvolvingArmorItem)) {
                return false;
            }
        }
        return true;
    }

    private int getTotalSetLevels(Player player) {
        int total = 0;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof EvolvingArmorItem) {
                total += getPieceLevel(armorStack);
            }
        }
        return total;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        float absorbed = getDamageAbsorbed(pStack);
        int level = getPieceLevel(pStack);

        pTooltipComponents.add(Component.literal("Nivel de Adaptación: " + level + "/10").withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(Component.literal("Daño absorbido: " + String.format("%.1f", absorbed)).withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.literal("Reducción: +" + (level * 1.5f) + "% | Reflejo: " + (level * 3) + "%").withStyle(ChatFormatting.GREEN));
        pTooltipComponents.add(Component.literal("Bonus de Set (4 pzas): Resistencia, Velocidad y Regeneración.").withStyle(ChatFormatting.GOLD));

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
