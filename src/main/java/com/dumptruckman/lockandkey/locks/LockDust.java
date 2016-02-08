/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LockDust {

    private static final String DUST_CODE = ChatColor.COLOR_CHAR + "*";
    private static final List<String> DUST_LORE;
    private static final List<String> DUST_BLOCK_LORE;
    static {
        DUST_LORE = new ArrayList<>(4);
        DUST_LORE.add("This dust contains the");
        DUST_LORE.add("magic required to lock");
        DUST_LORE.add("objects so that only");
        DUST_LORE.add("their owner may use them.");

        DUST_BLOCK_LORE = new ArrayList<>(4);
        DUST_BLOCK_LORE.add("A concentrated form of");
        DUST_BLOCK_LORE.add("Sealing Dust");
    }

    public static ItemStack createLockDust(int amount) {
        ItemStack itemStack = new ItemStack(Material.REDSTONE, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.GOLD + "Sealing Dust" + DUST_CODE);
        meta.setLore(DUST_LORE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack createDustBlock(int amount) {
        ItemStack itemStack = new ItemStack(Material.REDSTONE_BLOCK, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.GOLD + "Concentrated Sealing Dust" + DUST_CODE);
        meta.setLore(DUST_BLOCK_LORE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
