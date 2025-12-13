package thelm.packagedauto.integration.appeng.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.PatternType;

public class PackagePatternDetailsDecoder implements IPatternDetailsDecoder {

	public static final PackagePatternDetailsDecoder INSTANCE = new PackagePatternDetailsDecoder();

	protected PackagePatternDetailsDecoder() {}

	@Override
	public boolean isEncodedPattern(ItemStack stack) {
		return stack.getItem() instanceof IPackageItem;
	}

	@Override
	public IPatternDetails decodePattern(AEItemKey what, Level level) {
		return decodePattern(what.toStack(), level, false);
	}

	@Override
	public IPatternDetails decodePattern(ItemStack what, Level level, boolean tryRecovery) {
		if(what.getItem() instanceof IPackageItem packageItem) {
			PatternType patternType = packageItem.getPatternType(what);
			if(patternType != null) {
				switch(packageItem.getPatternType(what)) {
				case PACKAGE -> {
					IPackageRecipeInfo recipe = packageItem.getRecipeInfo(what);
					int index = packageItem.getIndex(what);
					if(recipe != null && recipe.isPackageable() && recipe.validPatternIndex(index)) {
						return new PackageCraftingPatternDetails(recipe.getPatterns().get(index));
					}
				}
				case RECIPE -> {
					IPackageRecipeInfo recipe = packageItem.getRecipeInfo(what);
					if(recipe != null && recipe.isCraftable()) {
						return new RecipeCraftingPatternDetails(recipe);
					}
				}
				case DIRECT -> {
					IPackageRecipeInfo recipe = packageItem.getRecipeInfo(what);
					if(recipe != null && recipe.isCraftable()) {
						return new DirectCraftingPatternDetails(recipe);
					}
				}
				}
			}
		}
		return null;
	}
}
