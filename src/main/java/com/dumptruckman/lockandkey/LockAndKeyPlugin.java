/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey;

import com.dumptruckman.lockandkey.PluginSettings.LockSettings;
import com.dumptruckman.lockandkey.PluginSettings.LockSettings.Descriptions;
import com.dumptruckman.lockandkey.commands.GiveKeyCommand;
import com.dumptruckman.lockandkey.commands.GiveLockCommand;
import com.dumptruckman.lockandkey.listeners.AntiPlaceListener;
import com.dumptruckman.lockandkey.listeners.RecipeListener;
import com.dumptruckman.lockandkey.locks.LockMaterial;
import com.dumptruckman.lockandkey.locks.Recipes;
import com.dumptruckman.lockandkey.util.ItemHelper;
import com.dumptruckman.lockandkey.util.Log;
import com.dumptruckman.lockandkey.commands.GiveDustCommand;
import com.dumptruckman.lockandkey.listeners.LockListener;
import com.dumptruckman.lockandkey.locks.LockRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pluginbase.bukkit.BukkitPluginAgent;
import pluginbase.plugin.PluginBase;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class LockAndKeyPlugin extends JavaPlugin {

    private final BukkitPluginAgent<LockAndKeyPlugin> pluginAgent = BukkitPluginAgent.getPluginAgent(LockAndKeyPlugin.class, this, "lak");

    private final Random random = new Random(System.currentTimeMillis());

    private LockRegistry lockRegistry;
    private Recipes recipes = new Recipes(this);

    private Set<ItemStack> exampleLockItems;

    public LockAndKeyPlugin() {
        pluginAgent.setDefaultSettingsCallable(() -> new PluginSettings(getPluginBase()));
        pluginAgent.setPermissionPrefix("lockandkey");
    }

    PluginBase getPluginBase() {
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
        Messages.setPlugin(this);

        try {
            lockRegistry = new LockRegistry(this);
            lockRegistry.loadLocks();
            lockRegistry.startSaveTask(getSettings().getLocks().getSaveTicks());
        } catch (IOException e) {
            e.printStackTrace();
            Log.severe("Could not load locks!");
        }

        if (!getServer().getPluginManager().isPluginEnabled("Magic")
                && !getServer().getPluginManager().isPluginEnabled("MagicLib")) {
            Log.severe("Magic or MagicLib must be installed before using LockAndKey!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        exampleLockItems = new LinkedHashSet<>(LockMaterial.values().length);
        for (LockMaterial material : LockMaterial.values()) {
            exampleLockItems.add(createLockItem(material, 1));
        }
        int recipesLoaded = recipes.loadRecipes();
        Log.info("Loaded " + recipesLoaded + " recipes.");

        new LockListener(this);
        new RecipeListener(this);
        new AntiPlaceListener(this);
    }

    @NotNull
    public PluginSettings getSettings() {
        return (PluginSettings) getPluginBase().getSettings();
    }

    public LockSettings getLockSettings() {
        return getSettings().getLocks();
    }

    public Descriptions getItemDescriptions() {
        return getLockSettings().getDescriptions();
    }

    @Override
    public void onDisable() {
        getLockRegistry().stopSaveTask();
        getLockRegistry().saveLocks(true);
        pluginAgent.disablePluginBase();
        Messages.setPlugin(null);
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
        return ItemHelper.builder(material.getItemMaterial(), amount)
                .setName(ChatColor.AQUA + "Locked " + ChatColor.WHITE + material.getItemName())
                .setLore(getItemDescriptions().getLockLore())
                .addGlow()
                .createLockData()
                .buildItem();
    }

    public ItemStack createSealingDust(int amount) {
        return ItemHelper.builder(Material.REDSTONE, amount)
                .setName(ChatColor.GOLD + getSettings().getLocks().getDustName())
                .setLore(getItemDescriptions().getDustLore())
                .addGlow()
                .makeUnplaceable()
                .createDustData()
                .buildItem();
    }

    public ItemStack createDustBlock(int amount) {
        return ItemHelper.builder(Material.REDSTONE_BLOCK, amount)
                .setName(ChatColor.GOLD + getSettings().getLocks().getDustBlockName())
                .setLore(getItemDescriptions().getDustBlockLore())
                .addGlow()
                .makeUnplaceable()
                .createDustData()
                .buildItem();
    }

    public ItemStack createBlankKeyItem(int amount) {
        return ItemHelper.builder(Material.TRIPWIRE_HOOK, amount)
                .setName(ChatColor.GOLD + "Uncut Key")
                .setLore(getItemDescriptions().getUncutKeyLore())
                .addGlow()
                .makeUnplaceable()
                .createKeyData()
                .buildItem();
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
}
