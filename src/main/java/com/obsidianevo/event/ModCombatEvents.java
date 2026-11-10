package com.obsidianevo.event;

import com.obsidianevo.ObsidianEvoMod;
import com.obsidianevo.item.custom.EvolvingArmorItem;
import com.obsidianevo.item.custom.EvolvingSwordItem;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ObsidianEvoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCombatEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 1. CUANDO EL JUGADOR ATACA CON LA ESPADA (Suma hasta +1000 de daño)
        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack mainHand = attacker.getMainHandItem();
            if (mainHand.getItem() instanceof EvolvingSwordItem) {
                float bonusDamage = EvolvingSwordItem.getBonusDamage(mainHand);
                event.setAmount(event.getAmount() + bonusDamage);

                // Sube puntos de alma por cada golpe acertado
                EvolvingSwordItem.addSoulPoints(mainHand, 5);

                // Efectos adicionales según la fase evolutiva
                int stage = EvolvingSwordItem.getEvolutionStage(mainHand);
                LivingEntity target = event.getEntity();
                if (stage >= 1) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                if (stage >= 2) {
                    attacker.heal(Math.min(10.0f, event.getAmount() * 0.05f)); // Robo de vida
                }
            }
        }

        // 2. CUANDO EL JUGADOR RECIBE DAÑO CON LA ARMADURA
        if (event.getEntity() instanceof Player player) {
            // Verificar si es daño de caída y lleva las botas evolutivas
            if (event.getSource().is(DamageTypes.FALL)) {
                ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
                if (boots.getItem() instanceof EvolvingArmorItem) {
                    EvolvingArmorItem.addDamageAbsorbed(boots, event.getAmount());
                    event.setCanceled(true); // Anula por completo el daño de caída
                    return;
                }
            }

            int totalLevels = 0;
            int piecesEquipped = 0;
            float incomingDamage = event.getAmount();

            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack armorPiece = player.getItemBySlot(slot);
                if (armorPiece.getItem() instanceof EvolvingArmorItem) {
                    piecesEquipped++;
                    // Registra el daño en la pieza (hasta 5000)
                    EvolvingArmorItem.addDamageAbsorbed(armorPiece, incomingDamage * 0.25f);
                    totalLevels += EvolvingArmorItem.getPieceLevel(armorPiece);
                }
            }

            if (piecesEquipped > 0) {
                // Con 4 piezas al nivel 100 (400 niveles en total), mitiga hasta un 88% del daño total
                float damageReduction = Math.min(0.88f, totalLevels * 0.0022f);
                float finalDamage = incomingDamage * (1.0f - damageReduction);
                event.setAmount(finalDamage);

                // Refleja parte del daño al atacante
                if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                    float reflectPercent = totalLevels * 0.003f; // Hasta 120% de reflejo al máximo nivel
                    attacker.hurt(player.damageSources().thorns(player), incomingDamage * reflectPercent);
                }
            }
        }
    }
}
