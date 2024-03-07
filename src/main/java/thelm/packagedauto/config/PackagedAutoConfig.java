package thelm.packagedauto.config;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import thelm.packagedauto.block.entity.CrafterBlockEntity;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;

public class PackagedAutoConfig {

	private PackagedAutoConfig() {}

	private static ModConfigSpec serverSpec;

	public static ModConfigSpec.IntValue encoderPatternSlots;

	public static ModConfigSpec.IntValue packagerEnergyCapacity;
	public static ModConfigSpec.IntValue packagerEnergyReq;
	public static ModConfigSpec.IntValue packagerEnergyUsage;
	public static ModConfigSpec.BooleanValue packagerDrawMEEnergy;

	public static ModConfigSpec.IntValue packagerExtensionEnergyCapacity;
	public static ModConfigSpec.IntValue packagerExtensionEnergyReq;
	public static ModConfigSpec.IntValue packagerExtensionEnergyUsage;
	public static ModConfigSpec.BooleanValue packagerExtensionDrawMEEnergy;

	public static ModConfigSpec.IntValue unpackagerEnergyCapacity;
	public static ModConfigSpec.IntValue unpackagerEnergyUsage;
	public static ModConfigSpec.BooleanValue unpackagerDrawMEEnergy;

	public static ModConfigSpec.IntValue crafterEnergyCapacity;
	public static ModConfigSpec.IntValue crafterEnergyReq;
	public static ModConfigSpec.IntValue crafterEnergyUsage;
	public static ModConfigSpec.BooleanValue crafterDrawMEEnergy;

	public static ModConfigSpec.IntValue fluidPackageFillerEnergyCapacity;
	public static ModConfigSpec.IntValue fluidPackageFillerEnergyReq;
	public static ModConfigSpec.IntValue fluidPackageFillerEnergyUsage;

	public static void registerConfig() {
		buildConfig();
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
	}

	private static void buildConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
		builder.comment("How much FE the Package Crafter should hold.");
		crafterEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Package Crafter should use per operation.");
		crafterEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Package Crafter can use.");
		crafterEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.comment("Should the Package Crafter draw energy from ME systems.");
		crafterDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.pop();

		builder.push("fluid_package_filler");
		builder.comment("How much FE the Fluid Package Filler should hold.");
		fluidPackageFillerEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Fluid Package Filler should use per operation.");
		fluidPackageFillerEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Fluid Package Filler can use.");
		fluidPackageFillerEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.pop();

		serverSpec = builder.build();
	}

	public static void reloadServerConfig() {
		EncoderBlockEntity.patternSlots = encoderPatternSlots.get();

		PackagerBlockEntity.energyCapacity = packagerEnergyCapacity.get();
		PackagerBlockEntity.energyReq = packagerEnergyReq.get();
		PackagerBlockEntity.energyUsage = packagerEnergyUsage.get();
		PackagerBlockEntity.drawMEEnergy = packagerDrawMEEnergy.get();

		PackagerExtensionBlockEntity.energyCapacity = packagerExtensionEnergyCapacity.get();
		PackagerExtensionBlockEntity.energyReq = packagerExtensionEnergyReq.get();
		PackagerExtensionBlockEntity.energyUsage = packagerExtensionEnergyUsage.get();
		PackagerExtensionBlockEntity.drawMEEnergy = packagerExtensionDrawMEEnergy.get();

		UnpackagerBlockEntity.energyCapacity = unpackagerEnergyCapacity.get();
		UnpackagerBlockEntity.energyUsage = unpackagerEnergyUsage.get();
		UnpackagerBlockEntity.drawMEEnergy = unpackagerDrawMEEnergy.get();

		CrafterBlockEntity.energyCapacity = crafterEnergyCapacity.get();
		CrafterBlockEntity.energyReq = crafterEnergyReq.get();
		CrafterBlockEntity.energyUsage = crafterEnergyUsage.get();
		CrafterBlockEntity.drawMEEnergy = crafterDrawMEEnergy.get();

		FluidPackageFillerBlockEntity.energyCapacity = fluidPackageFillerEnergyCapacity.get();
		FluidPackageFillerBlockEntity.energyReq = fluidPackageFillerEnergyReq.get();
		FluidPackageFillerBlockEntity.energyUsage = fluidPackageFillerEnergyUsage.get();
	}
}
