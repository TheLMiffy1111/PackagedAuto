package thelm.packagedauto.config;

import java.util.List;
import java.util.Set;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import thelm.packagedauto.block.entity.CrafterBlockEntity;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;
import thelm.packagedauto.block.entity.DistributorBlockEntity;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;

public class PackagedAutoConfig {

	private PackagedAutoConfig() {}

	private static ForgeConfigSpec serverSpec;

	public static ForgeConfigSpec.IntValue encoderPatternSlots;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> encoderDisabledRecipeTypes;

	public static ForgeConfigSpec.IntValue packagerEnergyCapacity;
	public static ForgeConfigSpec.IntValue packagerEnergyReq;
	public static ForgeConfigSpec.IntValue packagerEnergyUsage;
	public static ForgeConfigSpec.IntValue packagerRefreshInterval;
	public static ForgeConfigSpec.BooleanValue packagerDrawMEEnergy;

	public static ForgeConfigSpec.IntValue packagerExtensionEnergyCapacity;
	public static ForgeConfigSpec.IntValue packagerExtensionEnergyReq;
	public static ForgeConfigSpec.IntValue packagerExtensionEnergyUsage;
	public static ForgeConfigSpec.IntValue packagerExtensionRefreshInterval;
	public static ForgeConfigSpec.BooleanValue packagerExtensionDrawMEEnergy;

	public static ForgeConfigSpec.IntValue unpackagerEnergyCapacity;
	public static ForgeConfigSpec.IntValue unpackagerEnergyUsage;
	public static ForgeConfigSpec.IntValue unpackagerRefreshInterval;
	public static ForgeConfigSpec.BooleanValue unpackagerDrawMEEnergy;

	public static ForgeConfigSpec.IntValue distributorRange;
	public static ForgeConfigSpec.IntValue distributorRefreshInterval;

	public static ForgeConfigSpec.IntValue craftingProxyRange;

	public static ForgeConfigSpec.IntValue crafterEnergyCapacity;
	public static ForgeConfigSpec.IntValue crafterEnergyReq;
	public static ForgeConfigSpec.IntValue crafterEnergyUsage;
	public static ForgeConfigSpec.IntValue crafterRefreshInterval;
	public static ForgeConfigSpec.BooleanValue crafterDrawMEEnergy;

	public static ForgeConfigSpec.IntValue fluidPackageFillerEnergyCapacity;
	public static ForgeConfigSpec.IntValue fluidPackageFillerEnergyReq;
	public static ForgeConfigSpec.IntValue fluidPackageFillerEnergyUsage;
	public static ForgeConfigSpec.IntValue fluidPackageFillerRefreshInterval;

	@SuppressWarnings("removal")
	public static void registerConfig() {
		buildConfig();
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
	}

	private static void buildConfig() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.push("encoder");
		builder.comment("How many pattern slots should the Package Recipe Encoder have.", "Warning: Changing this value when world is running may cause client crashes.");
		encoderPatternSlots = builder.defineInRange("pattern_slots", 20, 1, 20);
		builder.comment("The list of recipe types to disable in the Package Recipe Encoder.");
		encoderDisabledRecipeTypes = builder.defineListAllowEmpty("disabled_recipe_types", List.of(), String.class::isInstance);
		builder.pop();

		builder.push("packager");
		builder.comment("How much FE the Packager should hold.");
		packagerEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Packager should use per operation.");
		packagerEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Packager can use.");
		packagerEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.comment("How many ticks should the Packager wait between each refresh.");
		packagerRefreshInterval = builder.defineInRange("refresh_interval", 4, 1, 40);
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
		builder.comment("How many ticks should the Packager Extension wait between each refresh.");
		packagerExtensionRefreshInterval = builder.defineInRange("refresh_interval", 4, 1, 40);
		builder.comment("Should the Packager Extension draw energy from ME systems.");
		packagerExtensionDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.pop();

		builder.push("unpackager");
		builder.comment("How much FE the Unpackager should hold.");
		unpackagerEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Unpackager should use per operation.");
		unpackagerEnergyUsage = builder.defineInRange("energy_usage", 50, 0, Integer.MAX_VALUE);
		builder.comment("How many ticks should the Unpackager wait between each refresh.");
		unpackagerRefreshInterval = builder.defineInRange("refresh_interval", 4, 1, 40);
		builder.comment("Should the Unpackager draw energy from ME systems.");
		unpackagerDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.pop();

