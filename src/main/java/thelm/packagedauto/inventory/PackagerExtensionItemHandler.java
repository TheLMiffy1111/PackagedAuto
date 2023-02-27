package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.tile.PackagerExtensionTile;

public class PackagerExtensionItemHandler extends BaseItemHandler<PackagerExtensionTile> {

	public PackagerExtensionItemHandler(PackagerExtensionTile tile) {
		super(tile, 11);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot < 9 && !tile.getLevel().isClientSide) {
			if(tile.isWorking && !getStackInSlot(slot).isEmpty() && !tile.isInputValid()) {
				tile.endProcess();
			}
		}
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) {
		switch(index) {
		case 9: return false;
		case 10: return stack.getCapability(CapabilityEnergy.ENERGY, null).isPresent();
		default: return tile.isWorking ? !getStackInSlot(index).isEmpty() : true;
		}
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerExtensionItemHandlerWrapper(this, s));
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
	public int getCount() {
		return 2;
	}
}
