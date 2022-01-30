package thelm.packagedauto.integration.appeng.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import thelm.packagedauto.api.IFluidPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class RecipeCraftingPatternDetails implements IPatternDetails {

	public final AEItemKey recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final GenericStack[] sparseInputs;
	public final GenericStack[] sparseOutputs;
	public final IInput[] inputs;
	public final GenericStack[] outputs;
	private int priority = 0;

	public RecipeCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe) {
		this.recipeHolder = AEItemKey.of(recipeHolder);
		this.recipe = recipe;
		sparseInputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(GenericStack::fromItemStack).toArray(GenericStack[]::new);
		sparseOutputs = recipe.getOutputs().stream().map(this::getItemOrFluidOutput).toArray(GenericStack[]::new);
		inputs = AppEngUtil.toInputs(sparseInputs);
		outputs = AppEngUtil.condenseStacks(sparseOutputs);
	}

	@Override
	public AEItemKey getDefinition() {
		return recipeHolder;
	}

	public GenericStack[] getSparseInputs() {
		return sparseInputs;
	}

	public GenericStack[] getSparseOutputs() {
		return sparseOutputs;
	}

	@Override
	public IInput[] getInputs() {
		return inputs;
	}

	@Override
	public GenericStack[] getOutputs() {
		return outputs;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecipeCraftingPatternDetails) {
			RecipeCraftingPatternDetails other = (RecipeCraftingPatternDetails)obj;
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return recipe.hashCode();
	}

	private GenericStack getItemOrFluidOutput(ItemStack stack) {
		if(stack.getItem() instanceof IFluidPackageItem fluidPackage) {
			FluidStack fluidStack = fluidPackage.getFluidStack(stack);
			return GenericStack.fromFluidStack(new FluidStack(fluidStack.getFluid(), fluidStack.getAmount()*stack.getCount()));
		}
		return GenericStack.fromItemStack(stack);
	}
}
