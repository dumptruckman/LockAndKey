/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.config.annotation.NoTypeKey;

import java.util.Objects;
import java.util.UUID;

@NoTypeKey
public final class LockLocation {

    private UUID worldId;
    private int x, y, z;

    private LockLocation() {
        this(null, 0, 0, 0);
    }

    public LockLocation(Block block) {
        this(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    public LockLocation(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LockLocation)) return false;
        final LockLocation that = (LockLocation) o;
        return x == that.x &&
                y == that.y &&
                z == that.z &&
                Objects.equals(worldId, that.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, y, z);
    }

    @NotNull
    public UUID getWorldId() {
        return worldId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @NotNull
    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(worldId), x, y, z);
    }

    @Nullable
    public Block getBlock() {
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            return null;
        }
        return world.getBlockAt(x, y, z);
    }

    @Override
    public String toString() {
        return "LockLocation{" +
                "worldId=" + worldId +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
