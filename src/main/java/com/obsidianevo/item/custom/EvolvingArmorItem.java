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
    public static final float MAX_DAMAGE_ABSORBED = 5000.0f;
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
            tag.putFloat(DAMAGE_ABSORBED_KEY, Math.min(MAX_DAMAGE_ABSORBED, current + amount));
        });
    }

    // Escala de 0 a 100 niveles (1 nivel cada 50 de daño, hasta 5000)
    public static int getPieceLevel(ItemStack stack) {
        return Math.min(100, (int) (getDamageAbsorbed(stack) / 50.0f));
    }

    // Dureza progresiva: Entre más se usa y sube de nivel, más difícil es que pierda durabilidad (hasta 90% irrompible)
    @Override
    public void setDamage(ItemStack stack, int damage) {
        int currentDamage = this.getDamage(stack);
        if (damage > currentDamage) {
            float progress = getDamageAbsorbed(stack) / MAX_DAMAGE_ABSORBED; // 0.0 a 1.0
            float saveChance = progress * 0.90f; // Hasta 90% de ignorar desgaste
            if (RANDOM.nextFloat() < saveChance) {
                return; // Cancela la pérdida de durabilidad
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
        int level = getPieceLevel(pStack);
        int hardnessBonus = (int) ((absorbed / MAX_DAMAGE_ABSORBED) * 90);

        pTooltipComponents.add(Component.literal("Nivel de Adaptación: " + level + "/100").withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(Component.literal("Daño absorbido: " + String.format("%.1f", absorbed) + " / 5000").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.literal("Mitigación: +" + String.format("%.1f", level * 0.22f) + "% | Dureza extra: +" + hardnessBonus + "%").withStyle(ChatFormatting.GREEN));
        if (this.getType() == Type.BOOTS) {
            pTooltipComponents.add(Component.literal("Pasiva de Botas: Inmunidad al daño de caída.").withStyle(ChatFormatting.BLUE));
        }
        pTooltipComponents.add(Component.literal("Bonus de Set (4 pzas): Resistencia, Velocidad y Regeneración.").withStyle(ChatFormatting.GOLD));

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
