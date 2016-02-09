/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.util.Log;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.config.datasource.DataSource;
import pluginbase.config.datasource.hocon.HoconDataSource;
import pluginbase.messages.messaging.SendablePluginBaseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LockRegistry {

    @NotNull
    private final LockAndKeyPlugin plugin;

    @NotNull
    private final DataSource locksDataSource;

    Map<LockLocation, Lock> lockedBlocks = new ConcurrentHashMap<>();
    private final ExecutorService dataWorker = Executors.newSingleThreadExecutor();

    private BukkitTask saveTask;

    public LockRegistry(@NotNull LockAndKeyPlugin plugin) throws IOException {
        this.plugin = plugin;
        File locksDataFolder = new File(plugin.getDataFolder(), "data");
        locksDataFolder.mkdirs();
        File locksDataFile = new File(locksDataFolder, "locks.dat");
        if (!locksDataFile.exists()) {
            locksDataFile.createNewFile();
        }
        locksDataSource = HoconDataSource.builder().setFile(locksDataFile).setCommentsEnabled(false).build();
    }

    LockAndKeyPlugin getPlugin() {
        return plugin;
    }

    public void loadLocks() {
        Future<Map<LockLocation, Lock>> lockLoader = dataWorker.submit(() -> {
            try {
                LockStore lockStore = locksDataSource.load(LockStore.class);
                if (lockStore == null) {
                    Log.info("Could not load lock data source... creating new data.");
                    lockStore = new LockStore();
                    locksDataSource.save(lockStore);
                }
                return lockStore.loadData(LockRegistry.this);
            } catch (SendablePluginBaseException e) {
                e.printStackTrace();
                Log.severe("No locks were loaded!");
                return new HashMap<>();
            }
        });
        try {
            lockedBlocks = lockLoader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        cleanupLocks();
        Log.info("Loaded " + lockedBlocks.size() + " locks");
    }

    private void cleanupLocks() {
        List<Lock> locksToRemove = new ArrayList<>(lockedBlocks.size());
        for (Lock lock : lockedBlocks.values()) {
            Block block = lock.getBlock();
            if (block != null) {
                if (LockMaterial.getByBlockMaterial(block.getType()) == null) {
                    locksToRemove.add(lock);
                }
            }
        }
        locksToRemove.stream().forEach(this::removeLock);
        if (!locksToRemove.isEmpty()) {
            Log.info("Removed " + locksToRemove.size() + " locks that aren't locks anymore!");
        }
    }

    public void startSaveTask(long saveTicks) {
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveLocks(false);
            }
        }.runTaskTimer(plugin, saveTicks, saveTicks);
    }

    public void stopSaveTask() {
        if (saveTask != null) {
            saveTask.cancel();
        }
    }

    public void saveLocks(boolean block) {
        Future lockSaver = dataWorker.submit(() -> {
            try {
                locksDataSource.save(new LockStore().saveData(lockedBlocks));
            } catch (SendablePluginBaseException e) {
                e.printStackTrace();
                Log.severe("Could not save lock data!");
            }
        });
        if (block) {
            try {
                lockSaver.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    public Lock createLock(@NotNull Block block, @NotNull Player owner, @Nullable String keyCode) {
        LockMaterial lockMaterial = LockMaterial.getByBlockMaterial(block.getType());
        if (lockMaterial == null) {
            throw new IllegalArgumentException("Invalid lock material: " + block.getType());
        }
        Lock lock = new Lock(this, block, owner, keyCode);
        lockedBlocks.put(lock.getLocation(), lock);
        if (lock.isDoor()) {
            Lock topLock = new Lock(this, block.getRelative(BlockFace.UP), owner, keyCode);
            lockedBlocks.put(topLock.getLocation(), topLock);
        }
        return lock;
    }

    public void removeLock(@NotNull Lock lock) {
        lockedBlocks.remove(lock.getLocation());
        if (lock.isDoor()) {
            lockedBlocks.remove(lock.getConnectedLocation());
        }
    }

    public boolean isLockedBlock(@NotNull Block block) {
        LockLocation location = new LockLocation(block);
        return lockedBlocks.containsKey(location);
    }

    @Nullable
    public Lock getLock(@NotNull Block block) {
        if (LockMaterial.getByBlockMaterial(block.getType()) == null) {
            return null;
        }
        LockLocation location = new LockLocation(block);
        return getLock(location);
    }

    @Nullable
    public Lock getLock(@NotNull LockLocation location) {
        return lockedBlocks.get(location);
    }
}
