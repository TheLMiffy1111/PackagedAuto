package thelm.packagedauto.integration.jei.category;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.PackagedAutoItems;

public class PackageRecipeCategory implements IRecipeCategory<IPackageRecipeInfo> {

	public static final IRecipeType<IPackageRecipeInfo> TYPE = IRecipeType.create("packagedauto", "package_recipe", IPackageRecipeInfo.class);
	public static final Component TITLE = Component.translatable("jei.category.packagedauto.package_recipe");

	private final IDrawable background;
	private final IDrawable icon;

	public PackageRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 0, 0, 162, 226);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PackagedAutoItems.ENCODER.toStack());
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
		return 162;
	}

	@Override
	public int getHeight() {
		return 226;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IPackageRecipeInfo recipe, IFocusGroup focuses) {
		IPackageRecipeType recipeType = recipe.getRecipeType();
		Int2ObjectMap<ItemStack> map = recipe.getEncoderStacks();
		List<ItemStack> outputs = recipe.getOutputs();
		List<IPackagePattern> patterns = recipe.getPatterns();
		IRecipeSlotBuilder slot;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				int index = i*9+j;
				slot = builder.addSlot(RecipeIngredientRole.INPUT, 1+j*18, 11+i*18);
				slot.setBackground(new ColoredSlot(recipeType.getSlotColor(index)), 0, 0);
				if(map.containsKey(index)) {
					slot.add(map.get(index));
				}
			}
		}
		for(int i = 0; i < 9; ++i) {
			slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 1+i*18, 191);
			if(i < outputs.size()) {
				slot.add(outputs.get(i));
			}
		}
		for(int i = 0; i < 9; ++i) {
			slot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1+i*18, 209);
			if(i < patterns.size()) {
				slot.add(patterns.get(i).getOutput());
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

	class ColoredSlot implements IDrawable {

		private Vec3i color;

		public ColoredSlot(Vec3i color) {
			this.color = color;
		}

		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
			guiGraphics.fill(xOffset, yOffset, xOffset+16, yOffset+16, ARGB.color(color.getX(), color.getY(), color.getZ()));
		}
	}
}