		builder.push("distributor");
		builder.comment("How large the range of the Positioned Package Distributor should be.");
		distributorRange = builder.defineInRange("range", 16, 1, Integer.MAX_VALUE);
		builder.comment("How many ticks should the Positioned Package Distributor wait between each refresh.");
		distributorRefreshInterval = builder.defineInRange("refresh_interval", 4, 1, 40);
		builder.pop();

		builder.push("crafting_proxy");
		builder.comment("How large the range of the Package Crafting Machine Proxy should be.");
		craftingProxyRange = builder.defineInRange("range", 8, 1, Integer.MAX_VALUE);
		builder.pop();

		builder.push("crafter");
		builder.comment("How much FE the Package Crafter should hold.");
		crafterEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Package Crafter should use per operation.");
		crafterEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How much FE/t maximum the Package Crafter can use.");
		crafterEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.comment("How many ticks should the Package Crafter wait between each refresh.");
		crafterRefreshInterval = builder.defineInRange("refresh_interval", 4, 1, 40);
		builder.comment("Should the Package Crafter draw energy from ME systems.");
		crafterDrawMEEnergy = builder.define("draw_me_energy", true);
		builder.pop();

		builder.push("fluid_package_filler");
		builder.comment("How much FE the Fluid Package Filler should hold.");
		fluidPackageFillerEnergyCapacity = builder.defineInRange("energy_capacity", 5000, 0, Integer.MAX_VALUE);
		builder.comment("How much total FE the Fluid Package Filler should use per operation.");
		fluidPackageFillerEnergyReq = builder.defineInRange("energy_req", 500, 0, Integer.MAX_VALUE);
		builder.comment("How many ticks should the Fluid Package Filler wait between each refresh.");
		fluidPackageFillerRefreshInterval = builder.defineInRange("refresh_interval", 4, 1, 40);
		builder.comment("How much FE/t maximum the Fluid Package Filler can use.");
		fluidPackageFillerEnergyUsage = builder.defineInRange("energy_usage", 100, 0, Integer.MAX_VALUE);
		builder.pop();

		serverSpec = builder.build();
	}

	public static void reloadServerConfig() {
		EncoderBlockEntity.patternSlots = encoderPatternSlots.get();
		EncoderBlockEntity.disabledRecipeTypes = Set.copyOf(encoderDisabledRecipeTypes.get());

		PackagerBlockEntity.energyCapacity = packagerEnergyCapacity.get();
		PackagerBlockEntity.energyReq = packagerEnergyReq.get();
		PackagerBlockEntity.energyUsage = packagerEnergyUsage.get();
		PackagerBlockEntity.refreshInterval = packagerRefreshInterval.get();
		PackagerBlockEntity.drawMEEnergy = packagerDrawMEEnergy.get();

		PackagerExtensionBlockEntity.energyCapacity = packagerExtensionEnergyCapacity.get();
		PackagerExtensionBlockEntity.energyReq = packagerExtensionEnergyReq.get();
		PackagerExtensionBlockEntity.energyUsage = packagerExtensionEnergyUsage.get();
		PackagerExtensionBlockEntity.refreshInterval = packagerExtensionRefreshInterval.get();
		PackagerExtensionBlockEntity.drawMEEnergy = packagerExtensionDrawMEEnergy.get();

		UnpackagerBlockEntity.energyCapacity = unpackagerEnergyCapacity.get();
		UnpackagerBlockEntity.energyUsage = unpackagerEnergyUsage.get();
		UnpackagerBlockEntity.refreshInterval = unpackagerRefreshInterval.get();
		UnpackagerBlockEntity.drawMEEnergy = unpackagerDrawMEEnergy.get();

		DistributorBlockEntity.range = distributorRange.get();
		DistributorBlockEntity.refreshInterval = distributorRefreshInterval.get();

		CraftingProxyBlockEntity.range = craftingProxyRange.get();

		CrafterBlockEntity.energyCapacity = crafterEnergyCapacity.get();
		CrafterBlockEntity.energyReq = crafterEnergyReq.get();
		CrafterBlockEntity.energyUsage = crafterEnergyUsage.get();
		CrafterBlockEntity.refreshInterval = crafterRefreshInterval.get();
		CrafterBlockEntity.drawMEEnergy = crafterDrawMEEnergy.get();

		FluidPackageFillerBlockEntity.energyCapacity = fluidPackageFillerEnergyCapacity.get();
		FluidPackageFillerBlockEntity.energyReq = fluidPackageFillerEnergyReq.get();
		FluidPackageFillerBlockEntity.energyUsage = fluidPackageFillerEnergyUsage.get();
		FluidPackageFillerBlockEntity.refreshInterval = fluidPackageFillerRefreshInterval.get();
	}
}
