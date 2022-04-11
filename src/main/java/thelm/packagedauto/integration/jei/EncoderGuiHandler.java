package thelm.packagedauto.integration.jei;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import thelm.packagedauto.client.screen.EncoderScreen;

public class EncoderGuiHandler implements IGuiContainerHandler<EncoderScreen> {

	@Override
	public Collection<IGuiClickableArea> getGuiClickableAreas(EncoderScreen containerScreen, double mouseX, double mouseY) {
		Rect2i area = new Rect2i(172, 129, 22, 16);
		IGuiClickableArea clickableArea = new IGuiClickableArea() {
			@Override
			public Rect2i getArea() {
				return area;
			}

			@Override
			public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
				List<ResourceLocation> categories = containerScreen.menu.patternItemHandler.recipeType.getJEICategories();
				if(categories.isEmpty()) {
					categories = PackagedAutoJEIPlugin.getAllRecipeCategories();
				}
				List<ResourceLocation> categories2 = categories;
				List<RecipeType<?>> types = PackagedAutoJEIPlugin.jeiRuntime.getRecipeManager().createRecipeCategoryLookup().get().
						<RecipeType<?>>map(c->c.getRecipeType()).filter(t->categories2.contains(t.getUid())).toList();
				if(!types.isEmpty()) {
					recipesGui.showTypes(types);
				}
			}
		};
		return Collections.singleton(clickableArea);
	}
}
