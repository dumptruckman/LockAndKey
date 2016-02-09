/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import org.jetbrains.annotations.NotNull;
import pluginbase.config.annotation.NoTypeKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoTypeKey
final class LockStore {

    List<Lock> lockData = new ArrayList<>();

    @NotNull
    Map<LockLocation, Lock> loadData(@NotNull LockRegistry registry) {
        Map<LockLocation, Lock> map = new HashMap<>(lockData.size());
        for (Lock lock : lockData) {
            lock.setRegistry(registry);
            map.put(lock.getLocation(), lock);
        }
        return map;
    }

    LockStore saveData(@NotNull Map<LockLocation, Lock> data) {
        lockData = new ArrayList<>(data.values());
        return this;
    }
}
