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

import java.util.ArrayList;
import java.util.List;

@NoTypeKey
public final class PluginSettings extends Settings {

    private LockSettings locks = new LockSettings();

    public PluginSettings(@NotNull PluginBase plugin) {
        super(plugin);
    }

    private PluginSettings() {
    }

    public LockSettings getLocks() {
        return locks;
    }

    @NoTypeKey
    public final static class LockSettings {

        @Comment({"How often to save lock data (in ticks).", "1200 ticks is approximately 1 minute"})
        private long saveTicks = 1200L;
        @Comment({"Whether the other must have a key to open their own locks", "False means that the owner can lock/unlock without a key."})
        private boolean keyRequiredForOwner = false;
        @Comment({"The name of the lock creation ingredient (Redstone)"})
        private String dustName = "Sealing Dust";
        @Comment({"The name of the lock creation ingredient block (Redstone Block)"})
        private String dustBlockName = "Concentrated Sealing Dust";
        @Comment({"This section is where you can change the description on the different items from this plugin."})
        private Descriptions descriptions = new Descriptions();
        @Comment({"Whether or not to show the lock code as the last line of the item lore of items"})
        private boolean lockCodeVisible = true;
        @Comment({"This is a string of all the valid characters than can be used for a lock code.",
                 "A random selection of these will be created for every new key-lock combination."})
        private String lockCodeCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        @Comment({"This is the number of characters to use in a lock code.",
                "Having a small length and few characters raises the chance to produce keys that work on locks they were not created for."})
        private int lockCodeLength = 5;

        public long getSaveTicks() {
            return saveTicks;
        }

        public boolean isKeyRequiredForOwner() {
            return keyRequiredForOwner;
        }

        public String getDustName() {
            return dustName;
        }

        public String getDustBlockName() {
            return dustBlockName;
        }

        public String getLockCodeCharacters() {
            return lockCodeCharacters;
        }

        public int getLockCodeLength() {
            return lockCodeLength;
        }

        public boolean isLockCodeVisible() {
            return lockCodeVisible;
        }

        public Descriptions getDescriptions() {
            return descriptions;
        }

        @NoTypeKey
        public final static class Descriptions {

            @Comment({"This is the item description of locked items (doors, buttons, etc)"})
            private List<String> lockLore;
            {
                lockLore = new ArrayList<>();
                lockLore.add("This item is locked when placed.");
                lockLore.add("It can only be opened by the owner");
                lockLore.add("or anyone with a key.");
            }

            @Comment({"This is the item description of the Sealing Dust"})
            private List<String> dustLore;
            {
                dustLore = new ArrayList<>();
                dustLore.add("This dust contains the");
                dustLore.add("magic required to lock");
                dustLore.add("objects so that only");
                dustLore.add("their owner may use them.");
            }

            @Comment({"This is the item description of the Concentrated Sealing Dust"})
            private List<String> dustBlockLore;
            {
                dustBlockLore = new ArrayList<>();
                dustBlockLore.add("A concentrated form of");
                dustBlockLore.add("Sealing Dust");
            }

            @Comment({"This is the item description of an Uncut Key"})
            private List<String> uncutKeyLore;
            {
                uncutKeyLore = new ArrayList<>();
                uncutKeyLore.add("This key can be cut to fit any lock.");
                uncutKeyLore.add("Sneak right click the locked block");
                uncutKeyLore.add("to configure this key for that block.");
            }

            @Comment({"This is the item description of an (cut) Key"})
            private List<String> keyLore;
            {
                keyLore = new ArrayList<>();
                keyLore.add("This key unlocks something somewhere...");
            }

            public List<String> getLockLore() {
                return lockLore;
            }

            public List<String> getDustLore() {
                return dustLore;
            }

            public List<String> getDustBlockLore() {
                return dustBlockLore;
            }

            public List<String> getUncutKeyLore() {
                return uncutKeyLore;
            }

            public List<String> getKeyLore() {
                return keyLore;
            }
        }
    }
}
