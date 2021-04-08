package thelm.packagedauto.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import thelm.packagedauto.tile.CrafterTile;
import thelm.packagedauto.tile.EncoderTile;
import thelm.packagedauto.tile.PackagerExtensionTile;
import thelm.packagedauto.tile.PackagerTile;
import thelm.packagedauto.tile.UnpackagerTile;

public class PackagedAutoConfig {

	private PackagedAutoConfig() {};

	private static ForgeConfigSpec serverSpec;

	public static ForgeConfigSpec.IntValue encoderPatternSlots;

	public static ForgeConfigSpec.IntValue packagerEnergyCapacity;
	public static ForgeConfigSpec.IntValue packagerEnergyReq;
	public static ForgeConfigSpec.IntValue packagerEnergyUsage;
	public static ForgeConfigSpec.BooleanValue packagerDrawMEEnergy;
	public static ForgeConfigSpec.BooleanValue packagerCheckDisjoint;
	public static ForgeConfigSpec.BooleanValue packagerForceDisjoint;

	public static ForgeConfigSpec.IntValue packagerExtensionEnergyCapacity;
	public static ForgeConfigSpec.IntValue packagerExtensionEnergyReq;
	public static ForgeConfigSpec.IntValue packagerExtensionEnergyUsage;
	public static ForgeConfigSpec.BooleanValue packagerExtensionDrawMEEnergy;
	public static ForgeConfigSpec.BooleanValue packagerExtensionCheckDisjoint;
	public static ForgeConfigSpec.BooleanValue packagerExtensionForceDisjoint;

	public static ForgeConfigSpec.IntValue unpackagerEnergyCapacity;
	public static ForgeConfigSpec.IntValue unpackagerEnergyUsage;
	public static ForgeConfigSpec.BooleanValue unpackagerDrawMEEnergy;

	public static ForgeConfigSpec.IntValue crafterEnergyCapacity;
	public static ForgeConfigSpec.IntValue crafterEnergyReq;
	public static ForgeConfigSpec.IntValue crafterEnergyUsage;
	public static ForgeConfigSpec.BooleanValue crafterDrawMEEnergy;

	public static void registerConfig() {
		buildConfig();
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
	}

	private static void buildConfig() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.push("encoder");
		builder.comment("How many pattern slots should the Package Recipe Encoder have.", "Warning: Changing this value when world is running may cause client crashes.");
		encoderPatternSlots = builder.defineInRange("pattern_slots", 20, 1, 20);
		builder.pop();

		builder.push("packager");
		builder.comment("How much FE the Packager should hold.");
		packagerEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Packager should use per operation.");
		packagerEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Packager can use.");
		packagerEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.comment("Should the Packager draw energy from ME systems.");
		packagerDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.comment("Should the Packager not require exact inputs when it will not be ambiguous which package to make.");
		packagerCheckDisjoint = builder.define("check_disjoint", true);
		builder.comment("Should the Packager not require exact inputs. Overrides check_disjoint.");
		packagerForceDisjoint = builder.define("force_disjoint", false);
		builder.pop();

		builder.push("packager_extension");
		builder.comment("How much FE the Packager Extension should hold.");
		packagerExtensionEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Packager Extension should use per operation.");
		packagerExtensionEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Packager Extension can use.");
		packagerExtensionEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.comment("Should the Packager Extension draw energy from ME systems.");
		packagerExtensionDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.comment("Should the Packager Extension not require exact inputs when it will not be ambiguous which package to make.");
		packagerExtensionCheckDisjoint = builder.define("check_disjoint", true);
		builder.comment("Should the Packager Extension not require exact inputs. Overrides check_disjoint.");
		packagerExtensionForceDisjoint = builder.define("force_disjoint", false);
		builder.pop();

		builder.push("unpackager");
		builder.comment("How much FE the Unpackager should hold.");
		unpackagerEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Unpackager should use per operation.");
		unpackagerEnergyUsage = builder.defineInRange("energy_usage", 50, 0, Integer.MAX_VALUE);
		builder.comment("Should the Unpackager draw energy from ME systems.");
		unpackagerDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.pop();

		builder.push("crafter");
		builder.comment("How much FE the Crafter should hold.");
		crafterEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Crafter should use per operation.");
		crafterEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Crafter can use.");
		crafterEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.comment("Should the Crafter draw energy from ME systems.");
		crafterDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.pop();

		serverSpec = builder.build();
	}

	public static void reloadServerConfig() {
		EncoderTile.patternSlots = encoderPatternSlots.get();

		PackagerTile.energyCapacity = packagerEnergyCapacity.get();
		PackagerTile.energyReq = packagerEnergyReq.get();
		PackagerTile.energyUsage = packagerEnergyUsage.get();
		PackagerTile.drawMEEnergy = packagerDrawMEEnergy.get();
		PackagerTile.checkDisjoint = packagerCheckDisjoint.get();
		PackagerTile.forceDisjoint = packagerForceDisjoint.get();

		PackagerExtensionTile.energyCapacity = packagerExtensionEnergyCapacity.get();
		PackagerExtensionTile.energyReq = packagerExtensionEnergyReq.get();
		PackagerExtensionTile.energyUsage = packagerExtensionEnergyUsage.get();
		PackagerExtensionTile.drawMEEnergy = packagerExtensionDrawMEEnergy.get();
		PackagerExtensionTile.checkDisjoint = packagerExtensionCheckDisjoint.get();
		PackagerExtensionTile.forceDisjoint = packagerExtensionForceDisjoint.get();

		UnpackagerTile.energyCapacity = unpackagerEnergyCapacity.get();
		UnpackagerTile.energyUsage = unpackagerEnergyUsage.get();
		UnpackagerTile.drawMEEnergy = unpackagerDrawMEEnergy.get();

		CrafterTile.energyCapacity = crafterEnergyCapacity.get();
		CrafterTile.energyReq = crafterEnergyReq.get();
		CrafterTile.energyUsage = crafterEnergyUsage.get();
		CrafterTile.drawMEEnergy = crafterDrawMEEnergy.get();
	}
}
