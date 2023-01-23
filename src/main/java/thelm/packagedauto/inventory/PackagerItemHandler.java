package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.tile.PackagerExtensionTile;
import thelm.packagedauto.tile.PackagerTile;
import thelm.packagedauto.util.MiscHelper;

public class PackagerItemHandler extends BaseItemHandler<PackagerTile> {

	public PackagerItemHandler(PackagerTile tile) {
		super(tile, 12);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot < 9 && !tile.getWorld().isRemote) {
			if(tile.isWorking && !getStackInSlot(slot).isEmpty() && !tile.isInputValid()) {
				tile.endProcess();
			}
		}
		if(slot == 10) {
			updatePatternList();
		}
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		switch(slot) {
		case 9: return false;
		case 10: return stack.getItem() instanceof IPackageRecipeListItem || stack.getItem() instanceof IPackageItem;
		case 11: return stack.getCapability(CapabilityEnergy.ENERGY, null).isPresent();
		default: return tile.isWorking ? !getStackInSlot(slot).isEmpty() : true;
		}
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		switch(id) {
		case 0: return tile.remainingProgress;
		case 1: return tile.isWorking ? 1 : 0;
		default: return 0;
		}
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0:
			tile.remainingProgress = value;
			break;
		case 1:
			tile.isWorking = value != 0;
			break;
		}
	}

	@Override
	public int size() {
		return 2;
	}

	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		updatePatternList();
	}

	public void updatePatternList() {
		tile.patternList.clear();
		ItemStack listStack = getStackInSlot(10);
		if(listStack.getItem() instanceof IPackageRecipeListItem) {
			((IPackageRecipeListItem)listStack.getItem()).getRecipeList(tile.getWorld(), listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(tile.patternList::add));
		}
		else if(listStack.getItem() instanceof IPackageItem) {
			IPackageItem packageItem = (IPackageItem)listStack.getItem();
			tile.patternList.add(packageItem.getRecipeInfo(listStack).getPatterns().get(packageItem.getIndex(listStack)));
		}
		if(tile.mode == PackagerTile.Mode.FIRST) {
			tile.disjoint = true;
		}
		else if(tile.mode == PackagerTile.Mode.DISJOINT) {
			tile.disjoint = MiscHelper.INSTANCE.arePatternsDisjoint(tile.patternList);
		}
		if(tile.getWorld() != null && !tile.getWorld().isRemote) {
			tile.postPatternChange();
		}
		if(tile.getWorld() != null) {
			BlockPos.getAllInBox(tile.getPos().add(-1, -1, -1), tile.getPos().add(1, 1, 1)).
			map(tile.getWorld()::getTileEntity).filter(t->t instanceof PackagerExtensionTile).
			map(t->(PackagerExtensionTile)t).forEach(t->t.updatePatternList());
		}
	}
}
