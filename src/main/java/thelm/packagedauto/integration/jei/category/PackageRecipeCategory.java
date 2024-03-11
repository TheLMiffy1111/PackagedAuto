package thelm.packagedauto.integration.jei.category;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

public class PackageRecipeCategory implements IRecipeCategory<IPackageRecipeInfo> {

	public static final ResourceLocation UID = new ResourceLocation("packagedauto:package_recipe");
	public static final ITextComponent TITLE = new TranslationTextComponent("jei.category.packagedauto.package_recipe");

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;

	public PackageRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 0, 0, 162, 208);
		icon = guiHelper.createDrawableIngredient(new ItemStack(EncoderBlock.INSTANCE));
		slotBackground = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 162, 118, 16, 16);
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
	public void setIngredients(IPackageRecipeInfo recipe, IIngredients ingredients) {}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IPackageRecipeInfo recipe, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		IPackageRecipeType recipeType = recipe.getRecipeType();
		Int2ObjectMap<ItemStack> map = recipe.getEncoderStacks();
		List<ItemStack> outputs = recipe.getOutputs();
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

	@Override
	public void draw(IPackageRecipeInfo recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		FontRenderer font = Minecraft.getInstance().font;
		String s = recipe.getRecipeType().getDisplayName().getString();
		font.draw(matrixStack, s, background.getWidth()/2 - font.width(s)/2, 0, 0x404040);
	}

	class ColoredSlot implements IDrawable {

		private Vector3i color;

		public ColoredSlot(Vector3i color) {
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
		public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
			RenderSystem.color4f(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
			slotBackground.draw(matrixStack, xOffset+1, yOffset+1);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}
	}
}
