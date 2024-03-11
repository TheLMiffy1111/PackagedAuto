package thelm.packagedauto.integration.jei.category;

import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.block.BlockPackager;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

public class PackagingCategory implements IRecipeCategory<PackagingWrapper> {

	public static final String UID = "packagedauto:packaging";

	private final IDrawable background;
	private final IDrawable icon;

	public PackagingCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 108, 64, 112, 54);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockPackager.INSTANCE));
	}

	@Override
	public String getUid() {
		return UID;
	}

	@Override
	public String getTitle() {
		return I18n.translateToLocal("jei.category.packagedauto.packaging");
	}

	@Override
	public String getModName() {
		return "PackagedAuto";
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, PackagingWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		List<ItemStack> inputs = recipeWrapper.pattern.getInputs();
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				stacks.init(index, true, j*18, i*18);
				if(index < inputs.size()) {
					stacks.set(index, inputs.get(index));
				}
			}
		}
		stacks.init(9, false, 90, 18);
		stacks.set(9, recipeWrapper.pattern.getOutput());
	}
}
