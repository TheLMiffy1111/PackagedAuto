package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.helpers.CraftingPatternDetails;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class RecipeCraftingPatternDetails implements ICraftingPatternDetails {

	public final ItemStack recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final IAEItemStack[] sparseInputs;
	public final IAEItemStack[] sparseOutputs;
	public final List<IAEItemStack> inputs;
	public final List<IAEItemStack> outputs;
	private int priority = 0;

	public RecipeCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe) {
		this.recipeHolder = recipeHolder;
		this.recipe = recipe;
		IItemStorageChannel storageChannel = Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
		sparseInputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(storageChannel::createStack).toArray(i->new IAEItemStack[i]);
		sparseOutputs = recipe.getOutputs().stream().map(storageChannel::createStack).toArray(i->new IAEItemStack[i]);
		inputs = AppEngUtil.condenseStacks(sparseInputs);
		outputs = AppEngUtil.condenseStacks(sparseOutputs);
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
	public List<IAEItemStack> getInputs() {
		return inputs;
	}

	@Override
	public List<IAEItemStack> getOutputs() {
		return outputs;
	}

	@Override
	public IAEItemStack[] getSparseInputs() {
		return sparseInputs;
	}

	@Override
	public IAEItemStack[] getSparseOutputs() {
		return sparseOutputs;
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public List<IAEItemStack> getSubstituteInputs(int index) {
		throw new IllegalStateException("Not supported.");
	}

	@Override
	public ItemStack getOutput(CraftingInventory craftingInv, World world) {
		throw new IllegalStateException("Not supported.");
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public CraftingPatternDetails toAEInternal(World world) {
		ItemStack patternStack = new ItemStack(Api.instance().definitions().items().encodedPattern());
		CompoundNBT encodedValue = new CompoundNBT();
		ListNBT tagIn = new ListNBT();
		ListNBT tagOut = new ListNBT();
		for(int i = 0; i < 9; ++i) {
			ItemStack is = i < recipe.getPatterns().size() ? recipe.getPatterns().get(i).getOutput() : ItemStack.EMPTY;
			tagIn.add(createItemTag(is));
		}
		for(int i = 0; i < 3; ++i) {
			ItemStack is = i < recipe.getOutputs().size() ? recipe.getOutputs().get(i) : ItemStack.EMPTY;
			tagOut.add(createItemTag(is));
		}
		encodedValue.put("in", tagIn);
		encodedValue.put("out", tagOut);
		patternStack.setTag(encodedValue);
		IAEItemStack patternAEStack = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(patternStack);
		return new CraftingPatternDetails(patternAEStack, world);
	}

	private static INBT createItemTag(ItemStack i) {
		CompoundNBT c = new CompoundNBT();
		if(!i.isEmpty()) {
			i.write(c);
		}
		return c;
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
}
