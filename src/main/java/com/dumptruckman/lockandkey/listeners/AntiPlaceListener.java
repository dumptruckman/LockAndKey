/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.listeners;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.util.CompatibilityUtils;
import com.dumptruckman.lockandkey.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AntiPlaceListener implements Listener {

    @NotNull
    private final LockAndKeyPlugin plugin;

    public AntiPlaceListener(@NotNull final LockAndKeyPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void preventPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null) {
            return;
        }
        if (CompatibilityUtils.isUnplaceable(item)) {
            event.setCancelled(true);
            return;
        }
        if (item.getType() != Material.CHEST) {
            return;
        }
        boolean isLock = ItemHelper.isLockItem(item);
        Block block = event.getBlock();
        Block relative = block.getRelative(BlockFace.NORTH);
        if (relative.getType() == Material.CHEST && isLock && plugin.getLockRegistry().getLock(relative) == null) {
            event.setCancelled(true);
            return;
        }
        relative = block.getRelative(BlockFace.SOUTH);
        if (relative.getType() == Material.CHEST && isLock && plugin.getLockRegistry().getLock(relative) == null) {
            event.setCancelled(true);
            return;
        }
        relative = block.getRelative(BlockFace.EAST);
        if (relative.getType() == Material.CHEST && isLock && plugin.getLockRegistry().getLock(relative) == null) {
            event.setCancelled(true);
            return;
        }
        relative = block.getRelative(BlockFace.WEST);
        if (relative.getType() == Material.CHEST && isLock && plugin.getLockRegistry().getLock(relative) == null) {
            event.setCancelled(true);
        }
    }
}
