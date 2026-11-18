package thelm.packagedauto.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.util.MiscHelper;

public class FluidPackageContentsCategory implements IRecipeCategory<IFluidStackWrapper> {

	public static final IRecipeType<IFluidStackWrapper> TYPE = IRecipeType.create("packagedauto", "fluid_package_contents", IFluidStackWrapper.class);
	public static final Component TITLE = Component.translatable("jei.category.packagedauto.fluid_package_contents");

	private final IDrawable background;
	private final IDrawable icon;

	public FluidPackageContentsCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 0, 226, 76, 26);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PackagedAutoItems.VOLUME_PACKAGE.toStack());
	}

	@Override
	public IRecipeType<IFluidStackWrapper> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}

	@Override
	public int getWidth() {
		return 76;
	}

	@Override
	public int getHeight() {
		return 26;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IFluidStackWrapper recipe, IFocusGroup focuses) {
		IRecipeSlotBuilder slot;
		slot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
		slot.add(MiscHelper.INSTANCE.makeVolumePackage(recipe));
		slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 5);
		slot.setFluidRenderer((long)recipe.getAmount(), false, 16, 16);
		slot.add(NeoForgeTypes.FLUID_STACK, recipe.getFluid());
	}

	@Override
	public void draw(IFluidStackWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics);
	}
}
