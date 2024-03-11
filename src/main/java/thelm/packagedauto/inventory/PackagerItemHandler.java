package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
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
		if(slot < 9 && !tile.getLevel().isClientSide) {
			if(tile.isWorking && !getStackInSlot(slot).isEmpty() && !tile.isInputValid()) {
				tile.endProcess();
			}
		}
		if(slot == 10) {
			updatePatternList();
		}
		super.onContentsChanged(slot);
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
		case 2: return tile.mode.ordinal();
		case 3: return tile.getEnergyStorage().getEnergyStored();
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
		case 2:
			tile.mode = PackagerTile.Mode.values()[value];
			break;
		case 3:
			tile.getEnergyStorage().setEnergyStored(value);
			break;
		}
	}

	@Override
	public int getCount() {
		return 4;
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
			((IPackageRecipeListItem)listStack.getItem()).getRecipeList(tile.getLevel(), listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(tile.patternList::add));
		}
		else if(listStack.getItem() instanceof IPackageItem) {
			IPackageItem packageItem = (IPackageItem)listStack.getItem();
			IPackageRecipeInfo recipe = packageItem.getRecipeInfo(listStack);
			int index = packageItem.getIndex(listStack);
			if(recipe != null && recipe.validPatternIndex(index)) {
				tile.patternList.add(recipe.getPatterns().get(index));
			}
		}
		switch(tile.mode) {
		case EXACT:
			tile.disjoint = false;
			break;
		case DISJOINT:
			tile.disjoint = MiscHelper.INSTANCE.arePatternsDisjoint(tile.patternList);
			break;
		case FIRST:
			tile.disjoint = true;
			break;
		}
		if(tile.getLevel() != null && !tile.getLevel().isClientSide) {
			tile.postPatternChange();
		}
		if(tile.getLevel() != null) {
			BlockPos.betweenClosedStream(tile.getBlockPos().offset(-1, -1, -1), tile.getBlockPos().offset(1, 1, 1)).
			map(tile.getLevel()::getBlockEntity).filter(t->t instanceof PackagerExtensionTile).
			map(t->(PackagerExtensionTile)t).forEach(t->t.updatePatternList());
		}
	}
}
