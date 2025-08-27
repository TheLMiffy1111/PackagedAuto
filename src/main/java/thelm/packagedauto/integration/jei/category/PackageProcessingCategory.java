package thelm.packagedauto.integration.jei.category;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

public class PackageProcessingCategory implements IRecipeCategory<IPackageRecipeInfo> {

	public static final ResourceLocation UID = new ResourceLocation("packagedauto:package_processing");
	public static final ITextComponent TITLE = new TranslationTextComponent("jei.category.packagedauto.package_processing");

	private final IDrawable background;
	private final IDrawable icon;

	public PackageProcessingCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 108, 0, 140, 64);
		icon = guiHelper.createDrawableIngredient(new ItemStack(UnpackagerBlock.INSTANCE));
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends IPackageRecipeInfo> getRecipeClass() {
		return IPackageRecipeInfo.class;
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
	public void setIngredients(IPackageRecipeInfo recipe, IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, Lists.transform(recipe.getPatterns(), IPackagePattern::getOutput));
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IPackageRecipeInfo recipe, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				stacks.init(index, true, j*18, 10+i*18);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				int slot = 9+index;
				stacks.init(slot, false, 86+j*18, 10+i*18);
			}
		}
		stacks.set(ingredients);
	}

	@Override
	public void draw(IPackageRecipeInfo recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		FontRenderer font = Minecraft.getInstance().font;
		String s = recipe.getRecipeType().getDisplayName().getString();
		font.draw(matrixStack, s, background.getWidth()/2 - font.width(s)/2, 0, 0x404040);
	}
}
