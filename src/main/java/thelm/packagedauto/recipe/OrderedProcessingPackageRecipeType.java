package thelm.packagedauto.recipe;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import thelm.packagedauto.api.IPackageRecipeInfo;

public class OrderedProcessingPackageRecipeType extends ProcessingPackageRecipeType {

	public static final OrderedProcessingPackageRecipeType INSTANCE = new OrderedProcessingPackageRecipeType();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:ordered_processing");

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public MutableComponent getDisplayName() {
		return new TranslatableComponent("recipe.packagedauto.ordered_processing");
	}

	@Override
	public MutableComponent getShortDisplayName() {
		return new TranslatableComponent("recipe.packagedauto.ordered_processing.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new OrderedProcessingPackageRecipeInfo();
	}
}
