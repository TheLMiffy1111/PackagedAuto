package thelm.packagedauto.item;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thelm.packagedauto.block.PackagedAutoBlocks;

public class PackagedAutoItems {

	private PackagedAutoItems() {}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("packagedauto");

	public static final DeferredItem<?> ENCODER = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.ENCODER);
	public static final DeferredItem<?> PACKAGER = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.PACKAGER);
	public static final DeferredItem<?> PACKAGER_EXTENSION = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.PACKAGER_EXTENSION);
	public static final DeferredItem<?> UNPACKAGER = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.UNPACKAGER);
	public static final DeferredItem<?> DISTRIBUTOR = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.DISTRIBUTOR);
	public static final DeferredItem<?> CRAFTING_PROXY = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.CRAFTING_PROXY);
	public static final DeferredItem<?> CRAFTER = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.CRAFTER);
	public static final DeferredItem<?> FLUID_PACKAGE_FILLER = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.FLUID_PACKAGE_FILLER);
	public static final DeferredItem<?> PACKAGING_PROVIDER = ITEMS.registerSimpleBlockItem(PackagedAutoBlocks.PACKAGING_PROVIDER);

	public static final DeferredItem<RecipeHolderItem> RECIPE_HOLDER = ITEMS.register("recipe_holder", RecipeHolderItem::new);
	public static final DeferredItem<MarkerItem> DISTRIBUTOR_MARKER = ITEMS.registerItem("distributor_marker", MarkerItem::new);
	public static final DeferredItem<MarkerItem> PROXY_MARKER = ITEMS.registerItem("proxy_marker", MarkerItem::new);
	public static final DeferredItem<SettingsClonerItem> SETTINGS_CLONER = ITEMS.register("settings_cloner", SettingsClonerItem::new);
	public static final DeferredItem<PackageItem> PACKAGE = ITEMS.register("package", PackageItem::new);
	public static final DeferredItem<VolumePackageItem> VOLUME_PACKAGE = ITEMS.register("volume_package", VolumePackageItem::new);
	public static final DeferredItem<?> PACKAGE_COMPONENT = ITEMS.registerSimpleItem("package_component");
	public static final DeferredItem<?> ME_PACKAGE_COMPONENT = ITEMS.registerSimpleItem("me_package_component");
}
