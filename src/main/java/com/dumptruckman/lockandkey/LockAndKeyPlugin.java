/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey;

import com.dumptruckman.lockandkey.commands.GiveLockCommand;
import com.dumptruckman.lockandkey.listeners.DustListener;
import com.dumptruckman.lockandkey.listeners.RecipeListener;
import com.dumptruckman.lockandkey.locks.Lock;
import com.dumptruckman.lockandkey.locks.LockMaterial;
import com.dumptruckman.lockandkey.locks.Recipes;
import com.dumptruckman.lockandkey.util.Log;
import com.dumptruckman.lockandkey.commands.GiveDustCommand;
import com.dumptruckman.lockandkey.listeners.LockListener;
import com.dumptruckman.lockandkey.locks.LockRegistry;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pluginbase.bukkit.BukkitPluginAgent;
import pluginbase.plugin.PluginBase;
import pluginbase.plugin.Settings;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class LockAndKeyPlugin extends JavaPlugin {

    private static final String LOCKABLE_NODE_NAME = "lockable";
    private static final String DUST_NODE_NAME = "dust";
    private static final String KEY_NODE_NAME = "keye";
    private static final String KEY_CODE_KEY = "keycode";

    private final BukkitPluginAgent<LockAndKeyPlugin> pluginAgent = BukkitPluginAgent.getPluginAgent(LockAndKeyPlugin.class, this, "lak");

    private LockRegistry lockRegistry;
    private Recipes recipes = new Recipes(this);

    private Set<ItemStack> exampleLockItems;

    public LockAndKeyPlugin() {
        pluginAgent.setDefaultSettingsCallable(new Callable<Settings>() {
            @Override
            public Settings call() throws Exception {
                return new PluginConfig(getPluginBase());
            }
        });
        pluginAgent.setPermissionPrefix("lockandkey");
    }

    private PluginBase getPluginBase() {
        return pluginAgent.getPluginBase();
    }

    @Override
    public void onLoad() {
        pluginAgent.loadPluginBase();
        Log.init(getPluginBase().getLog());

        // Register commands
        pluginAgent.registerCommand(GiveLockCommand.class);
        pluginAgent.registerCommand(GiveDustCommand.class);
    }

    @Override
    public void onEnable() {
        // Check for Magic

        pluginAgent.enablePluginBase();

        exampleLockItems = new LinkedHashSet<>(LockMaterial.values().length);
        for (LockMaterial material : LockMaterial.values()) {
            exampleLockItems.add(createLockItem(material, 1));
        }
        int recipesLoaded = recipes.loadRecipes();
        Log.info("Loaded " + recipesLoaded + " recipes.");

        try {
            lockRegistry = new LockRegistry(this);
            lockRegistry.loadLocks();
            lockRegistry.startSaveTask(getSettings().getLocks().getSaveTicks());
        } catch (IOException e) {
            e.printStackTrace();
            Log.severe("Could not load locks!");
        }

        new LockListener(this);
        new RecipeListener(this);
        new DustListener(this);
    }

    @NotNull
    public PluginConfig getSettings() {
        return (PluginConfig) getPluginBase().getSettings();
    }

    @Override
    public void onDisable() {
        getLockRegistry().stopSaveTask();
        getLockRegistry().saveLocks(true);
        pluginAgent.disablePluginBase();
        Log.shutdown();
    }

    @Override
    public void reloadConfig() {
        getPluginBase().reloadConfig();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
        return pluginAgent.callCommand(sender, command, label, args);
    }

    public LockRegistry getLockRegistry() {
        return lockRegistry;
    }

    public Set<ItemStack> getExampleLockItems() {
        return exampleLockItems;
    }

    public ItemStack createLockItem(@NotNull LockMaterial material, int amount) {
        ItemStack item = new ItemStack(material.getItemMaterial(), amount);
        item = InventoryUtils.makeReal(item);

        CompatibilityUtils.setLore(item, getSettings().getLocks().getLockableLore());
        CompatibilityUtils.addGlow(item);
        CompatibilityUtils.setDisplayName(item, ChatColor.AQUA + "Lockable " + ChatColor.WHITE + material.getItemName());

        InventoryUtils.createNode(item, LOCKABLE_NODE_NAME);

        return item;
    }

    public boolean isLockItem(@NotNull ItemStack item) {
        return InventoryUtils.getNode(item, LOCKABLE_NODE_NAME) != null;
    }

    public ItemStack createSealingDust(int amount) {
        ItemStack item = new ItemStack(Material.REDSTONE, amount);
        item = InventoryUtils.makeReal(item);

        CompatibilityUtils.setLore(item, getSettings().getLocks().getDustLore());
        CompatibilityUtils.addGlow(item);
        CompatibilityUtils.setDisplayName(item, ChatColor.GOLD + getSettings().getLocks().getDustName());

        InventoryUtils.createNode(item, DUST_NODE_NAME);

        return item;
    }

    public ItemStack createDustBlock(int amount) {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK, amount);
        item = InventoryUtils.makeReal(item);

        CompatibilityUtils.setLore(item, getSettings().getLocks().getDustBlockLore());
        CompatibilityUtils.addGlow(item);
        CompatibilityUtils.setDisplayName(item, ChatColor.GOLD + getSettings().getLocks().getDustBlockName());

        InventoryUtils.createNode(item, DUST_NODE_NAME);

        return item;
    }

    public boolean isDustItem(@NotNull ItemStack item) {
        return InventoryUtils.getNode(item, DUST_NODE_NAME) != null;
    }

    public ItemStack createBlankKeyItem(int amount) {
        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK, amount);
        item = InventoryUtils.makeReal(item);

        CompatibilityUtils.setLore(item, getSettings().getLocks().getUncutKeyLore());
        CompatibilityUtils.addGlow(item);
        CompatibilityUtils.setDisplayName(item, ChatColor.GOLD + "Uncut Key");

        InventoryUtils.createNode(item, KEY_NODE_NAME);

        return item;
    }

    public boolean isKeyItem(@NotNull ItemStack item) {
        return InventoryUtils.getNode(item, KEY_NODE_NAME) != null;
    }

    public boolean isBlankKey(@NotNull ItemStack item) {
        String keyCode = getKeyCode(item);
        return keyCode == null || keyCode.isEmpty();
    }

    public String getKeyCode(@NotNull ItemStack item) {
        Object keyNode = InventoryUtils.getNode(item, KEY_NODE_NAME);
        return InventoryUtils.getMeta(keyNode, KEY_CODE_KEY);
    }

    public boolean isKeyCompatible(@NotNull ItemStack item, @NotNull Lock lock) {
        String keyCode = getKeyCode(item);
        return keyCode != null && lock.isCorrectKeyCode(keyCode);
    }
}
