package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Random;

public class EvolvingSwordItem extends SwordItem {
    public static final String SOUL_POINTS_KEY = "SoulPoints";
    public static final float MAX_BONUS_DAMAGE = 1000.0f;
    private static final Random RANDOM = new Random();

    public EvolvingSwordItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    public static int getSoulPoints(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getInt(SOUL_POINTS_KEY);
    }

    public static void addSoulPoints(ItemStack stack, int amount) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            int current = tag.getInt(SOUL_POINTS_KEY);
            tag.putInt(SOUL_POINTS_KEY, current + amount);
        });
    }

    // Cada 5 Soul Points otorga +1 de daño, hasta un máximo de +1000 de daño (5000 Soul Points)
    public static float getBonusDamage(ItemStack stack) {
        return Math.min(MAX_BONUS_DAMAGE, getSoulPoints(stack) / 5.0f);
    }

    public static int getEvolutionStage(ItemStack stack) {
        float bonus = getBonusDamage(stack);
        if (bonus >= 1000.0f) return 4;
        if (bonus >= 500.0f) return 3;
        if (bonus >= 150.0f) return 2;
        if (bonus >= 50.0f) return 1;
        return 0;
    }

    // Dureza progresiva: Entre más se usa, menos durabilidad gasta (hasta 95% de reducción de desgaste)
    @Override
    public void setDamage(ItemStack stack, int damage) {
        int currentDamage = this.getDamage(stack);
        if (damage > currentDamage) {
            float progress = Math.min(1.0f, getBonusDamage(stack) / MAX_BONUS_DAMAGE);
            float saveChance = progress * 0.95f;
            if (RANDOM.nextFloat() < saveChance) {
                return;
            }
        }
        super.setDamage(stack, damage);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        int souls = getSoulPoints(pStack);
        float bonusDamage = getBonusDamage(pStack);
        int stage = getEvolutionStage(pStack);
        int hardnessBonus = (int) (Math.min(1.0f, bonusDamage / MAX_BONUS_DAMAGE) * 95);

        pTooltipComponents.add(Component.literal("Fase Evolutiva: " + stage + "/4").withStyle(ChatFormatting.DARK_PURPLE));
        pTooltipComponents.add(Component.literal("Puntos de Alma: " + souls).withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(Component.literal("Daño Adicional: +" + String.format("%.1f", bonusDamage) + " / 1000.0").withStyle(ChatFormatting.RED));
        pTooltipComponents.add(Component.literal("Dureza Evolutiva: +" + hardnessBonus + "% resistencia al desgaste").withStyle(ChatFormatting.GREEN));

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Random;

public class EvolvingSwordItem extends SwordItem {
    public static final String SOUL_POINTS_KEY = "SoulPoints";
    public static final float MAX_BONUS_DAMAGE = 1000.0f;
    private static final Random RANDOM = new Random();

    public EvolvingSwordItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    public static int getSoulPoints(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getInt(SOUL_POINTS_KEY);
    }

    public static void addSoulPoints(ItemStack stack, int amount) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            int current = tag.getInt(SOUL_POINTS_KEY);
            tag.putInt(SOUL_POINTS_KEY, current + amount);
        });
    }

    // Cada 5 Soul Points otorga +1 de daño, hasta un máximo de +1000 de daño (5000 Soul Points)
    public static float getBonusDamage(ItemStack stack) {
        return Math.min(MAX_BONUS_DAMAGE, getSoulPoints(stack) / 5.0f);
    }

    public static int getEvolutionStage(ItemStack stack) {
        float bonus = getBonusDamage(stack);
        if (bonus >= 1000.0f) return 4;
        if (bonus >= 500.0f) return 3;
        if (bonus >= 150.0f) return 2;
        if (bonus >= 50.0f) return 1;
        return 0;
    }

    // Dureza progresiva: Entre más se usa, menos durabilidad gasta (hasta 95% de reducción de desgaste)
    @Override
    public void setDamage(ItemStack stack, int damage) {
        int currentDamage = this.getDamage(stack);
        if (damage > currentDamage) {
            float progress = Math.min(1.0f, getBonusDamage(stack) / MAX_BONUS_DAMAGE);
            float saveChance = progress * 0.95f;
            if (RANDOM.nextFloat() < saveChance) {
                return;
            }
        }
        super.setDamage(stack, damage);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        int souls = getSoulPoints(pStack);
        float bonusDamage = getBonusDamage(pStack);
        int stage = getEvolutionStage(pStack);
        int hardnessBonus = (int) (Math.min(1.0f, bonusDamage / MAX_BONUS_DAMAGE) * 95);

        pTooltipComponents.add(Component.literal("Fase Evolutiva: " + stage + "/4").withStyle(ChatFormatting.DARK_PURPLE));
        pTooltipComponents.add(Component.literal("Puntos de Alma: " + souls).withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(Component.literal("Daño Adicional: +" + String.format("%.1f", bonusDamage) + " / 1000.0").withStyle(ChatFormatting.RED));
        pTooltipComponents.add(Component.literal("Dureza Evolutiva: +" + hardnessBonus + "% resistencia al desgaste").withStyle(ChatFormatting.GREEN));

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
