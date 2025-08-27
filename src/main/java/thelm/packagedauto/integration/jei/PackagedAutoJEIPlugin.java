package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.integration.jei.category.FluidPackageContentsCategory;
import thelm.packagedauto.integration.jei.category.FluidPackageFillingCategory;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;
import thelm.packagedauto.item.PackagedAutoItems;

@JeiPlugin
public class PackagedAutoJEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = ResourceLocation.parse("packagedauto:jei");
	public static final ResourceLocation BACKGROUND = ResourceLocation.parse("packagedauto:textures/gui/jei.png");

	public static IJeiRuntime jeiRuntime;
	private static List<ResourceLocation> allCategories;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(PackagedAutoItems.PACKAGE.get(), new PackageItemSubtypeInterpreter());
		registration.registerSubtypeInterpreter(PackagedAutoItems.VOLUME_PACKAGE.get(), new VolumePackageItemSubtypeInterpreter());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if(!ModList.get().isLoaded("emi")) {
			IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
			registration.addRecipeCategories(
					new PackageRecipeCategory(guiHelper),
					new PackagingCategory(guiHelper),
					new PackageProcessingCategory(guiHelper),
					new PackageContentsCategory(guiHelper),
					new FluidPackageFillingCategory(guiHelper),
					new FluidPackageContentsCategory(guiHelper));
		}
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		if(!ModList.get().isLoaded("emi")) {
			IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
			registration.addRecipeTransferHandler(new PackageRecipeTransferHandler(transferHelper), PackageRecipeCategory.TYPE);
			registration.addUniversalRecipeTransferHandler(new EncoderTransferHandler(transferHelper));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if(!ModList.get().isLoaded("emi")) {
			registration.addRecipeCatalyst(PackagedAutoItems.ENCODER.toStack(), PackageRecipeCategory.TYPE);
			registration.addRecipeCatalyst(PackagedAutoItems.PACKAGER.toStack(), PackagingCategory.TYPE);
			registration.addRecipeCatalyst(PackagedAutoItems.PACKAGER_EXTENSION.toStack(), PackagingCategory.TYPE);
			registration.addRecipeCatalyst(PackagedAutoItems.UNPACKAGER.toStack(), PackageProcessingCategory.TYPE);
			registration.addRecipeCatalyst(PackagedAutoItems.FLUID_PACKAGE_FILLER.toStack(), FluidPackageFillingCategory.TYPE);
		}
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if(!ModList.get().isLoaded("emi")) {
			IIngredientManager ingredientManager = registration.getJeiHelpers().getIngredientManager();
			registration.addGuiContainerHandler(EncoderScreen.class, new EncoderGuiHandler());
			registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new FluidVolumeGuiHandler(ingredientManager));
			registration.addGhostIngredientHandler(EncoderScreen.class, new EncoderGhostIngredientHandler());
		}
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		if(!ModList.get().isLoaded("emi")) {
			registration.addRecipeManagerPlugin(new PackageManagerPlugin());
			registration.addRecipeManagerPlugin(new FluidPackageManagerPlugin());
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		PackagedAutoJEIPlugin.jeiRuntime = jeiRuntime;
	}

	public static List<ResourceLocation> getAllRecipeCategories() {
		if(allCategories == null) {
			if(jeiRuntime == null) {
				return List.of();
			}
			allCategories = jeiRuntime.getRecipeManager().createRecipeCategoryLookup().includeHidden().get().map(c->c.getRecipeType().getUid()).toList();
		}
		return allCategories;
	}

	public static List<ResourceLocation> getRecipeCategoriesForRecipe(Object recipe) {
		if(jeiRuntime == null) {
			return List.of();
		}
		return jeiRuntime.getRecipeManager().createRecipeCategoryLookup().includeHidden().get().
				map(c->c.getRecipeType()).filter(t->t.getRecipeClass().isInstance(recipe)).map(t->t.getUid()).toList();
	}
}
