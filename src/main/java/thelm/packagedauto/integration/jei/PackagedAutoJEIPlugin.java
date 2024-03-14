package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;

@JeiPlugin
public class PackagedAutoJEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation("packagedauto:jei");
	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/jei.png");

	public static IJeiRuntime jeiRuntime;
	public static List<ResourceLocation> allCategories = Collections.emptyList();

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(
				new PackageRecipeCategory(guiHelper),
				new PackagingCategory(guiHelper),
				new PackageProcessingCategory(guiHelper),
				new PackageContentsCategory(guiHelper));
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
		registration.addRecipeTransferHandler(new PackageRecipeTransferHandler(transferHelper), PackageRecipeCategory.UID);
		registration.addUniversalRecipeTransferHandler(new EncoderTransferHandler(transferHelper));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(EncoderBlock.INSTANCE), PackageRecipeCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(PackagerBlock.INSTANCE), PackagingCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(PackagerExtensionBlock.INSTANCE), PackagingCategory.UID);
		registration.addRecipeCatalyst(new ItemStack(UnpackagerBlock.INSTANCE), PackageProcessingCategory.UID);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(EncoderScreen.class, new EncoderGuiHandler());
		registration.addGhostIngredientHandler(EncoderScreen.class, new EncoderGhostIngredientHandler());
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		registration.addRecipeManagerPlugin(new PackageManagerPlugin());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		PackagedAutoJEIPlugin.jeiRuntime = jeiRuntime;
		allCategories = jeiRuntime.getRecipeManager().getRecipeCategories(null, true).stream().map(IRecipeCategory::getUid).collect(Collectors.toList());
	}

	public static List<ResourceLocation> getAllRecipeCategories() {
		return allCategories;
	}
}
