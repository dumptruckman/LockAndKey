/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import com.google.common.base.CaseFormat;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum LockMaterial {

    WOODEN_DOOR(Material.OAK_DOOR, true),
    WOOD_DOOR(Material.OAK_DOOR, true),
    OAK_DOOR(Material.OAK_DOOR, true),
    IRON_DOOR(Material.IRON_DOOR, true),
    ACACIA_DOOR(Material.ACACIA_DOOR, true),
    BIRCH_DOOR(Material.BIRCH_DOOR, true),
    JUNGLE_DOOR(Material.JUNGLE_DOOR, true),
    SPRUCE_DOOR(Material.SPRUCE_DOOR, true),
    DARK_OAK_DOOR(Material.DARK_OAK_DOOR, true),
    TRAP_DOOR(Material.OAK_TRAPDOOR),
    TRAPDOOR(Material.OAK_TRAPDOOR),
    IRON_TRAPDOOR(Material.IRON_TRAPDOOR),
    ACACIA_TRAPDOOR(Material.ACACIA_TRAPDOOR),
    BIRCH_TRAPDOOR(Material.BIRCH_TRAPDOOR),
    JUNGLE_TRAPDOOR(Material.JUNGLE_TRAPDOOR),
    SPRUCE_TRAPDOOR(Material.SPRUCE_TRAPDOOR),
    DARK_OAK_TRAPDOOR(Material.DARK_OAK_TRAPDOOR),
    LEVER(Material.LEVER),
    WOOD_PLATE(Material.OAK_PRESSURE_PLATE, Action.PHYSICAL),
    STONE_PLATE(Material.STONE_PRESSURE_PLATE, Action.PHYSICAL),
    IRON_PLATE(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, "Weighted Pressure Plate (Heavy)", Action.PHYSICAL),
    GOLD_PLATE(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "Weighted Pressure Plate (Light)", Action.PHYSICAL),
    WOOD_BUTTON(Material.OAK_BUTTON),
    STONE_BUTTON(Material.STONE_BUTTON),
    ACACIA_BUTTON(Material.ACACIA_BUTTON),
    BIRCH_BUTTON(Material.BIRCH_BUTTON),
    JUNGLE_BUTTON(Material.JUNGLE_BUTTON),
    SPRUCE_BUTTON(Material.SPRUCE_BUTTON),
    DARK_OAK_BUTTON(Material.DARK_OAK_BUTTON),
    FENCE_GATE(Material.OAK_FENCE_GATE),
    OAK_FENCE_GATE(Material.OAK_FENCE_GATE),
    ACACIA_FENCE_GATE(Material.ACACIA_FENCE_GATE),
    BIRCH_FENCE_GATE(Material.BIRCH_FENCE_GATE),
    DARK_OAK_FENCE_GATE(Material.DARK_OAK_FENCE_GATE),
    JUNGLE_FENCE_GATE(Material.JUNGLE_FENCE_GATE),
    SPRUCE_FENCE_GATE(Material.SPRUCE_FENCE_GATE),
    CHEST(Material.CHEST) {
        @Override
        public boolean isChest() {
            return true;
        }
    },
    FURNACE(Material.FURNACE),
    HOPPER(Material.HOPPER),
    DROPPER(Material.DROPPER),
    DISPENSER(Material.DISPENSER),
    ;

    private static final Map<Material, LockMaterial> itemLookupMap = new HashMap<>(LockMaterial.values().length);
    private static final Map<Material, LockMaterial> blockLookupMap = new HashMap<>(LockMaterial.values().length);
    static {
        for (LockMaterial lockMaterial : LockMaterial.values()) {
            itemLookupMap.put(lockMaterial.item, lockMaterial);
            blockLookupMap.put(lockMaterial.block, lockMaterial);
        }
    }

    @Nullable
    public static LockMaterial getByItemMaterial(@NotNull Material material) {
        return itemLookupMap.get(material);
    }

    @Nullable
    public static LockMaterial getByBlockMaterial(@NotNull Material material) {
        return blockLookupMap.get(material);
    }

    @NotNull
    private final Material item;
    @NotNull
    private final Material block;
    @NotNull
    private final String itemName;
    private final boolean isDoor;
    private final Action action;

    LockMaterial(@NotNull Material item) {
        this(item, item, false);
    }

    LockMaterial(@NotNull Material item, boolean isDoor) {
        this(item, item, isDoor);
    }

    LockMaterial(@NotNull Material item, @NotNull Action action) {
        this(item, item, false, action);
    }

    LockMaterial(@NotNull Material item, @NotNull Material block, boolean isDoor) {
        this(item, block, formatName(block.name()), isDoor);
    }

    LockMaterial(@NotNull Material item, @NotNull Material block, boolean isDoor, @NotNull final Action action) {
        this(item, block, formatName(block.name()), isDoor, action);
    }

    LockMaterial(@NotNull Material item, @NotNull String name) {
        this(item, item, name, false);
    }

    LockMaterial(@NotNull Material item, @NotNull String name, @NotNull Action action) {
        this(item, item, name, false, action);
    }

    LockMaterial(@NotNull Material item, @NotNull Material block, @NotNull String name, boolean isDoor) {
        this(item, block, name, isDoor, Action.RIGHT_CLICK_BLOCK);
    }

    LockMaterial(@NotNull Material item, @NotNull Material block, @NotNull String name, boolean isDoor, @NotNull Action action) {
        this.item = item;
        this.block = block;
        this.itemName = name;
        this.isDoor = isDoor;
        this.action = action;
    }

    @NotNull
    public String getItemName() {
        return itemName;
    }

    public boolean isDoor() {
        return isDoor;
    }

    public boolean isChest() {
        return false;
    }

    @NotNull
    public Material getItemMaterial() {
        return item;
    }

    public Action getInteractAction() {
        return action;
    }

    private static String formatName(@NotNull String itemName) {
        String[] split = itemName.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : split) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, part));
        }
        return builder.toString();
    }
}
