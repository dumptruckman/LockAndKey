/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.commands;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.locks.LockMaterial;
import com.dumptruckman.lockandkey.util.Perms;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.command.Command;
import pluginbase.command.CommandContext;
import pluginbase.command.CommandException;
import pluginbase.command.CommandInfo;
import pluginbase.command.CommandProvider;
import pluginbase.messages.Message;
import pluginbase.minecraft.BasePlayer;
import pluginbase.permission.Perm;

import static com.dumptruckman.lockandkey.Messages.*;

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
            IN_GAME_ONLY.sendByChat(sender);
            return true;
        }

        String type = context.getString(0);
        LockMaterial material = null;
        try {
            material = LockMaterial.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            INVALID_LOCK_TYPE.sendByChat(sender, type);
            return true;
        }

        int amount = 1;
        if (context.argsLength() > 1) {
            try {
                amount = context.getInteger(1);
            } catch (NumberFormatException e) {
                INVALID_AMOUNT.sendByChat(sender, context.getString(1));
                return true;
            }
        }

        String name = context.getFlag('p', sender.getName());

        Player player = getPlugin().getServer().getPlayer(name);
        if (player == null) {
            INVALID_PLAYER.sendByChat(sender, name);
            return false;
        }

        ItemStack lockItem = getPlugin().createLockItem(material, amount);
        String itemName = lockItem.getItemMeta().getDisplayName();
        player.getInventory().addItem(lockItem);
        player.updateInventory();
        GAVE_ITEM.sendByChat(sender, name, amount, itemName);
        return true;
    }

    private void listLockTypes(@NotNull BasePlayer sender) {
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
        LOCK_TYPES_AVAILABLE.sendByChat(sender, allTypes.toString());
    }
}
