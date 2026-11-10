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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ObsidianEvoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCombatEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 1. DAÑO DE LA ESPADA (Suma daño y otorga 1 punto de alma por baja)
        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack mainHand = attacker.getMainHandItem();
            if (mainHand.getItem() instanceof EvolvingSwordItem) {
                float bonusDamage = EvolvingSwordItem.getBonusDamage(mainHand);
                event.setAmount(event.getAmount() + bonusDamage);

                int stage = EvolvingSwordItem.getEvolutionStage(mainHand);
                LivingEntity target = event.getEntity();
                if (stage >= 1) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                if (stage >= 2 && attacker.getHealth() < attacker.getMaxHealth()) {
                    attacker.heal(Math.min(4.0f, event.getAmount() * 0.03f));
                }
            }
        }

        // 2. SISTEMA DE ADAPTACIÓN INFINITA DE LA ARMADURA
        if (event.getEntity() instanceof Player player) {
            // Inmunidad al daño de caída con las botas
            if (event.getSource().is(DamageTypes.FALL)) {
                ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
                if (boots.getItem() instanceof EvolvingArmorItem) {
                    EvolvingArmorItem.addDamageAbsorbed(boots, event.getAmount());
                    event.setCanceled(true);
                    return;
                }
            }

            float incomingDamage = event.getAmount();
            float totalMitigationBonus = 0.0f;
            int piecesEquipped = 0;

            // Recorre las 4 piezas para aplicar absorción 1:1 y acumular mitigación
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack armorPiece = player.getItemBySlot(slot);
                if (armorPiece.getItem() instanceof EvolvingArmorItem) {
                    piecesEquipped++;
                    // CADA 1 DE DAÑO RECIBIDO SUMA 1 PUNTO DE ABSORCIÓN A CADA PIEZA
                    EvolvingArmorItem.addDamageAbsorbed(armorPiece, incomingDamage);
                    totalMitigationBonus += EvolvingArmorItem.getPieceMitigationBonus(armorPiece);
                }
            }

            if (piecesEquipped > 0) {
                // Límite de seguridad del 95% de mitigación extra para mantener el juego divertido
                float finalReduction = Math.min(0.95f, totalMitigationBonus / 100.0f);
                float reducedDamage = incomingDamage * (1.0f - finalReduction);
                event.setAmount(reducedDamage);

                // Reflejo de daño proporcional al atacante
                if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                    float reflectPercent = totalMitigationBonus * 0.005f;
                    attacker.hurt(player.damageSources().thorns(player), incomingDamage * reflectPercent);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 3. PUNTOS DE ALMA DE LA ESPADA AL ELIMINAR UNA ENTIDAD (1 punto por baja)
        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack mainHand = attacker.getMainHandItem();
            if (mainHand.getItem() instanceof EvolvingSwordItem) {
                EvolvingSwordItem.addSoulPoints(mainHand, 1);
                attacker.heal(6.0f);
            }
        }
    }
}
