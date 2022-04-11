package moreid;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.recipes.MCRecipeManager;
import crafttweaker.runtime.ScriptLoader;
import ilib.util.Registries;
import roj.collect.MyHashSet;
import stanhebben.zenscript.ZenModule;

import net.minecraft.item.crafting.IRecipe;

import net.minecraftforge.registries.ForgeRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roj234
 */
//!!AT [["crafttweaker.runtime.ScriptLoader", ["loaderStage"]]]
public class CTRL {
    public static final List<IRecipe> TOADD_RECIPES = new ArrayList<>();

    public static final List<IRecipe> ADDED_RECIPES = new ArrayList<>();

    public static final List<IRecipe> TODELETE_RECIPES = new ArrayList<>();

    public static final List<IRecipe> DELETED_RECIPES = new ArrayList<>();

    public static void reloadCraftTweaker() {
        MoreId.logger().info("Reloading CT... ");
        MCRecipeManager.recipesToAdd.clear();
        MCRecipeManager.recipesToRemove.clear();
        ZenModule.loadedClasses.clear();
        ScriptLoader loader = CraftTweakerAPI.tweaker.getOrCreateLoader("crafttweaker", "recipeevent");
        loader.loaderStage = ScriptLoader.LoaderStage.NOT_LOADED;
        loader.setMainName("recipe_refresher");
        CraftTweakerAPI.tweaker.loadScript(false, loader);
        MCRecipeManager.recipesToAdd.forEach(CraftTweakerAPI::apply);
        MCRecipeManager.recipesToRemove.forEach(CraftTweakerAPI::apply);
        MoreId.logger().info("CT Reload successful!");
    }

    public static void refreshRecipe() {
        ForgeRegistry<IRecipe> recipes = (ForgeRegistry<IRecipe>) Registries.recipe();

        boolean flag = recipes.isLocked();
        if (flag) recipes.unfreeze();

        MyHashSet<IRecipe> total = new MyHashSet<>(recipes);
        total.removeIf(e -> e.getRegistryName().getNamespace().equals("crafttweaker"));

        recipes.clear();
        for (IRecipe entry : total) {
            recipes.register(entry);
        }

        for (IRecipe recipe : DELETED_RECIPES) {
            recipes.register(recipe);
        }
        DELETED_RECIPES.clear();

        for (IRecipe recipe : TODELETE_RECIPES) {
            recipes.remove(recipe.getRegistryName());
        }
        DELETED_RECIPES.addAll(TODELETE_RECIPES);
        TODELETE_RECIPES.clear();

        for (IRecipe recipe : ADDED_RECIPES) {
            recipes.remove(recipe.getRegistryName());
        }
        ADDED_RECIPES.clear();

        for (IRecipe recipe : TOADD_RECIPES) {
            if (recipe == null) continue;
            IRecipe r = recipes.getValue(recipe.getRegistryName());
            if (r != null)
                MoreId.logger().warn("Found recipe overriding! (" + recipe.getRegistryName().toString() + ")");
            recipes.register(recipe);
        }
        ADDED_RECIPES.addAll(TOADD_RECIPES);
        TOADD_RECIPES.clear();

        reloadCraftTweaker();

        if (flag) recipes.freeze();
    }
}
