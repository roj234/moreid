package moreid;

import roj.config.JSONConfiguration;
import roj.config.data.CMapping;

import java.io.File;

/**
 * @author Roj234
 */
public final class Config extends JSONConfiguration {
    public static int blockMax, itemMax, biomeMax, potionMax, enchantMax, villagerProfessionMax;
    public static boolean useNewChunkFormat, tinyTileMap;

    static {
        new Config().save();
    }

    private Config() {
        super(new File("config/MoreId.json"));
    }

    @Override
    protected void readConfig(CMapping map) {
        blockMax = map.putIfAbsent("最大方块", 100000);
        itemMax = map.putIfAbsent("最大物品", 65535);
        biomeMax = map.putIfAbsent("最大生物群系", 255);
        potionMax = map.putIfAbsent("最大药水", 16384);
        enchantMax = map.putIfAbsent("最大附魔", 32766);
        villagerProfessionMax = map.putIfAbsent("最大村民职业", 1024);

        useNewChunkFormat = !map.putIfAbsent("恢复原版格式(卸载MoreId)", false);

        tinyTileMap = map.putIfAbsent("Tiny TileEntity Map", true);
    }
}
