package thelm.packagedauto.config;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thelm.packagedauto.tile.TileCrafter;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TileUnpackager;

public class PackagedAutoConfig {

	private PackagedAutoConfig() {}

	public static Configuration config;

	public static void init(File file) {
		MinecraftForge.EVENT_BUS.register(PackagedAutoConfig.class);
		config = new Configuration(file);
		config.load();
		init();
	}

	public static void init() {
		String category;
		category = "blocks.packager";
		TilePackager.energyCapacity = config.get(category, "energy_capacity", TilePackager.energyCapacity, "How much FE the Packager should hold.", 0, Integer.MAX_VALUE).getInt();
		TilePackager.energyReq = config.get(category, "energy_req", TilePackager.energyReq, "How much FE the Packager should use.", 0, Integer.MAX_VALUE).getInt();
		TilePackager.energyUsage = config.get(category, "energy_usage", TilePackager.energyUsage, "How much FE/t maximum the Packager should use.", 0, Integer.MAX_VALUE).getInt();
		category = "blocks.unpackager";
		TileUnpackager.energyCapacity = config.get(category, "energy_capacity", TileUnpackager.energyCapacity, "How much FE the Unpackager should hold.", 0, Integer.MAX_VALUE).getInt();
		TileUnpackager.energyUsage = config.get(category, "energy_usage", TileUnpackager.energyUsage, "How much FE/t maximum the Unpackager should use.", 0, Integer.MAX_VALUE).getInt();
		category = "blocks.crafter";
		TileCrafter.enabled = config.get(category, "enabled", TileCrafter.enabled, "Should the Package Crafter be enabled.").setRequiresMcRestart(true).getBoolean();
		TileCrafter.energyCapacity = config.get(category, "energy_capacity", TileCrafter.energyCapacity, "How much FE the Package Crafter should hold.", 0, Integer.MAX_VALUE).getInt();
		TileCrafter.energyReq = config.get(category, "energy_req", TileCrafter.energyReq, "How much FE the Package Crafter should use.", 0, Integer.MAX_VALUE).getInt();
		TileCrafter.energyUsage = config.get(category, "energy_usage", TileCrafter.energyUsage, "How much FE/t maximum the Package Crafter should use.", 0, Integer.MAX_VALUE).getInt();
		if(config.hasChanged()) {
			config.save();
		}
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if(event.getModID().equals("packagedauto")) {
			init();
		}
	}
}
