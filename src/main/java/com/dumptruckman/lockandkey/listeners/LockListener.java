/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.listeners;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.locks.Lock;
import com.dumptruckman.lockandkey.locks.LockRegistry;
import com.dumptruckman.lockandkey.util.ActionBarUtil;
import com.dumptruckman.lockandkey.util.ItemHelper;
import com.dumptruckman.lockandkey.util.Log;
import com.dumptruckman.lockandkey.util.Perms;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.*;
import org.jetbrains.annotations.NotNull;

public class LockListener implements Listener {

    @NotNull
    private final LockAndKeyPlugin plugin;

    public LockListener(@NotNull final LockAndKeyPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private LockRegistry getLockRegistry() {
        return plugin.getLockRegistry();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void placeLockedBlock(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!ItemHelper.isLockItem(item)) {
            return;
        }
        Lock lock = getLockRegistry().createLock(event.getBlock(), event.getPlayer(), ItemHelper.getKeyCode(item));
        event.getPlayer().sendMessage(ChatColor.AQUA + "You placed a Locked " + lock.getLockMaterial().getItemName() + "!");
    }

    @EventHandler(ignoreCancelled = false)
    public void useLockedBlock(PlayerInteractEvent event) {
        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) || event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Lock lock = getLockRegistry().getLock(block);
        if (lock == null) {
            return;
        }

        ItemStack itemInHand = player.getItemInHand();
        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && ItemHelper.isKeyItem(itemInHand)) {
            fitKey(player, lock, itemInHand);
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        if (event.getAction() != lock.getLockMaterial().getInteractAction()) {
            return;
        }

        if (Perms.BYPASS_LOCKS.hasPermission(player)) {
            return;
        }

        if (!plugin.getSettings().getLocks().isKeyRequiredForOwner() && lock.isOwner(player.getUniqueId())){
            return;
        }

        if (!ItemHelper.isKeyItem(itemInHand) || !lock.isKeyCompatible(itemInHand)) {
            OfflinePlayer lockOwner = lock.getOwner();
            if (lockOwner == null) {
                Log.info("Removing orphaned lock: " + lock);
                getLockRegistry().removeLock(lock);
                return;
            }
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            ActionBarUtil.sendActionBarMessage(player, ChatColor.RED + "You need a key to use that " + lock.getLockMaterial().getItemName());
            player.getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ZOMBIE_WOOD, .2F, 1F);
        }
    }

    private void fitKey(@NotNull Player player, @NotNull Lock lock, @NotNull ItemStack itemInHand) {
        if (!lock.isOwner(player.getUniqueId())) {
            ActionBarUtil.sendActionBarMessage(player, ChatColor.RED + "You must be the owner to configure the lock!");
            return;
        }
        if (ItemHelper.isBlankKeyItem(itemInHand)) {
            if (itemInHand.getAmount() > 1) {
                ActionBarUtil.sendActionBarMessage(player, ChatColor.RED + "You can only cut one key at a time!");
                return;
            }
            if (lock.hasKeyCode()) {
                lock.cutKey(itemInHand);
                ActionBarUtil.sendActionBarMessage(player, ChatColor.GREEN + "Key has been cut to existing lock!");
            } else {
                lock.setKeyCode(plugin.createRandomizedKeyCode());
                lock.cutKey(itemInHand);
                ActionBarUtil.sendActionBarMessage(player, ChatColor.GREEN + "Key has been cut to new lock!");
            }
        } else {
            if (lock.hasKeyCode()) {
                ActionBarUtil.sendActionBarMessage(player, ChatColor.RED + "This lock already uses a different key.");
            } else {
                lock.useNewKey(itemInHand);
                ActionBarUtil.sendActionBarMessage(player, ChatColor.GREEN + "New lock installed for this key!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void dropLockedBlock(BlockPhysicsEvent event) {
        Lock lock = getLockRegistry().getLock(event.getBlock());
        if (lock == null) {
            return;
        }
        BlockState state = event.getBlock().getState();
        if (state.getData() instanceof Attachable && event.getBlock().getState().getRawData() == 3) {
            // Water is breaking it
            dropItems(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void dropLockedBlock(BlockBreakEvent event) {
        if (dropItems(event.getBlock())) {
            return;
        }
        checkAttachedFaces(event);
    }

    private void checkAttachedFaces(BlockEvent event) {
        Block block = event.getBlock();
        Block relativeBlock = block.getRelative(BlockFace.UP);
        MaterialData check = relativeBlock.getState().getData();
        if (check instanceof Door
                || check instanceof Attachable
                || check instanceof PressurePlate
                || check.getItemType() == Material.GOLD_PLATE
                || check.getItemType() == Material.IRON_PLATE) {
            dropItems(relativeBlock);
            return;
        }
        checkAttachable(event, BlockFace.DOWN);
        checkAttachable(event, BlockFace.NORTH);
        checkAttachable(event, BlockFace.SOUTH);
        checkAttachable(event, BlockFace.EAST);
        checkAttachable(event, BlockFace.WEST);
    }

    private void checkAttachable(BlockEvent event, BlockFace face) {
        Block relativeBlock = event.getBlock().getRelative(face);
        MaterialData check = relativeBlock.getState().getData();
        if (check instanceof Attachable) {
            dropItems(relativeBlock);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void dropLockedBlock(BlockExplodeEvent event) {
        if (dropItems(event.getBlock())) {
            return;
        }
        checkAttachedFaces(event);
    }

    private boolean dropItems(Block block) {
        Lock lock = getLockRegistry().getLock(block);
        if (lock == null) {
            return false;
        }

        getLockRegistry().removeLock(lock);

        MaterialData data = block.getState().getData();
        if (data instanceof Door) {
            Door door = (Door) data;
            if (door.isTopHalf()) {
                block.getRelative(BlockFace.DOWN).setType(Material.AIR, false);
            } else {
                block.getRelative(BlockFace.UP).setType(Material.AIR, false);
            }
        }
        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), lock.createLockItem(1));
        return true;
    }
}