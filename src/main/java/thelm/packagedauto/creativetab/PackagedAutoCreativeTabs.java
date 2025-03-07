package thelm.packagedauto.creativetab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thelm.packagedauto.item.PackagedAutoItems;

public class PackagedAutoCreativeTabs {

	private PackagedAutoCreativeTabs() {}

	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "packagedauto");

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
			"tab", ()->CreativeModeTab.builder().
			title(Component.translatable("itemGroup.packagedauto")).
			icon(PackagedAutoItems.PACKAGE::toStack).
			displayItems((parameters, output)->{
				output.accept(PackagedAutoItems.ENCODER);
				output.accept(PackagedAutoItems.PACKAGER);
				output.accept(PackagedAutoItems.PACKAGER_EXTENSION);
				output.accept(PackagedAutoItems.UNPACKAGER);
				output.accept(PackagedAutoItems.DISTRIBUTOR);
				output.accept(PackagedAutoItems.CRAFTING_PROXY);
				output.accept(PackagedAutoItems.CRAFTER);
				output.accept(PackagedAutoItems.FLUID_PACKAGE_FILLER);
				if(ModList.get().isLoaded("ae2")) {
					output.accept(PackagedAutoItems.PACKAGING_PROVIDER);
				}
				output.accept(PackagedAutoItems.RECIPE_HOLDER);
				output.accept(PackagedAutoItems.DISTRIBUTOR_MARKER);
				output.accept(PackagedAutoItems.proxy_marker);
				output.accept(PackagedAutoItems.SETTINGS_CLONER);
				output.accept(PackagedAutoItems.PACKAGE_COMPONENT);
				if(ModList.get().isLoaded("ae2")) {
					output.accept(PackagedAutoItems.ME_PACKAGE_COMPONENT);
				}
			}).build());
}
