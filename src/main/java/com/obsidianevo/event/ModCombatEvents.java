package com.obsidianevo.event;

import com.obsidianevo.ObsidianEvoMod;
import com.obsidianevo.item.custom.EvolvingArmorItem;
import com.obsidianevo.item.custom.EvolvingSwordItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ObsidianEvoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCombatEvents {

    @SubscribeEvent
    public static void onEnemyKilled(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        LivingEntity victim = event.getEntity();

        if (killer instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof EvolvingSwordItem) {
                int oldStage = EvolvingSwordItem.getEvolutionStage(mainHandItem);
                int pointsEarned = calculateEnemyPoints(victim);
                EvolvingSwordItem.addSoulPoints(mainHandItem, pointsEarned);
                int newStage = EvolvingSwordItem.getEvolutionStage(mainHandItem);

                if (newStage > oldStage) {
                    player.displayClientMessage(
                            Component.literal("¡Tu espada de obsidiana ha evolucionado a la Fase " + newStage + "!")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                            true
                    );
                    player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
    }

    private static int calculateEnemyPoints(LivingEntity victim) {
        if (victim instanceof EnderDragon || victim instanceof WitherBoss || victim instanceof Warden) {
            return 100;
        }
        float maxHealth = victim.getMaxHealth();
        if (maxHealth >= 150.0f) return 50;
        if (maxHealth >= 40.0f) return 5;
        return 1;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.getItem() instanceof EvolvingSwordItem) {
                float bonusDamage = EvolvingSwordItem.getBonusDamage(weapon);
                event.setAmount(event.getAmount() + bonusDamage);
            }
        }

        if (event.getEntity() instanceof Player defender) {
            float incomingDamage = event.getAmount();
            int totalArmorLevels = 0;
            int equippedPieces = 0;

            for (ItemStack armorStack : defender.getArmorSlots()) {
                if (armorStack.getItem() instanceof EvolvingArmorItem) {
                    equippedPieces++;
                    EvolvingArmorItem.addDamageAbsorbed(armorStack, incomingDamage);
                    totalArmorLevels += EvolvingArmorItem.getPieceLevel(armorStack);
                }
            }

            if (equippedPieces > 0) {
                float damageReductionPercent = Math.min(0.60f, totalArmorLevels * 0.015f);
                event.setAmount(incomingDamage * (1.0f - damageReductionPercent));

                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity livingAttacker) {
                    float reflectedDamage = incomingDamage * (totalArmorLevels * 0.03f);
                    if (reflectedDamage > 0.5f) {
                        livingAttacker.hurt(defender.damageSources().thorns(defender), reflectedDamage);
                    }
                }
            }
        }
    }
}
