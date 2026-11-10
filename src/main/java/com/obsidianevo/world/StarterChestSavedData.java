package com.obsidianevo.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class StarterChestSavedData extends SavedData {
    private static final String DATA_NAME = "obsidian_evo_starter_chest";
    private boolean chestSpawned = false;

    public static StarterChestSavedData create() {
        return new StarterChestSavedData();
    }

    public static StarterChestSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        StarterChestSavedData data = create();
        data.chestSpawned = tag.getBoolean("ChestSpawned");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("ChestSpawned", this.chestSpawned);
        return tag;
    }

    public boolean isChestSpawned() {
        return this.chestSpawned;
    }

    public void setChestSpawned(boolean spawned) {
        this.chestSpawned = spawned;
        this.setDirty();
    }

    public static StarterChestSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(StarterChestSavedData::create, StarterChestSavedData::load, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }
}
