package thelm.packagedauto.integration.jei.category;

import java.awt.Color;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.block.BlockEncoder;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

public class PackageRecipeCategory implements IRecipeCategory<PackageRecipeWrapper> {

	public static final String UID = "packagedauto:package_recipe";

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;

	public PackageRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 0, 0, 162, 208);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockEncoder.INSTANCE));
		slotBackground = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 162, 118, 16, 16);
	}

	@Override
	public String getUid() {
		return UID;
	}

	@Override
	public String getTitle() {
		return I18n.translateToLocal("jei.category.packagedauto.package_recipe");
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
	public void setRecipe(IRecipeLayout recipeLayout, PackageRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		IRecipeType recipeType = recipeWrapper.recipe.getRecipeType();
		Int2ObjectMap<ItemStack> map = recipeWrapper.recipe.getEncoderStacks();
		List<ItemStack> outputs = recipeWrapper.recipe.getOutputs();
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				int index = i*9+j;
				stacks.init(index, true, j*18, 10+i*18);
				stacks.setBackground(index, new ColoredSlot(recipeType.getSlotColor(index)));
				if(map.containsKey(index)) {
					stacks.set(index, map.get(index));
				}
			}
		}
		for(int index = 0; index < 9; ++index) {
			int slot = 81+index;
			stacks.init(slot, false, index*18, 190);
			if(index < outputs.size()) {
				stacks.set(slot, outputs.get(index));
			}
		}
	}

	class ColoredSlot implements IDrawable {

		private Color color;

		public ColoredSlot(Color color) {
			this.color = color;
		}

		@Override
		public int getWidth() {
			return 18;
		}

		@Override
		public int getHeight() {
			return 18;
		}

		@Override
		public void draw(Minecraft minecraft, int xOffset, int yOffset) {
			GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, 1);
			slotBackground.draw(minecraft, xOffset+1, yOffset+1);
			GlStateManager.color(1, 1, 1, 1);
		}
	}
}
