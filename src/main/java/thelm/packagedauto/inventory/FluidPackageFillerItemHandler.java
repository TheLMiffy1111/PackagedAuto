package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;

public class FluidPackageFillerItemHandler extends BaseItemHandler<FluidPackageFillerBlockEntity> {

	public FluidPackageFillerItemHandler(FluidPackageFillerBlockEntity blockEntity) {
		super(blockEntity, 3);
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) {
		switch(index) {
		case 1: return false;
		case 2: return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
		default: return blockEntity.isWorking ? !getStackInSlot(index).isEmpty() : true;
		}
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new FluidPackageFillerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		switch(id) {
		case 0: return blockEntity.remainingProgress;
		case 1: return blockEntity.isWorking ? 1 : 0;
		default: return 0;
		}
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0:
			blockEntity.remainingProgress = value;
			break;
		case 1:
			blockEntity.isWorking = value != 0;
			break;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}
}
