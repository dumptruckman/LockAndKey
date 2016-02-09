/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.listeners;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.util.ItemHelper;
import org.bukkit.Material;
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
        if (ItemHelper.isDustItem(item)
                || ItemHelper.isDustBlockItem(item)
                || ItemHelper.isKeyItem(item)) {
            event.setCancelled(true);
        }
    }
}
