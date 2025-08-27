package thelm.packagedauto.integration.jei.category;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;

public class PackageProcessingWrapper implements IRecipeWrapper {

	public final IRecipeInfo recipe;

	public PackageProcessingWrapper(IRecipeInfo recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, Lists.transform(recipe.getPatterns(), IPackagePattern::getOutput));
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		FontRenderer font = minecraft.fontRenderer;
		String s = recipe.getRecipeType().getLocalizedName();
		font.drawString(s, recipeWidth/2 - font.getStringWidth(s)/2, 0, 0x404040);
	}
}
