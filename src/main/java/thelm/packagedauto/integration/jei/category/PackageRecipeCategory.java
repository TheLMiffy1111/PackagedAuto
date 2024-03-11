package thelm.packagedauto.integration.jei.category;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

public class PackageRecipeCategory implements IRecipeCategory<IPackageRecipeInfo> {

	public static final RecipeType<IPackageRecipeInfo> TYPE = RecipeType.create("packagedauto", "package_recipe", IPackageRecipeInfo.class);
	public static final Component TITLE = new TranslatableComponent("jei.category.packagedauto.package_recipe");

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;

	public PackageRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 0, 0, 162, 226);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(EncoderBlock.INSTANCE));
		slotBackground = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 162, 118, 16, 16);
	}

	@Override
	public RecipeType<IPackageRecipeInfo> getRecipeType() {
		return TYPE;
	}

	@Override
	public ResourceLocation getUid() {
		return TYPE.getUid();
	}

	@Override
	public Class<? extends IPackageRecipeInfo> getRecipeClass() {
		return TYPE.getRecipeClass();
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
					slot.addItemStack(map.get(index));
				}
			}
		}
		for(int i = 0; i < 9; ++i) {
			slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 1+i*18, 191);
			if(i < outputs.size()) {
				slot.addItemStack(outputs.get(i));
			}
		}
		for(int i = 0; i < 9; ++i) {
			slot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1+i*18, 209);
			if(i < patterns.size()) {
				slot.addItemStack(patterns.get(i).getOutput());
			}
		}
	}

	@Override
	public void draw(IPackageRecipeInfo recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		String s = recipe.getRecipeType().getDisplayName().getString();
		font.draw(stack, s, background.getWidth()/2 - font.width(s)/2, 0, 0x404040);
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
		public void draw(PoseStack poseStack, int xOffset, int yOffset) {
			RenderSystem.setShaderColor(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
			slotBackground.draw(poseStack, xOffset, yOffset);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}
	}
}
