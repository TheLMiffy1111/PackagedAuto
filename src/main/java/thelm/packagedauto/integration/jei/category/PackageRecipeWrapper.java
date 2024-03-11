package thelm.packagedauto.integration.jei.category;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import thelm.packagedauto.api.IRecipeInfo;

public class PackageRecipeWrapper implements IRecipeWrapper {

	public final IRecipeInfo recipe;

	public PackageRecipeWrapper(IRecipeInfo recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		FontRenderer font = minecraft.fontRenderer;
		String s = recipe.getRecipeType().getLocalizedName();
		font.drawString(s, recipeWidth/2 - font.getStringWidth(s)/2, 0, 0x404040);
	}
}
