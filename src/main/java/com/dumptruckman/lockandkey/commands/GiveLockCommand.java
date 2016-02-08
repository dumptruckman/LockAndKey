/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.commands;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.locks.Lock;
import com.dumptruckman.lockandkey.locks.LockMaterial;
import com.dumptruckman.lockandkey.util.Perms;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.command.*;
import pluginbase.messages.Message;
import pluginbase.minecraft.BasePlayer;
import pluginbase.permission.Perm;

@CommandInfo(
        primaryAlias = "givelock",
        aliases = {"givelock"},
        desc = "Gives a locked block to the player",
        usage = "[<type> [amount]]",
        flags = "p:",
        min = 0,
        max = 2
)
public class GiveLockCommand extends Command<LockAndKeyPlugin> {

    public GiveLockCommand(@NotNull CommandProvider<LockAndKeyPlugin> commandProvider) {
        super(commandProvider);
    }

    @Nullable
    @Override
    public Perm getPerm() {
        return Perms.CMD_GIVELOCK;
    }

    @Nullable
    @Override
    public Message getHelp() {
        return null;
    }

    @Override
    public boolean runCommand(@NotNull BasePlayer sender, @NotNull CommandContext context) throws CommandException {
        if (context.argsLength() == 0) {
            listLockTypes(sender);
            return true;
        }

        if (!sender.isPlayer() && !context.hasFlag('p')) {
            sender.sendMessage("Must specify a player to use from console.");
            return true;
        }

        String type = context.getString(0);
        LockMaterial material = null;
        try {
            material = LockMaterial.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("'" + type + "' is not a valid Lock type");
            return true;
        }

        int amount = 1;
        if (context.argsLength() > 1) {
            try {
                amount = context.getInteger(1);
            } catch (NumberFormatException e) {
                sender.sendMessage("'" + context.getString(1) + "' is not a valid amount!");
                return true;
            }
        }

        String name = context.getFlag('p', sender.getName());

        Player player = getPlugin().getServer().getPlayer(name);
        if (player == null) {
            sender.sendMessage("'" + name + "' is not a valid player!");
            return false;
        }

        ItemStack lockItem = Lock.createLockedItem(material, amount);
        player.getInventory().addItem(lockItem);
        player.updateInventory();
        sender.sendMessage("Gave " + ChatColor.GOLD + name + " " + ChatColor.ITALIC + ChatColor.GREEN + amount + ChatColor.AQUA + " Locked " + material.getItemName());
        return true;
    }

    private void listLockTypes(@NotNull BasePlayer sender) {
        sender.sendMessage("Lock types available: ");
        StringBuilder allTypes = new StringBuilder();
        ChatColor color = ChatColor.WHITE;
        for (LockMaterial material : LockMaterial.values()) {
            if (allTypes.length() != 0) {
                allTypes.append(", ");
            }
            allTypes.append(color);
            allTypes.append(material.name());
            if (color == ChatColor.WHITE) {
                color = ChatColor.YELLOW;
            } else {
                color = ChatColor.WHITE;
            }
        }
    }
}
