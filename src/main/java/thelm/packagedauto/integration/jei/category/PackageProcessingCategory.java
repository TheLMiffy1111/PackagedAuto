package thelm.packagedauto.integration.jei.category;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.PackagedAutoItems;

public class PackageProcessingCategory implements IRecipeCategory<IPackageRecipeInfo> {

	public static final IRecipeType<IPackageRecipeInfo> TYPE = IRecipeType.create("packagedauto", "package_processing", IPackageRecipeInfo.class);
	public static final Component TITLE = Component.translatable("jei.category.packagedauto.package_processing");

	private final IDrawable background;
	private final IDrawable icon;

	public PackageProcessingCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 108, 0, 140, 64);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PackagedAutoItems.UNPACKAGER.toStack());
	}

	@Override
	public IRecipeType<IPackageRecipeInfo> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}

	@Override
	public int getWidth() {
		return 140;
	}

	@Override
	public int getHeight() {
		return 64;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IPackageRecipeInfo recipe, IFocusGroup focuses) {
		List<IPackagePattern> patterns = recipe.getPatterns();
		List<ItemStack> outputs = recipe.getOutputs();
		IRecipeSlotBuilder slot;
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				slot = builder.addSlot(RecipeIngredientRole.INPUT, 1+j*18, 11+i*18);
				if(index < patterns.size()) {
					slot.add(patterns.get(index).getOutput());
				}
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int index = i*3+j;
				slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 87+j*18, 11+i*18);
				if(index < outputs.size()) {
					slot.add(outputs.get(index));
				}
			}
		}
	}

	@Override
	public void draw(IPackageRecipeInfo recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics);
		Font font = Minecraft.getInstance().font;
		String s = recipe.getRecipeType().getDisplayName().getString();
		guiGraphics.text(font, s, background.getWidth()/2 - font.width(s)/2, 0, 0xFF404040, false);
	}
}
