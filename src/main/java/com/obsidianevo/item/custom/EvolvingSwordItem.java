package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EvolvingSwordItem extends SwordItem {
    public static final String SOUL_POINTS_KEY = "SoulPoints";

    public EvolvingSwordItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    public static int getSoulPoints(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getInt(SOUL_POINTS_KEY);
    }

    public static void addSoulPoints(ItemStack stack, int points) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            int current = tag.getInt(SOUL_POINTS_KEY);
            tag.putInt(SOUL_POINTS_KEY, current + points);
        });
    }

    public static int getEvolutionStage(ItemStack stack) {
        int pts = getSoulPoints(stack);
        if (pts >= 600) return 3;
        if (pts >= 300) return 2;
        if (pts >= 100) return 1;
        return 0;
    }

    public static float getBonusDamage(ItemStack stack) {
        return Math.min(25.0f, getSoulPoints(stack) / 20.0f);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        int stage = getEvolutionStage(pStack);
        if (stage >= 1) {
            pTarget.igniteForSeconds(4);
        }
        if (stage >= 2) {
            pTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            pAttacker.heal(2.0f);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide() && getEvolutionStage(stack) >= 3) {
            AABB area = pPlayer.getBoundingBox().inflate(5.0D);
            List<LivingEntity> enemies = pLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != pPlayer);
            for (LivingEntity enemy : enemies) {
                enemy.hurt(pPlayer.damageSources().playerAttack(pPlayer), 12.0f);
                enemy.knockback(1.5F, pPlayer.getX() - enemy.getX(), pPlayer.getZ() - enemy.getZ());
            }
            pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
            pPlayer.getCooldowns().addCooldown(this, 100);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        int pts = getSoulPoints(pStack);
        int stage = getEvolutionStage(pStack);
        float bonusDamage = getBonusDamage(pStack);

        String[] stageNames = {"Dormida (Fase 0)", "Despertada (Fase 1)", "Ascendida (Fase 2)", "Definitiva (Fase 3)"};
        pTooltipComponents.add(Component.literal("Estado: " + stageNames[stage]).withStyle(ChatFormatting.LIGHT_PURPLE));
        pTooltipComponents.add(Component.literal("Puntos de Alma: " + pts).withStyle(ChatFormatting.DARK_RED));
        pTooltipComponents.add(Component.literal("Daño evolutivo: +" + String.format("%.1f", bonusDamage)).withStyle(ChatFormatting.GOLD));

        if (stage >= 3) {
            pTooltipComponents.add(Component.literal("[Clic Derecho] Onda de Almas desbloqueada").withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
