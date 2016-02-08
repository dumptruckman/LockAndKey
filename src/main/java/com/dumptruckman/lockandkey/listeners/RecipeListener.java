/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.listeners;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.locks.LockDust;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class RecipeListener implements Listener {

    @NotNull
    private final LockAndKeyPlugin plugin;

    public RecipeListener(@NotNull final LockAndKeyPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void dustBlockRecipe(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult().getType() != Material.REDSTONE_BLOCK) {
            return;
        }
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (matrix.length < 9) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (matrix[i] == null) {
                return;
            }
            if (!plugin.isDustItem(matrix[i])) {
                return;
            }
        }
        event.getInventory().setResult(plugin.createDustBlock(1));
    }

    @EventHandler
    public void dustRecipe(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult().getType() != Material.REDSTONE) {
            return;
        }
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (item != null && item.getType() == Material.REDSTONE_BLOCK) {
                if (!plugin.isDustItem(item)) {
                    return;
                }
            }
        }
        event.getInventory().setResult(plugin.createSealingDust(9));
    }

    @EventHandler
    public void lockRecipe(PrepareItemCraftEvent event) {
        if (!plugin.getExampleLockItems().contains(event.getRecipe().getResult())) {
            return;
        }
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (item != null && item.getType() == Material.REDSTONE) {
                if (!plugin.isDustItem(item)) {
                    event.getInventory().setResult(null);
                }
            }
        }
    }
}
