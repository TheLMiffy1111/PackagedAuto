package thelm.packagedauto.integration.appeng.recipe;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngHelper;

public class RecipeCraftingPatternDetails implements ICraftingPatternDetails {

	public final ItemStack recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final IAEItemStack[] inputs;
	public final IAEItemStack[] outputs;
	public final IAEItemStack[] condensedInputs;
	public final IAEItemStack[] condensedOutputs;

	public RecipeCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe) {
		this.recipeHolder = recipeHolder;
		this.recipe = recipe;
		IStorageHelper storageHelper = AEApi.instance().storage();
		inputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(storageHelper::createItemStack).toArray(IAEItemStack[]::new);
		outputs = recipe.getOutputs().stream().map(storageHelper::createItemStack).toArray(IAEItemStack[]::new);
		condensedInputs = AppEngHelper.INSTANCE.condenseStacks(inputs);
		condensedOutputs = AppEngHelper.INSTANCE.condenseStacks(outputs);
	}

	@Override
	public ItemStack getPattern() {
		return recipeHolder;
	}

	@Override
	public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
		throw new IllegalStateException("Not supported.");
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return outputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return condensedInputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return condensedOutputs;
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
		throw new IllegalStateException("Not supported.");
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setPriority(int priority) {}

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
}
