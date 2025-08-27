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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.block.FluidPackageFillerBlock;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.integration.jei.category.FluidPackageContentsCategory;
import thelm.packagedauto.integration.jei.category.FluidPackageFillingCategory;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;
import thelm.packagedauto.item.PackageItem;
import thelm.packagedauto.item.VolumePackageItem;

@JeiPlugin
public class PackagedAutoJEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation("packagedauto:jei");
	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/jei.png");

	public static IJeiRuntime jeiRuntime;
	private static List<ResourceLocation> allCategories;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(PackageItem.INSTANCE, VolumePackageItem.INSTANCE);
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
			registration.addRecipeCatalyst(new ItemStack(EncoderBlock.INSTANCE), PackageRecipeCategory.TYPE);
			registration.addRecipeCatalyst(new ItemStack(PackagerBlock.INSTANCE), PackagingCategory.TYPE);
			registration.addRecipeCatalyst(new ItemStack(PackagerExtensionBlock.INSTANCE), PackagingCategory.TYPE);
			registration.addRecipeCatalyst(new ItemStack(UnpackagerBlock.INSTANCE), PackageProcessingCategory.TYPE);
			registration.addRecipeCatalyst(new ItemStack(FluidPackageFillerBlock.INSTANCE), FluidPackageFillingCategory.TYPE);
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
