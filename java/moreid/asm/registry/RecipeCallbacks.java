package moreid.asm.registry;

import ilib.misc.DummyRecipe;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;

/**
 * @author Roj234
 */
public class RecipeCallbacks implements IForgeRegistry.MissingFactory<IRecipe>, IForgeRegistry.ValidateCallback<IRecipe> {
    public void onValidate(IForgeRegistryInternal<IRecipe> owner, RegistryManager stage, int id, ResourceLocation key, IRecipe obj) {
        if (stage == RegistryManager.ACTIVE) {
            Item item = obj.getRecipeOutput().getItem();
            if (!stage.getRegistry(Item.class).containsValue(item)) {
                throw new IllegalStateException(String.format("Recipe %s (%s) 合成了未注册的物品 %s (%s)", key, obj, item.getRegistryName(), item));
            }
        }
    }

    public IRecipe createMissing(ResourceLocation key, boolean isNetwork) {
        return isNetwork ? new DummyRecipe().setRegistryName(key) : null;
    }
}
