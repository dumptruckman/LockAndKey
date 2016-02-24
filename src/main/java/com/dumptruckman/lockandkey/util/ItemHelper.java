/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.util;

import com.dumptruckman.lockandkey.locks.LockMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemHelper {

    private static final String LOCK_NODE_NAME = "isLock";
    private static final String DUST_NODE_NAME = "isDust";
    private static final String KEY_NODE_NAME = "isKey";
    private static final String KEY_CODE_KEY = "keyCode";

    public static ItemHelper builder(@NotNull Material material, int amount) {
        return new ItemHelper(new ItemStack(material, amount), false);
    }

    public static ItemHelper builder(@NotNull ItemStack item) {
        return new ItemHelper(item, true);
    }

    @Contract("null -> false")
    public static boolean isLockItem(@Nullable ItemStack item) {
        return item != null && LockMaterial.getByItemMaterial(item.getType()) != null
                && hasNode(item, LOCK_NODE_NAME);
    }

    @Contract("null -> false")
    public static boolean isDustItem(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.REDSTONE
                && hasNode(item, DUST_NODE_NAME);
    }

    @Contract("null -> false")
    public static boolean isDustBlockItem(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.REDSTONE_BLOCK
                && hasNode(item, DUST_NODE_NAME);
    }

    @Contract("null -> false")
    public static boolean isKeyItem(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.TRIPWIRE_HOOK
                && hasNode(item, KEY_NODE_NAME);
    }

    public static boolean isBlankKeyItem(@NotNull ItemStack item) {
        if (!isKeyItem(item)) {
            return false;
        }
        String keyCode = getKeyCode(item);
        return keyCode == null || keyCode.isEmpty();
    }

    @Nullable
    public static String getKeyCode(@NotNull ItemStack item) {
        return CompatibilityUtils.getMeta(item, KEY_CODE_KEY);
    }

    public static ItemStack setKeyCode(@NotNull ItemStack item, @Nullable String keyCode) {
        if (keyCode == null) {
            CompatibilityUtils.removeMeta(item, KEY_CODE_KEY);
        } else {
            CompatibilityUtils.setMeta(item, KEY_CODE_KEY, keyCode);
        }
        return item;
    }

    private static void createNode(@NotNull ItemStack item, @NotNull String nodeName) {
        CompatibilityUtils.setMeta(item, nodeName, "true");
    }

    private static boolean hasNode(@NotNull ItemStack item, @NotNull String nodeName) {
        return CompatibilityUtils.hasMeta(item, nodeName);
    }

    @NotNull
    private ItemStack item;
    boolean real;

    private ItemHelper(@NotNull ItemStack item, boolean real) {
        this.item = item;
        this.real = real;
    }

    private void makeReal() {
        if (!real) {
            this.item = CompatibilityUtils.makeReal(item);
        }
    }

    private void createNode(@NotNull String nodeName) {
        makeReal();
        createNode(item, nodeName);
    }

    public ItemHelper createLockData() {
        createNode(LOCK_NODE_NAME);
        return this;
    }

    public ItemHelper createDustData() {
        createNode(DUST_NODE_NAME);
        return this;
    }

    public ItemHelper createKeyData() {
        createNode(KEY_NODE_NAME);
        return this;
    }

    public ItemHelper setKeyCode(@Nullable String keyCode) {
        setKeyCode(item, keyCode);
        return this;
    }

    public ItemHelper makeUnplaceable() {
        makeReal();
        CompatibilityUtils.makeUnplaceable(item);
        return this;
    }

    public ItemHelper setName(@NotNull String name) {
        makeReal();
        CompatibilityUtils.setDisplayName(item, name);
        return this;
    }

    public ItemHelper setLore(@NotNull List<String> lore) {
        makeReal();
        CompatibilityUtils.setLore(item, lore);
        return this;
    }

    public ItemHelper addGlow() {
        makeReal();
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        //CompatibilityUtils.addGlow(item);
        return this;
    }

    public ItemStack buildItem() {
        return item;
    }
}
