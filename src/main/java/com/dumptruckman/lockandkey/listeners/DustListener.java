/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.listeners;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DustListener implements Listener {

    @NotNull
    private final LockAndKeyPlugin plugin;

    public DustListener(@NotNull final LockAndKeyPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void placeDust(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null) {
            return;
        }
        if (item.getType() != Material.REDSTONE && item.getType() != Material.REDSTONE_BLOCK) {
            return;
        }
        if (plugin.isDustItem(item)) {
            event.setCancelled(true);
        }
    }
}
