/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey;

import com.dumptruckman.lockandkey.commands.GiveKeyCommand;
import com.dumptruckman.lockandkey.commands.GiveLockCommand;
import com.dumptruckman.lockandkey.listeners.AntiPlaceListener;
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
import org.jetbrains.annotations.Nullable;
import pluginbase.bukkit.BukkitPluginAgent;
import pluginbase.plugin.PluginBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LockAndKeyPlugin extends JavaPlugin {

    private static final String LOCK_NODE_NAME = "lockNode";
    private static final String DUST_NODE_NAME = "dustNode";
    private static final String KEY_NODE_NAME = "keyNode";
    private static final String KEY_CODE_KEY = "keyCode";

    private final BukkitPluginAgent<LockAndKeyPlugin> pluginAgent = BukkitPluginAgent.getPluginAgent(LockAndKeyPlugin.class, this, "lak");

    private final Random random = new Random(System.currentTimeMillis());

    private LockRegistry lockRegistry;
    private Recipes recipes = new Recipes(this);

    private Set<ItemStack> exampleLockItems;

    public LockAndKeyPlugin() {
        pluginAgent.setDefaultSettingsCallable(() -> new PluginSettings(getPluginBase()));
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
        pluginAgent.registerCommand(GiveKeyCommand.class);
    }

    @Override
    public void onEnable() {
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
        new AntiPlaceListener(this);
    }

    @NotNull
    public PluginSettings getSettings() {
        return (PluginSettings) getPluginBase().getSettings();
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

        CompatibilityUtils.setLore(item, getSettings().getLocks().getLockLore());
        CompatibilityUtils.addGlow(item);
        CompatibilityUtils.setDisplayName(item, ChatColor.AQUA + "Locked " + ChatColor.WHITE + material.getItemName());

        InventoryUtils.createNode(item, LOCK_NODE_NAME);

        return item;
    }

    public ItemStack createLockItem(@NotNull Lock lock, int amount) {
        ItemStack item = createLockItem(lock.getLockMaterial(), amount);
        Object lockNode = InventoryUtils.createNode(item, LOCK_NODE_NAME);
        InventoryUtils.setMeta(lockNode, KEY_CODE_KEY, lock.getKeyCode());
        return item;
    }

    public boolean isLockItem(@NotNull ItemStack item) {
        return InventoryUtils.getNode(item, LOCK_NODE_NAME) != null;
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

    @Nullable
    public String getKeyCode(@NotNull ItemStack item) {
        Object keyNode = InventoryUtils.getNode(item, KEY_NODE_NAME);
        return InventoryUtils.getMeta(keyNode, KEY_CODE_KEY);
    }

    public boolean isKeyCompatible(@NotNull ItemStack item, @NotNull Lock lock) {
        String keyCode = getKeyCode(item);
        return keyCode != null && lock.isCorrectKeyCode(keyCode);
    }

    public String createRandomizedKeyCode() {
        String keyCharacters = getSettings().getLocks().getLockCodeCharacters();
        int lockCodeLength = getSettings().getLocks().getLockCodeLength();
        StringBuilder code = new StringBuilder(lockCodeLength);
        for (int i = 0; i < lockCodeLength; i++) {
            code.append(keyCharacters.charAt(random.nextInt(keyCharacters.length())));
        }
        return code.toString();
    }

    public void configureKeyToLock(@NotNull ItemStack key, @NotNull Lock lock) {
        String keyCode = lock.getKeyCode();
        if (keyCode == null) {
            throw new IllegalArgumentException("The lock must have a key code first.");
        }
        Object keyNode = InventoryUtils.getNode(key, KEY_NODE_NAME);
        if (keyNode == null) {
            throw new IllegalArgumentException("Item must represent a key.");
        }
        InventoryUtils.setMeta(keyNode, KEY_CODE_KEY, keyCode);
        List<String> lore = new ArrayList<>(getSettings().getLocks().getKeyLore());
        if (getSettings().getLocks().isLockCodeVisible()) {
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + keyCode);
        }
        CompatibilityUtils.setLore(key, lore);
        CompatibilityUtils.addGlow(key);
        CompatibilityUtils.setDisplayName(key, ChatColor.GOLD + "Key");
    }

    public void configureLockToKey(@NotNull Lock lock, @NotNull ItemStack key) {
        Object keyNode = InventoryUtils.getNode(key, KEY_NODE_NAME);
        if (keyNode == null) {
            throw new IllegalArgumentException("Item must represent a key.");
        }
        String keyCode = InventoryUtils.getMeta(keyNode, KEY_CODE_KEY);
        if (keyCode == null || keyCode.isEmpty()) {
            throw new IllegalArgumentException("Item must represent a cut key.");
        }
        lock.setKeyCode(keyCode);
    }
}
