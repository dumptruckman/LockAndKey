/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.commands;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
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
        primaryAlias = "givedust",
        aliases = {"givedust"},
        desc = "Gives sealing magic dust to the player",
        usage = "[amount]",
        flags = "bp:",
        min = 0,
        max = 1
)
public class GiveDustCommand extends Command<LockAndKeyPlugin> {

    public GiveDustCommand(@NotNull CommandProvider<LockAndKeyPlugin> commandProvider) {
        super(commandProvider);
    }

    @Nullable
    @Override
    public Perm getPerm() {
        return Perms.CMD_GIVEDUST;
    }

    @Nullable
    @Override
    public Message getHelp() {
        return null;
    }

    @Override
    public boolean runCommand(@NotNull BasePlayer sender, @NotNull CommandContext context) throws CommandException {
        if (!sender.isPlayer() && !context.hasFlag('p')) {
            IN_GAME_ONLY.sendByChat(sender);
            return true;
        }

        int amount = 1;
        if (context.argsLength() > 0) {
            try {
                amount = context.getInteger(0);
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

        ItemStack item;
        if (context.hasFlag('b')) {
            item = getPlugin().createDustBlock(amount);
        } else {
            item = getPlugin().createSealingDust(amount);
        }

        player.getInventory().addItem(item);
        player.updateInventory();
        GAVE_ITEM.sendByChat(sender, name, amount, item.getItemMeta().getDisplayName());
        return true;
    }
}
