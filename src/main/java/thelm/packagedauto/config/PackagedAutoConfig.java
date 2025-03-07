package thelm.packagedauto.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import thelm.packagedauto.tile.CrafterTile;
import thelm.packagedauto.tile.CraftingProxyTile;
import thelm.packagedauto.tile.DistributorTile;
import thelm.packagedauto.tile.EncoderTile;
import thelm.packagedauto.tile.PackagerExtensionTile;
import thelm.packagedauto.tile.PackagerTile;
import thelm.packagedauto.tile.UnpackagerTile;

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
		encoderDisabledRecipeTypes = builder.defineList("disabled_recipe_types", ArrayList<String>::new, s->true);
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

		builder.push("crafting_proxy");
		builder.comment("How large the range of the Package Crafting Machine Proxy should be.");
		craftingProxyRange = builder.defineInRange("range", 8, 1, Integer.MAX_VALUE);

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

		serverSpec = builder.build();
	}

	public static void reloadServerConfig() {
		EncoderTile.patternSlots = encoderPatternSlots.get();
		EncoderTile.disabledRecipeTypes = ImmutableSet.copyOf(encoderDisabledRecipeTypes.get());

		PackagerTile.energyCapacity = packagerEnergyCapacity.get();
		PackagerTile.energyReq = packagerEnergyReq.get();
		PackagerTile.energyUsage = packagerEnergyUsage.get();
		PackagerTile.refreshInterval = packagerRefreshInterval.get();
		PackagerTile.drawMEEnergy = packagerDrawMEEnergy.get();

		PackagerExtensionTile.energyCapacity = packagerExtensionEnergyCapacity.get();
		PackagerExtensionTile.energyReq = packagerExtensionEnergyReq.get();
		PackagerExtensionTile.energyUsage = packagerExtensionEnergyUsage.get();
		PackagerExtensionTile.refreshInterval = packagerExtensionRefreshInterval.get();
		PackagerExtensionTile.drawMEEnergy = packagerExtensionDrawMEEnergy.get();

		UnpackagerTile.energyCapacity = unpackagerEnergyCapacity.get();
		UnpackagerTile.energyUsage = unpackagerEnergyUsage.get();
		UnpackagerTile.refreshInterval = unpackagerRefreshInterval.get();
		UnpackagerTile.drawMEEnergy = unpackagerDrawMEEnergy.get();

		DistributorTile.range = distributorRange.get();
		DistributorTile.refreshInterval = distributorRefreshInterval.get();

		CraftingProxyTile.range = craftingProxyRange.get();

		CrafterTile.energyCapacity = crafterEnergyCapacity.get();
		CrafterTile.energyReq = crafterEnergyReq.get();
		CrafterTile.energyUsage = crafterEnergyUsage.get();
		CrafterTile.refreshInterval = crafterRefreshInterval.get();
		CrafterTile.drawMEEnergy = crafterDrawMEEnergy.get();
	}
}
