package thelm.packagedauto.recipe;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IPackageRecipeInfo;

public class OrderedProcessingPackageRecipeType extends ProcessingPackageRecipeType {

	public static final OrderedProcessingPackageRecipeType INSTANCE = new OrderedProcessingPackageRecipeType();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:ordered_processing");

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public IFormattableTextComponent getDisplayName() {
		return new TranslationTextComponent("recipe.packagedauto.ordered_processing");
	}

	@Override
	public IFormattableTextComponent getShortDisplayName() {
		return new TranslationTextComponent("recipe.packagedauto.ordered_processing.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new OrderedProcessingPackageRecipeInfo();
	}

	@Override
	public boolean isOrdered() {
		return true;
	}
}
