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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // Solo ejecutar en el servidor y cuando el portador es un jugador
        if (!level.isClientSide() && entity instanceof Player player) {
            // Verificar que este ítem sea el casco (para no ejecutar el chequeo 4 veces por tick)
            // y que el jugador tenga las 4 piezas del set equipadas
            if (stack.getItem() == this && player.getItemBySlot(EquipmentSlot.HEAD) == stack) {
                if (hasFullSet(player)) {
                    applySetEffects(player);
                }
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
        // Velocidad I y Resistencia al Fuego se pueden refrescar continuamente (220 ticks = 11 seg)
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, false, false, true));

        // IMPORTANTE PARA REGENERACIÓN: Solo aplicarla si no la tiene o si le quedan menos de 20 ticks,
        // de lo contrario el contador de curación se reinicia y nunca cura.
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
