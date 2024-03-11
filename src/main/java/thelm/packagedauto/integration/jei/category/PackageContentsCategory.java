package thelm.packagedauto.integration.jei.category;

import java.util.List;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.PackageItem;

public class PackageContentsCategory implements IRecipeCategory<IPackagePattern> {

	public static final ResourceLocation UID = new ResourceLocation("packagedauto:package_contents");
	public static final ITextComponent TITLE = new TranslationTextComponent("jei.category.packagedauto.package_contents");

	private final IDrawable background;
	private final IDrawable icon;

	public PackageContentsCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 144, 172, 104, 54);
		icon = guiHelper.createDrawableIngredient(new ItemStack(PackageItem.INSTANCE));
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends IPackagePattern> getRecipeClass() {
		return IPackagePattern.class;
	}

	@Override
	public ITextComponent getTitleAsTextComponent() {
		return TITLE;
	}

	@Override
	public String getTitle() {
		return TITLE.getString();
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
	public void setIngredients(IPackagePattern recipe, IIngredients ingredients) {}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IPackagePattern recipe, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		List<ItemStack> inputs = recipe.getInputs();
		stacks.init(0, true, 0, 18);
		stacks.set(0, recipe.getOutput());
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
