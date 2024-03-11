package thelm.packagedauto.integration.jei.category;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.PackageItem;

public class PackageContentsCategory implements IRecipeCategory<IPackagePattern> {

	public static final RecipeType<IPackagePattern> TYPE = RecipeType.create("packagedauto", "package_contents", IPackagePattern.class);
	public static final Component TITLE = Component.translatable("jei.category.packagedauto.package_contents");

	private final IDrawable background;
	private final IDrawable icon;

	public PackageContentsCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 144, 172, 104, 54);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PackageItem.INSTANCE));
	}

	@Override
	public RecipeType<IPackagePattern> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return TITLE;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IPackagePattern recipe, IFocusGroup focuses) {
		List<ItemStack> inputs = recipe.getInputs();
		IRecipeSlotBuilder slot;
		slot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 19);
		slot.addItemStack(recipe.getOutput());
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 51+j*18, 1+i*18);
				if(index < inputs.size()) {
					slot.addItemStack(inputs.get(index));
				}
			}
		}
	}
}
