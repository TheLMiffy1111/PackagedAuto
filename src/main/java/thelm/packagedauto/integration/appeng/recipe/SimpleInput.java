package thelm.packagedauto.integration.appeng.recipe;

import com.mojang.logging.LogUtils;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class SimpleInput implements IInput {

	private final IPackageRecipeInfo recipe;
	private final GenericStack[] template;
	private final long multiplier;

	public SimpleInput(IPackageRecipeInfo recipe, GenericStack stack, HolderLookup.Provider registries) {
		this.recipe = recipe;
		template = new GenericStack[] {getGenericInput(stack, registries)};
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

	private GenericStack getGenericInput(GenericStack stack, HolderLookup.Provider registries) {
		if(stack.what() instanceof AEItemKey itemKey && itemKey.toStack().has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
			IVolumeStackWrapper vStack = itemKey.toStack().get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
			if(!vStack.isEmpty() && vStack.getVolumeType().supportsAE()) {
				try(ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(()->"ae_key_convert", LogUtils.getLogger())) {
					TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
					vStack.saveAEKey(output);
					ValueInput input = TagValueInput.create(reporter, registries, output.buildResult());
					AEKey key = AEKey.fromTagGeneric(input);
					if(key != null) {
						return new GenericStack(key, vStack.getAmount());
					}
				}
			}
		}
		return new GenericStack(stack.what(), 1);
	}
}
