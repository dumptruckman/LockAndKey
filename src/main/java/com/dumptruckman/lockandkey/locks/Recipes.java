/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.dumptruckman.lockandkey.locks;

import com.dumptruckman.lockandkey.LockAndKeyPlugin;
import com.dumptruckman.lockandkey.util.Log;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Recipes {

    @NotNull
    private final LockAndKeyPlugin plugin;

    public Recipes(@NotNull LockAndKeyPlugin plugin) {
        this.plugin = plugin;
    }

    public int loadRecipes() {
        int count = 0;
        for (ItemStack item : plugin.getExampleLockItems()) {
            int i = 0;
            List<Recipe> recipes = plugin.getServer().getRecipesFor(item);
            if (recipes != null && !recipes.isEmpty()) {
                i += recipes.size();
            }
            ShapedRecipe recipe = new ShapedRecipe(item);
            recipe.shape("ddd", "dxd", "ddd");
            recipe.setIngredient('x', item.getType());
            recipe.setIngredient('d', Material.REDSTONE);
            plugin.getServer().addRecipe(recipe);
            recipes = plugin.getServer().getRecipesFor(item);
            if (recipes != null && !recipes.isEmpty()) {
                count += recipes.size() - i;
            }
        }
        return count;
    }
}
