package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.client.screen.EncoderScreen;

@JeiPlugin
public class PackagedAutoJEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation("packagedauto:jei");

	public static IJeiRuntime jeiRuntime;
	public static List<ResourceLocation> allCategories = Collections.emptyList();

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addUniversalRecipeTransferHandler(new EncoderTransferHandler(registration.getTransferHelper()));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(EncoderScreen.class, new EncoderGuiHandler());
		registration.addGhostIngredientHandler(EncoderScreen.class, new EncoderGhostIngredientHandler());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		PackagedAutoJEIPlugin.jeiRuntime = jeiRuntime;
		allCategories = jeiRuntime.getRecipeManager().getRecipeCategories(null, false).stream().map(IRecipeCategory::getUid).collect(Collectors.toList());
	}

	public static List<ResourceLocation> getAllRecipeCategories() {
		return allCategories;
	}
}
