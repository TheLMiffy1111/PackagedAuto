package thelm.packagedauto.integration.jei;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import thelm.packagedauto.client.screen.EncoderScreen;

public class EncoderGuiHandler implements IGuiContainerHandler<EncoderScreen> {

	@Override
	public Collection<IGuiClickableArea> getGuiClickableAreas(EncoderScreen containerScreen, double mouseX, double mouseY) {
		Rect2i area = new Rect2i(172, 129, 22, 16);
		List<Identifier> categories = containerScreen.menu.patternItemHandler.recipeType.getJEICategories();
		List<IRecipeType<?>> types = PackagedAutoJEIPlugin.jeiRuntime.getRecipeManager().createRecipeCategoryLookup().get().
				<IRecipeType<?>>map(c->c.getRecipeType()).filter(t->categories.contains(t.getUid())).toList();
		if(!types.isEmpty()) {
			return List.of(new IGuiClickableArea() {
				@Override
				public Rect2i getArea() {
					return area;
				}

				@Override
				public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
					recipesGui.showTypes(types);
				}
			});
		}
		return List.of();
	}
}
