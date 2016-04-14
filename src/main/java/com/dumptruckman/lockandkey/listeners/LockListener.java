/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.listeners;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.locks.Lock;
import com.dumptruckman.lockandkey.locks.LockRegistry;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Attachable;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PressurePlate;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dumptruckman.lockandkey.Messages.*;
import static com.dumptruckman.lockandkey.util.ItemHelper.getKeyUsesRemaining;

public class LockListener implements Listener {

    @NotNull
    private final LockAndKeyPlugin plugin;
    private final Set<Player> recentInteract = new HashSet<>();

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
        PLACED_LOCK.sendByChat(event.getPlayer(), lock.getLockMaterial().getItemName());
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

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
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

        if (Perms.NO_OWNER_KEY_NEEDED.hasPermission(player) && lock.isOwner(player.getUniqueId())){
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
            if (!recentInteract.contains(player)) {
                NEED_KEY.sendByActionBar(player, lock.getLockMaterial().getItemName());
                player.getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, .2F, 1F);
                if (event.getAction() == Action.PHYSICAL) {
                    recentInteract.add(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            recentInteract.remove(player);
                        }
                    }.runTaskLater(plugin, 60L);
                }
            }
        } else if (ItemHelper.isKeyItem(itemInHand) && lock.isKeyCompatible(itemInHand)) {
            int uses = ItemHelper.getKeyUsesRemaining(itemInHand);
            if (uses > 0) {
                uses--;
                ItemHelper.setKeyUsesRemaining(itemInHand, uses);
                updateKeyLore(itemInHand);
                if (uses == 0) {
                    player.getInventory().setItemInMainHand(null);
                }
                player.updateInventory();
            }
        }
    }

    private void updateKeyLore(@NotNull ItemStack itemStack) {
        if (!ItemHelper.isKeyItem(itemStack)) {
            throw new IllegalArgumentException("Item must represent a key.");
        }
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(plugin.getLockSettings().getDescriptions().getKeyLore());
        int uses = ItemHelper.getKeyUsesRemaining(itemStack);
        if (uses > 0) {
            lore.add(plugin.getLockSettings().getDescriptions().getKeyUsesLore(uses));
        }
        String keyCode = ItemHelper.getKeyCode(itemStack);
        if (plugin.getLockSettings().isLockCodeVisible()) {
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + keyCode);
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private void fitKey(@NotNull Player player, @NotNull Lock lock, @NotNull ItemStack itemInHand) {
        if (!lock.isOwner(player.getUniqueId())) {
            MUST_BE_OWNER.sendByActionBar(player);
            return;
        }
        if (ItemHelper.isBlankKeyItem(itemInHand)) {
            if (itemInHand.getAmount() > 1) {
                ONE_KEY_AT_A_TIME.sendByActionBar(player);
                return;
            }
            if (lock.hasKeyCode()) {
                lock.cutKey(itemInHand);
                KEY_CUT_FOR_EXISTING_LOCK.sendByActionBar(player);
            } else {
                lock.setKeyCode(plugin.createRandomizedKeyCode());
                lock.cutKey(itemInHand);
                KEY_CUT_FOR_NEW_LOCK.sendByActionBar(player);
            }
        } else {
            if (lock.hasKeyCode()) {
                LOCK_ALREADY_HAS_KEY.sendByActionBar(player);
            } else {
                lock.useNewKey(itemInHand);
                NEW_LOCK_INSTALLED_FOR_KEY.sendByActionBar(player);
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
