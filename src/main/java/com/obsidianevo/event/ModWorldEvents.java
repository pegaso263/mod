package com.obsidianevo.event;

import com.obsidianevo.ObsidianEvoMod;
import com.obsidianevo.item.ModItems;
import com.obsidianevo.world.StarterChestSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ObsidianEvoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModWorldEvents {

    @SubscribeEvent
    public static void onPlayerFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            StarterChestSavedData data = StarterChestSavedData.get(serverLevel);

            if (!data.isChestSpawned()) {
                ServerLevel overworld = serverLevel.getServer().overworld();
                BlockPos spawnPos = overworld.getSharedSpawnPos();

                double angle = overworld.random.nextDouble() * Math.PI * 2.0D;
                int targetX = spawnPos.getX() + (int) Math.round(Math.cos(angle) * 50.0D);
                int targetZ = spawnPos.getZ() + (int) Math.round(Math.sin(angle) * 50.0D);

                overworld.getChunk(targetX >> 4, targetZ >> 4);
                int targetY = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
                BlockPos chestPos = new BlockPos(targetX, targetY, targetZ);

                BlockPos belowPos = chestPos.below();
                if (!overworld.getBlockState(belowPos).isSolid()) {
                    overworld.setBlock(belowPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }

                overworld.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

                BlockEntity blockEntity = overworld.getBlockEntity(chestPos);
                if (blockEntity instanceof ChestBlockEntity chest) {
                    chest.setItem(11, new ItemStack(ModItems.CASCO_EVOLUTIVO.get()));
                    chest.setItem(12, new ItemStack(ModItems.PETO_EVOLUTIVO.get()));
                    chest.setItem(13, new ItemStack(ModItems.ESPADA_EVOLUTIVA.get()));
                    chest.setItem(14, new ItemStack(ModItems.GREBAS_EVOLUTIVAS.get()));
                    chest.setItem(15, new ItemStack(ModItems.BOTAS_EVOLUTIVAS.get()));
                }

                data.setChestSpawned(true);

                player.sendSystemMessage(
                        Component.literal("Un cofre con reliquias de obsidiana ha aparecido a 50 bloques del Spawn ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("[" + chestPos.getX() + ", " + chestPos.getY() + ", " + chestPos.getZ() + "]")
                                        .withStyle(ChatFormatting.AQUA))
                );
            }
        }
    }
}
