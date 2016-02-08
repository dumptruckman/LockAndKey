/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey;

import org.jetbrains.annotations.NotNull;
import pluginbase.config.annotation.Comment;
import pluginbase.config.annotation.NoTypeKey;
import pluginbase.plugin.PluginBase;
import pluginbase.plugin.Settings;

@NoTypeKey
public final class LockAndKeyConfig extends Settings {

    private Locks locks = new Locks();

    public LockAndKeyConfig(@NotNull PluginBase plugin) {
        super(plugin);
    }

    private LockAndKeyConfig() {
    }

    public Locks getLocks() {
        return locks;
    }

    @NoTypeKey
    public final static class Locks {

        @Comment({"How often to save lock data (in ticks).", "1200 ticks is approximately 1 minute"})
        private long saveTicks = 1200L;
        @Comment({"Whether the other must have a key to open their own locks", "False means that can lock/unlock with a key."})
        private boolean ownerRequiresKey = false;

        public long getSaveTicks() {
            return saveTicks;
        }

        public boolean isOwnerRequiresKey() {
            return ownerRequiresKey;
        }
    }
}
