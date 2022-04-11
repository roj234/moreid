package moreid.asm;

import moreid.Config;
import moreid.asm.registry.BlockCallbacks;
import moreid.asm.registry.RecipeCallbacks;
import roj.asm.nixim.Inject;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;
import roj.util.Helpers;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.registries.*;

import java.util.Locale;

/**
 * @author Roj234
 */
@Nixim("net.minecraftforge.registries.GameData")
class NxGameData extends GameData {
    @Shadow("hasInit")
    static boolean hasInit;
    @Shadow("DISABLE_VANILLA_REGISTRIES")
    static boolean DISABLE_VANILLA_REGISTRIES;
    @Shadow("entityRegistry")
    static ForgeRegistry<EntityEntry> entityRegistry;

    @Shadow("SERIALIZERS")
    public static ResourceLocation SERIALIZERS;

    @Shadow("makeRegistry")
    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type, int min, int max) {
        return Helpers.nonnull();
    }

    @Shadow("makeRegistry")
    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type, int max) {
        return Helpers.nonnull();
    }

    @Shadow("makeRegistry")
    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type, int max, ResourceLocation _default) {
        return Helpers.nonnull();
    }

    @Inject("checkPrefix")
    public static ResourceLocation checkPrefix(String name, boolean warnOverrides) {
        int index = name.lastIndexOf(':');
        String ns = (index == -1) ? "" : name.substring(0, index).toLowerCase(Locale.ROOT);
        String path = (index == -1) ? name : name.substring(index + 1);
        if (ns.isEmpty()) {
            final ModContainer mod = Loader.instance().activeModContainer();
            if (mod != null) {
                ns = ((mod instanceof InjectedModContainer && ((InjectedModContainer)mod).wrappedContainer instanceof FMLContainer) ? "minecraft" : mod.getModId().toLowerCase(Locale.ROOT));
            }
        }
        return new ResourceLocation(ns, path);
    }

    @Inject("init")
    public static void init() {
        if (DISABLE_VANILLA_REGISTRIES) {
            FMLLog.bigWarning("DISABLING VANILLA REGISTRY CREATION AS PER SYSTEM VARIABLE SETTING! forge.disableVanillaGameData");
        } else if (!hasInit) {
            hasInit = true;
            makeRegistry(BLOCKS, Block.class, Config.blockMax, new ResourceLocation("air")).addCallback(new BlockCallbacks()).create();
            makeRegistry(ITEMS, Item.class, Config.itemMax).addCallback(ItemCallbacks.INSTANCE).create();
            makeRegistry(POTIONS, Potion.class, Config.potionMax).create();
            makeRegistry(BIOMES, Biome.class, Config.biomeMax).create();
            makeRegistry(SOUNDEVENTS, SoundEvent.class, 67108863).create();
            makeRegistry(POTIONTYPES, PotionType.class, 67108863, new ResourceLocation("empty")).create();
            makeRegistry(ENCHANTMENTS, Enchantment.class, Config.enchantMax).create();
            makeRegistry(RECIPES, IRecipe.class, 67108863).disableSaving().allowModification().addCallback(new RecipeCallbacks()).create();
            makeRegistry(PROFESSIONS, VillagerRegistry.VillagerProfession.class, Config.villagerProfessionMax).create();
            entityRegistry = (ForgeRegistry<EntityEntry>)makeRegistry(ENTITIES, EntityEntry.class, 67108863).addCallback(EntityCallbacks.INSTANCE).create();
            try {
                makeRegistry(SERIALIZERS, DataSerializerEntry.class, 256, 67108863).disableSaving().disableOverrides().addCallback(SerializerCallbacks.INSTANCE).create();
            } catch (Throwable ignored) {}
        }
    }
}