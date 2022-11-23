package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategory;
import thelm.packagedauto.client.gui.GuiEncoder;

@JEIPlugin
public class PackagedAutoJEIPlugin implements IModPlugin {

	public static IModRegistry registry;
	public static IJeiRuntime jeiRuntime;
	public static List<String> allCategories = Collections.emptyList();

	@Override
	public void register(IModRegistry registry) {
		PackagedAutoJEIPlugin.registry = registry;
		registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(EncoderTransferHandler.INSTANCE);
		registry.addGhostIngredientHandler(GuiEncoder.class, new EncoderGhostIngredientHandler());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		PackagedAutoJEIPlugin.jeiRuntime = jeiRuntime;
		allCategories = jeiRuntime.getRecipeRegistry().getRecipeCategories().stream().map(IRecipeCategory::getUid).collect(Collectors.toList());
	}

	public static List<String> getAllRecipeCategories() {
		return allCategories;
	}

	public static void showCategories(List<String> categories) {
		if(jeiRuntime != null && !categories.isEmpty()) {
			jeiRuntime.getRecipesGui().showCategories(categories);
		}
	}
}
