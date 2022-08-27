package thelm.packagedauto.integration.appeng.recipe;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;

public class SimpleInput implements IInput {

	private final IPackageRecipeInfo recipe;
	private final GenericStack[] template;
	private final long multiplier;

	public SimpleInput(IPackageRecipeInfo recipe, GenericStack stack) {
		this.recipe = recipe;
		template = new GenericStack[] {getGenericInput(stack)};
		multiplier = stack.amount();
	}

	@Override
	public GenericStack[] getPossibleInputs() {
		return template;
	}

	@Override
	public long getMultiplier() {
		return multiplier;
	}

	@Override
	public boolean isValid(AEKey input, Level level) {
		return input.matches(template[0]);
	}

	@Override
	public AEKey getRemainingKey(AEKey template) {
		if(recipe != null && recipe.getRecipeType().hasCraftingRemainingItem() && template instanceof AEItemKey itemTemplate) {
			return AEItemKey.of(recipe.getCraftingRemainingItem(itemTemplate.toStack()));
		}
		return null;
	}

	private GenericStack getGenericInput(GenericStack stack) {
		if(stack.what() instanceof AEItemKey itemKey && itemKey.getItem() instanceof IVolumePackageItem vPackage) {
			IVolumeStackWrapper vStack = vPackage.getVolumeStack(itemKey.toStack());
			if(!vStack.isEmpty() && vStack.getVolumeType().supportsAE()) {
				AEKey key = AEKey.fromTagGeneric(vStack.saveAEKey(new CompoundTag()));
				if(key != null) {
					return new GenericStack(key, vStack.getAmount());
				}
			}
		}
		return new GenericStack(stack.what(), 1);
	}
}
