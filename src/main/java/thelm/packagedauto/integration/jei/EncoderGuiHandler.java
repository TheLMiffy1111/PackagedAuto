package thelm.packagedauto.integration.jei;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.client.screen.EncoderScreen;

public class EncoderGuiHandler implements IGuiContainerHandler<EncoderScreen> {

	@Override
	public Collection<IGuiClickableArea> getGuiClickableAreas(EncoderScreen containerScreen, double mouseX, double mouseY) {
		Rectangle2d area = new Rectangle2d(172, 129, 22, 16);
		IGuiClickableArea clickableArea = new IGuiClickableArea() {
			@Override
			public Rectangle2d getArea() {
				return area;
			}

			@Override
			public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
				List<ResourceLocation> categories = containerScreen.container.patternItemHandler.recipeType.getJEICategories();
				if(categories.isEmpty()) {
					categories = PackagedAutoJEIPlugin.getAllRecipeCategories();
				}
				recipesGui.showCategories(categories);
			}
		};
		return Collections.singleton(clickableArea);
	}

}
