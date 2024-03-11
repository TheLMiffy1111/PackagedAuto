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
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.block.BlockUnpackager;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

public class PackageProcessingCategory implements IRecipeCategory<PackageProcessingWrapper> {

	public static final String UID = "packagedauto:package_processing";

	private final IDrawable background;
	private final IDrawable icon;

	public PackageProcessingCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 108, 0, 140, 64);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockUnpackager.INSTANCE));
	}

	@Override
	public String getUid() {
		return UID;
	}

	@Override
	public String getTitle() {
		return I18n.translateToLocal("jei.category.packagedauto.package_processing");
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
	public void setRecipe(IRecipeLayout recipeLayout, PackageProcessingWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		List<IPackagePattern> patterns = recipeWrapper.recipe.getPatterns();
		List<ItemStack> outputs = recipeWrapper.recipe.getOutputs();
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				stacks.init(index, true, j*18, 10+i*18);
				if(index < patterns.size()) {
					stacks.set(index, patterns.get(index).getOutput()); 
				}
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				int slot = 9+index;
				stacks.init(slot, false, 86+j*18, 10+i*18);
				if(index < outputs.size()) {
					stacks.set(slot, outputs.get(index)); 
				}
			}
		}
	}
}
