package thelm.packagedauto.menu;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;

public class PackagedAutoMenus {

	private PackagedAutoMenus() {}

	public static <C extends AbstractContainerMenu, T extends BlockEntity> Supplier<MenuType<C>> of(PositionalBlockEntityMenuFactory.Factory<C, T> factory) {
		return ()->IMenuTypeExtension.create(new PositionalBlockEntityMenuFactory<>(factory));
	}

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "packagedauto");

	public static final DeferredHolder<MenuType<?>, MenuType<EncoderMenu>> ENCODER = MENUS.register("encoder", of(EncoderMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<PackagerMenu>> PACKAGER = MENUS.register("packager", of(PackagerMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<PackagerExtensionMenu>> PACKAGER_EXTENSION = MENUS.register("packager_extension", of(PackagerExtensionMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<UnpackagerMenu>> UNPACKAGER = MENUS.register("unpackager", of(UnpackagerMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<DistributorMenu>> DISTRIBUTOR = MENUS.register("distributor", of(DistributorMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<CraftingProxyMenu>> CRAFTING_PROXY = MENUS.register("crafting_proxy", of(CraftingProxyMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<CrafterMenu>> CRAFTER = MENUS.register("crafter", of(CrafterMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<FluidPackageFillerMenu>> FLUID_PACKAGE_FILLER = MENUS.register("fluid_package_filler", of(FluidPackageFillerMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<PackagingProviderMenu>> PACKAGING_PROVIDER = MENUS.register("packaging_provider", of(PackagingProviderMenu::new));
}
