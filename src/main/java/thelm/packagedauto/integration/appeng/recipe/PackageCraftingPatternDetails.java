package thelm.packagedauto.integration.appeng.recipe;

import java.util.Objects;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.integration.appeng.AppEngHelper;

public class PackageCraftingPatternDetails implements ICraftingPatternDetails {

	public final ItemStack recipeHolder;
	public final IPackagePattern pattern;
	public final IAEItemStack[] inputs;
	public final IAEItemStack[] outputs;
	public final IAEItemStack[] condensedInputs;
	public final IAEItemStack[] condensedOutputs;

	public PackageCraftingPatternDetails(ItemStack recipeHolder, IPackagePattern pattern) {
		this.recipeHolder = recipeHolder;
		this.pattern = pattern;
		IStorageHelper storageHelper = AEApi.instance().storage();
		inputs = pattern.getInputs().stream().map(storageHelper::createItemStack).toArray(IAEItemStack[]::new);
		outputs = new IAEItemStack[] {storageHelper.createItemStack(pattern.getOutput())};
		condensedInputs = AppEngHelper.INSTANCE.condenseStacks(inputs);
		condensedOutputs = outputs.clone();
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
		return true;
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
		if(obj instanceof PackageCraftingPatternDetails) {
			PackageCraftingPatternDetails other = (PackageCraftingPatternDetails)obj;
			return pattern.getIndex() == other.pattern.getIndex() && pattern.getRecipeInfo().equals(other.pattern.getRecipeInfo());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pattern.getIndex(), pattern.getRecipeInfo());
	}
}
