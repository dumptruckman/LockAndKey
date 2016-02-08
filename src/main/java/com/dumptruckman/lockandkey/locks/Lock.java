/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import com.dumptruckman.lockandkey.util.Perms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.config.annotation.NoTypeKey;

import java.util.*;

@NoTypeKey
public final class Lock {

    private static final String LOCK_CODE = ChatColor.COLOR_CHAR + "*";
    private static final List<String> LOCK_LORE;
    static {
        LOCK_LORE = new ArrayList<>(2);
        LOCK_LORE.add(ChatColor.WHITE + "Only usable by owner");
        LOCK_LORE.add(ChatColor.WHITE + "or designated friends");
    }

    private LockLocation location;
    @Nullable
    private LockLocation connectedLocation = null;
    private LockMaterial lockMaterial;
    private UUID ownerId;
    private boolean locked = false;
    private Set<UUID> whiteListedPlayers = new HashSet<>();

    private Lock() { }

    Lock(@NotNull Block block, @NotNull Player owner) {
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

    public LockLocation getLocation() {
        return location;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public boolean isDoor() {
        return lockMaterial.isDoor();
    }

    public boolean isOwner(@NotNull UUID playerId) {
        return playerId.equals(ownerId);
    }

    @Nullable
    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(ownerId);
    }

    public boolean isOnWhiteList(@NotNull UUID playerId) {
        return whiteListedPlayers.contains(playerId);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isUsableBy(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        return isOwner(playerId) || isOnWhiteList(playerId) || Perms.BYPASS_LOCKS.hasPermission(player);
    }

    @Nullable
    public LockLocation getConnectedLocation() {
        return connectedLocation;
    }

    public LockMaterial getLockMaterial() {
        return lockMaterial;
    }

    public static boolean isItemLockable(@NotNull ItemStack item) {
        return LockMaterial.getByItemMaterial(item.getType()) != null;
    }

    public static boolean isLockedItem(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        String displayName = meta.getDisplayName();
        return displayName.length() > 3 && displayName.endsWith(LOCK_CODE);
    }

    public static ItemStack createLockedItem(@NotNull LockMaterial material, int amount) {
        ItemStack item = new ItemStack(material.getItemMaterial(), amount);
        setEncodedLockName(item);
        return item;
    }

    private static ItemStack setEncodedLockName(@NotNull ItemStack itemToName) {
        LockMaterial lockMaterial = LockMaterial.getByItemMaterial(itemToName.getType());
        if (lockMaterial == null) {
            throw new IllegalArgumentException("Item type '" + itemToName.getType() + "' is not lockable");
        }

        String itemName;
        ItemMeta meta = itemToName.getItemMeta();

        if (meta.hasDisplayName()) {
            itemName = meta.getDisplayName();
        } else {
            itemName = ChatColor.WHITE + lockMaterial.getItemName();
        }
        itemName = ChatColor.AQUA + "Locked " + itemName + LOCK_CODE;
        meta.setDisplayName(itemName);
        meta.setLore(LOCK_LORE);
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemToName.setItemMeta(meta);
        return itemToName;
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
