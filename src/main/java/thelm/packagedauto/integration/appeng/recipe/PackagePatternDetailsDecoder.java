package thelm.packagedauto.integration.appeng.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.util.MiscHelper;

public class PackagePatternDetailsDecoder implements IPatternDetailsDecoder {

	public static final PackagePatternDetailsDecoder INSTANCE = new PackagePatternDetailsDecoder();

	protected PackagePatternDetailsDecoder() {}

	@Override
	public boolean isEncodedPattern(ItemStack stack) {
		return MiscHelper.INSTANCE.isPackage(stack) && stack.has(PackagedAutoDataComponents.PATTERN_TYPE);
	}

	@Override
	public IPatternDetails decodePattern(AEItemKey what, Level level) {
		return decodePattern(what.toStack(), level);
	}

	@Override
	public IPatternDetails decodePattern(ItemStack what, Level level) {
		if(isEncodedPattern(what)) {
			switch(what.get(PackagedAutoDataComponents.PATTERN_TYPE)) {
			case PACKAGE -> {
				IPackageRecipeInfo recipe = what.get(PackagedAutoDataComponents.RECIPE);
				int index = what.get(PackagedAutoDataComponents.PACKAGE_INDEX);
				if(recipe.isValid() && recipe.validPatternIndex(index)) {
					return new PackageCraftingPatternDetails(recipe.getPatterns().get(index), level.registryAccess());
				}
			}
			case RECIPE -> {
				IPackageRecipeInfo recipe = what.get(PackagedAutoDataComponents.RECIPE);
				if(recipe.isValid()) {
					return new RecipeCraftingPatternDetails(recipe, level.registryAccess());
				}
			}
			case DIRECT -> {
				IPackageRecipeInfo recipe = what.get(PackagedAutoDataComponents.RECIPE);
				if(recipe.isValid()) {
					return new DirectCraftingPatternDetails(recipe, level.registryAccess());
				}
			}
			}
		}
		return null;
	}
}
