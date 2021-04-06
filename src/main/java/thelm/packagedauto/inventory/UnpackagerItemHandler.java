package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.tile.UnpackagerTile;
import thelm.packagedauto.tile.UnpackagerTile.PackageTracker;

public class UnpackagerItemHandler extends BaseItemHandler<UnpackagerTile> {

	public UnpackagerItemHandler(UnpackagerTile tile) {
		super(tile, 11);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot == 9) {
			updateRecipeList();
		}
		else if(slot != 10) {
			clearRejectedIndexes();
		}
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		switch(slot) {
		case 9: return stack.getItem() instanceof IPackageRecipeListItem;
		case 10: return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
		default: return stack.getItem() instanceof IPackageItem;
		}
	}

	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		updateRecipeList();
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new UnpackagerItemHandlerWrapper(this, s));
	}

	public void updateRecipeList() {
		tile.recipeList.clear();
		ItemStack listStack = getStackInSlot(9);
		if(listStack.getItem() instanceof IPackageRecipeListItem) {
			tile.recipeList.addAll(((IPackageRecipeListItem)listStack.getItem()).getRecipeList(tile.getWorld(), listStack).getRecipeList());
		}
		if(tile.getWorld() != null && !tile.getWorld().isRemote) {
			tile.postPatternChange();
		}
	}

	public void clearRejectedIndexes() {
		for(PackageTracker tracker : tile.trackers) {
			tracker.clearRejectedIndexes();
		}
	}
}
