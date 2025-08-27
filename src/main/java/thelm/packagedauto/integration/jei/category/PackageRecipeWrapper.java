package thelm.packagedauto.integration.jei.category;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;

public class PackageRecipeWrapper implements IRecipeWrapper {

	public final IRecipeInfo recipe;

	public PackageRecipeWrapper(IRecipeInfo recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		Int2ObjectMap<ItemStack> map = recipe.getEncoderStacks();
		map.defaultReturnValue(ItemStack.EMPTY);
		ingredients.setInputs(VanillaTypes.ITEM, IntStream.range(0, 81).mapToObj(map::get).collect(Collectors.toList()));
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		FontRenderer font = minecraft.fontRenderer;
		String s = recipe.getRecipeType().getLocalizedName();
		font.drawString(s, recipeWidth/2 - font.getStringWidth(s)/2, 0, 0x404040);
	}
}
