/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.PluginSettings.Locks;
import com.dumptruckman.lockandkey.util.ItemHelper;
import com.dumptruckman.lockandkey.util.Log;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.config.annotation.NoTypeKey;

import java.util.*;

@NoTypeKey
public final class Lock {

    private transient LockRegistry registry;

    private LockLocation location;
    @Nullable
    LockLocation connectedLocation = null;
    private LockMaterial lockMaterial;
    private UUID ownerId;
    @Nullable
    String keyCode = null;

    private Lock() { }

    Lock(@NotNull LockRegistry lockRegistry, @NotNull Block block, @NotNull Player owner, @Nullable String keyCode) {
        this.registry = lockRegistry;
        this.location = new LockLocation(block);
        this.lockMaterial = LockMaterial.getByBlockMaterial(block.getType());
        if (lockMaterial == null) {
            throw new IllegalArgumentException("Invalid lock material: " + block.getType());
        }
        MaterialData blockData = block.getState().getData();
        if (lockMaterial.isDoor() && blockData instanceof Door) {
            Door door = (Door) blockData;
            if (door.isTopHalf()) {
                connectedLocation = new LockLocation(block.getRelative(BlockFace.DOWN));
            } else {
                connectedLocation = new LockLocation(block.getRelative(BlockFace.UP));
            }
        }
        this.ownerId = owner.getUniqueId();
    }

    void setRegistry(@NotNull LockRegistry registry) {
        this.registry = registry;
    }

    private LockRegistry getRegistry() {
        return registry;
    }

    private LockAndKeyPlugin getPlugin() {
        if (registry == null) {
            throw new IllegalStateException("Somehow this Lock was not set up correctly!");
        }
        return registry.getPlugin();
    }

    private Locks getLockSettings() {
        return getPlugin().getSettings().getLocks();
    }

    public LockLocation getLocation() {
        return location;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public boolean isDoor() {
        return lockMaterial.isDoor();
    }

    public boolean isChest() {
        return lockMaterial.isChest();
    }

    public boolean isOwner(@NotNull UUID playerId) {
        return playerId.equals(ownerId);
    }

    @Nullable
    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(ownerId);
    }

    @Nullable
    public LockLocation getConnectedLocation() {
        return connectedLocation;
    }

    @Nullable
    public Lock getConnectedLock() {
        return connectedLocation != null ? registry.getLock(connectedLocation) : null;
    }

    public LockMaterial getLockMaterial() {
        return lockMaterial;
    }

    public boolean hasKeyCode() {
        return keyCode != null && !keyCode.isEmpty();
    }

    public boolean isCorrectKeyCode(@Nullable String keyCode) {
        return this.keyCode != null && this.keyCode.equals(keyCode);
    }

    @Nullable
    public String getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(@Nullable String keyCode) {
        this.keyCode = keyCode;
        Lock connectedLock = getConnectedLock();
        if (connectedLock != null) {
            getConnectedLock().keyCode = keyCode;
        }
    }

    public ItemStack createLockItem(int amount) {
        ItemStack item = getPlugin().createLockItem(getLockMaterial(), amount);
        return ItemHelper.setKeyCode(item, getKeyCode());
    }

    public boolean isKeyCompatible(@NotNull ItemStack item) {
        String keyCode = ItemHelper.getKeyCode(item);
        return isCorrectKeyCode(keyCode);
    }

    /**
     * Turns the key into a key for this lock.
     *
     * @param key the item that represents the key.
     */
    public void cutKey(@NotNull ItemStack key) {
        if (!ItemHelper.isKeyItem(key)) {
            throw new IllegalArgumentException("Item must represent a key.");
        }
        String keyCode = getKeyCode();
        if (keyCode == null) {
            throw new IllegalArgumentException("The lock must have a key code first.");
        }

        List<String> lore = new ArrayList<>(getLockSettings().getKeyLore());
        if (getLockSettings().isLockCodeVisible()) {
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + keyCode);
        }

        ItemHelper.builder(key)
                .setName(ChatColor.GOLD + "Key")
                .setLore(lore)
                .addGlow()
                .setKeyCode(keyCode).buildItem();
    }

    /**
     * Changes this lock to use the given key.
     *
     * @param key the key this lock will be set to work with.
     */
    public void useNewKey(@NotNull ItemStack key) {
        if (!ItemHelper.isKeyItem(key)) {
            throw new IllegalArgumentException("Item must represent a key.");
        }
        String keyCode = ItemHelper.getKeyCode(key);
        if (keyCode == null || keyCode.isEmpty()) {
            throw new IllegalArgumentException("Item must represent a cut key.");
        }
        setKeyCode(keyCode);
    }

    @Override
    public String toString() {
        return "Lock{" +
                "location=" + location +
                ", connectedLocation=" + connectedLocation +
                ", lockMaterial=" + lockMaterial +
                ", ownerId=" + ownerId +
                '}';
    }
}
