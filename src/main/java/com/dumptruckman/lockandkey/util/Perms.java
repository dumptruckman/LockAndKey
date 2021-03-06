/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.util;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import pluginbase.bukkit.permission.BukkitPerm;
import pluginbase.bukkit.permission.BukkitPermFactory;
import pluginbase.permission.PermDefault;

public interface Perms {

    BukkitPerm CMD_GIVELOCK = BukkitPermFactory.newBukkitPerm(LockAndKeyPlugin.class, "cmd.givelock")
            .commandPermission().usePluginName().build();
    BukkitPerm CMD_GIVEDUST = BukkitPermFactory.newBukkitPerm(LockAndKeyPlugin.class, "cmd.givedust")
            .commandPermission().usePluginName().build();
    BukkitPerm CMD_GIVEKEY = BukkitPermFactory.newBukkitPerm(LockAndKeyPlugin.class, "cmd.givekey")
            .commandPermission().usePluginName().build();

    BukkitPerm BYPASS_LOCKS = BukkitPermFactory.newBukkitPerm(LockAndKeyPlugin.class, "bypass.locks")
            .usePluginName().def(PermDefault.FALSE).build();

    BukkitPerm NO_OWNER_KEY_NEEDED = BukkitPermFactory.newBukkitPerm(LockAndKeyPlugin.class, "openownlocks")
            .usePluginName().def(PermDefault.TRUE).build();
}
