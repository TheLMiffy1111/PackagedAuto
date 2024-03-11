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
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.ItemPackage;

public class PackageContentsCategory implements IRecipeCategory<PackageContentsWrapper> {

	public static final String UID = "packagedauto:package_contents";

	private final IDrawable background;
	private final IDrawable icon;

	public PackageContentsCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 144, 172, 104, 54);
		icon = guiHelper.createDrawableIngredient(new ItemStack(ItemPackage.INSTANCE));
	}

	@Override
	public String getUid() {
		return UID;
	}

	@Override
	public String getTitle() {
		return I18n.translateToLocal("jei.category.packagedauto.package_contents");
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
	public void setRecipe(IRecipeLayout recipeLayout, PackageContentsWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		List<ItemStack> inputs = recipeWrapper.pattern.getInputs();
		stacks.init(0, true, 0, 18);
		stacks.set(0, recipeWrapper.pattern.getOutput());
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				int slot = 1+index;
				stacks.init(slot, false, 50+j*18, i*18);
				if(index < inputs.size()) {
					stacks.set(slot, inputs.get(index));
				}
			}
		}
	}
}
