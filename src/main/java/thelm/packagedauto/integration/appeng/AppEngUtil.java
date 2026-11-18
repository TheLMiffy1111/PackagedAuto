package thelm.packagedauto.integration.appeng;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Functions;
import com.mojang.logging.LogUtils;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.appeng.recipe.SimpleInput;

public class AppEngUtil {

	private static final Comparator<GenericStack> COMPARE_BY_STACKSIZE = (s1, s2)->Long.compare(s1.amount(), s2.amount());

	private AppEngUtil() {}

	public static IInWorldGridNodeHost getAsInWorldGridNodeHost(BlockEntity blockEntity) {
		if(blockEntity instanceof IInWorldGridNodeHost inWorldGridNodeHost) {
			return inWorldGridNodeHost;
		}
		return null;
	}

	public static List<GenericStack> condenseStacks(List<GenericStack> stacks) {
		List<GenericStack> merged = stacks.stream().filter(Objects::nonNull).
				collect(Collectors.toMap(GenericStack::what, Functions.identity(), GenericStack::sum, LinkedHashMap::new)).
				values().stream().toList();
		if(merged.size() == 0) {
			throw new IllegalStateException("No pattern here!");
		}
		return merged;
	}

	public static IInput[] toInputs(List<GenericStack> stacks, HolderLookup.Provider registries) {
		return toInputs(null, stacks, registries);
	}

	public static IInput[] toInputs(IPackageRecipeInfo recipe, List<GenericStack> stacks, HolderLookup.Provider registries) {
		IInput[] inputs = new IInput[stacks.size()];
		for(int i = 0; i < stacks.size(); ++i) {
			inputs[i] = new SimpleInput(recipe, stacks.get(i), registries);
		}
		return inputs;
	}

	public static GenericStack getGenericOutput(ItemStack stack, HolderLookup.Provider registries) {
		if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
			IVolumeStackWrapper vStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
			if(!vStack.isEmpty() && vStack.getVolumeType() != null && vStack.getVolumeType().supportsAE()) {
				try(ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(()->"ae_key_convert", LogUtils.getLogger())) {
					TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
					vStack.saveAEKey(output);
					ValueInput input = TagValueInput.create(reporter, registries, output.buildResult());
					AEKey key = AEKey.fromTagGeneric(input);
					if(key != null) {
						return new GenericStack(key, vStack.getAmount()*stack.getCount());
					}
				}
			}
		}
		return GenericStack.fromItemStack(stack);
	}

	public static boolean isPatternProvider(BlockEntity blockEntity, Direction direction) {
		return blockEntity instanceof PatternProviderLogicHost || blockEntity instanceof IPartHost partHost && partHost.getPart(direction) instanceof PatternProviderLogicHost;
	}
}
