/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.commands;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.util.Perms;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.command.*;
import pluginbase.messages.Message;
import pluginbase.minecraft.BasePlayer;
import pluginbase.permission.Perm;

@CommandInfo(
        primaryAlias = "lock",
        aliases = {"lock"},
        desc = "Used to manage your locks.",
        usage = "[[<add|remove> <player>]|lock]",
        flags = "A",
        min = 0,
        max = 2
)
public class LockCommand extends Command<LockAndKeyPlugin> {

    public LockCommand(@NotNull CommandProvider<LockAndKeyPlugin> commandProvider) {
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
        return true;
    }

}
