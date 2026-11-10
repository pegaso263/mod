package com.obsidianevo.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModToolTiers {
    public static final Tier OBSIDIAN_EVO_TIER = new ForgeTier(
        2000,
        1.0f,
        0.0f,
        15,
        BlockTags.NEEDS_STONE_TOOL,
        () -> Ingredient.of(Items.OBSIDIAN),
        BlockTags.INCORRECT_FOR_STONE_TOOL
    );
}
