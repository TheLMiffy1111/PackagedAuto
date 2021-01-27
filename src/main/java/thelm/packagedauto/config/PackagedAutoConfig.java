package thelm.packagedauto.config;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thelm.packagedauto.tile.TileCrafter;
import thelm.packagedauto.tile.TileEncoder;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;
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
		category = "blocks.encoder";
		TileEncoder.patternSlots = config.get(category, "pattern_slots", TileEncoder.patternSlots, "How many pattern slots should the Package Recipe Encoder have.", 1, 20).getInt();
		category = "blocks.packager";
		TilePackager.energyCapacity = config.get(category, "energy_capacity", TilePackager.energyCapacity, "How much FE the Packager should hold.", 0, Integer.MAX_VALUE).getInt();
		TilePackager.energyReq = config.get(category, "energy_req", TilePackager.energyReq, "How much FE the Packager should use.", 0, Integer.MAX_VALUE).getInt();
		TilePackager.energyUsage = config.get(category, "energy_usage", TilePackager.energyUsage, "How much FE/t maximum the Packager should use.", 0, Integer.MAX_VALUE).getInt();
		TilePackager.drawMEEnergy = config.get(category, "draw_me_energy", TilePackager.drawMEEnergy, "Should the Packager draw energy from ME systems.").getBoolean();
		TilePackager.checkDisjoint = config.get(category, "check_disjoint", TilePackager.checkDisjoint, "Should the Packager not require exact inputs when it will not be ambiguous which package to make.").getBoolean();
		TilePackager.checkDisjoint = config.get(category, "force_disjoint", TilePackager.checkDisjoint, "Should the Packager not require exact inputs always. If it is ambiguous which package to use, the first matching will be used.\nIf this option is false, you still can force that behavior with redstone.").getBoolean();
		category = "blocks.unpackager";
		TileUnpackager.energyCapacity = config.get(category, "energy_capacity", TileUnpackager.energyCapacity, "How much FE the Unpackager should hold.", 0, Integer.MAX_VALUE).getInt();
		TileUnpackager.energyUsage = config.get(category, "energy_usage", TileUnpackager.energyUsage, "How much FE/t maximum the Unpackager should use.", 0, Integer.MAX_VALUE).getInt();
		TileUnpackager.drawMEEnergy = config.get(category, "draw_me_energy", TileUnpackager.drawMEEnergy, "Should the Unpackager draw energy from ME systems.").getBoolean();
		category = "blocks.packager_extension";
		TilePackagerExtension.energyCapacity = config.get(category, "energy_capacity", TilePackagerExtension.energyCapacity, "How much FE the Packager Extension should hold.", 0, Integer.MAX_VALUE).getInt();
		TilePackagerExtension.energyReq = config.get(category, "energy_req", TilePackagerExtension.energyReq, "How much FE the Packager Extension should use.", 0, Integer.MAX_VALUE).getInt();
		TilePackagerExtension.energyUsage = config.get(category, "energy_usage", TilePackagerExtension.energyUsage, "How much FE/t maximum the Packager Extension should use.", 0, Integer.MAX_VALUE).getInt();
		TilePackagerExtension.drawMEEnergy = config.get(category, "draw_me_energy", TilePackagerExtension.drawMEEnergy, "Should the Packager Extension draw energy from ME systems.").getBoolean();
		TilePackagerExtension.checkDisjoint = config.get(category, "check_disjoint", TilePackagerExtension.checkDisjoint, "Should the Packager Extension not require exact inputs when it will not be ambiguous which package to make.").getBoolean();
		TilePackagerExtension.forceDisjoint = config.get(category, "force_disjoint", TilePackagerExtension.forceDisjoint, "Should the Packager Extension always not require exact inputs. If it is ambiguous which package to use, the first matching will be used.\nIf this option is false, you still can force that behavior with redstone.").getBoolean();
		category = "blocks.crafter";
		TileCrafter.enabled = config.get(category, "enabled", TileCrafter.enabled, "Should the Package Crafter be enabled.").setRequiresMcRestart(true).getBoolean();
		TileCrafter.energyCapacity = config.get(category, "energy_capacity", TileCrafter.energyCapacity, "How much FE the Package Crafter should hold.", 0, Integer.MAX_VALUE).getInt();
		TileCrafter.energyReq = config.get(category, "energy_req", TileCrafter.energyReq, "How much FE the Package Crafter should use.", 0, Integer.MAX_VALUE).getInt();
		TileCrafter.energyUsage = config.get(category, "energy_usage", TileCrafter.energyUsage, "How much FE/t maximum the Package Crafter should use.", 0, Integer.MAX_VALUE).getInt();
		TileCrafter.drawMEEnergy = config.get(category, "draw_me_energy", TileCrafter.drawMEEnergy, "Should the Packager Crafter draw energy from ME systems.").getBoolean();
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
