package com.obsidianevo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof Player player) {
            // Verificamos desde el casco para ejecutar el chequeo solo una vez por tick
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
        // Velocidad I, Resistencia I y Resistencia al Fuego (220 ticks = 11 segundos)
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, false, false, true));

        // Regeneración I: Solo se renueva cuando quedan 20 ticks (1 seg) o menos,
        // permitiendo que se cumpla el ciclo de 50 ticks (2.5 seg) para curar vida.
        MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
        if (regen == null || regen.getDuration() <= 20) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false, true));
        }
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
