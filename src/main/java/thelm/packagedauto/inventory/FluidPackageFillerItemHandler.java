package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;

public class FluidPackageFillerItemHandler extends BaseItemHandler<FluidPackageFillerBlockEntity> {

	public FluidPackageFillerItemHandler(FluidPackageFillerBlockEntity blockEntity) {
		super(blockEntity, 3);
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) {
		return switch(index) {
		case 1 -> false;
		case 2 -> stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
		default -> (blockEntity.isWorking ? !getStackInSlot(index).isEmpty() : true) && FluidUtil.getFluidHandler(stack).isPresent();
		};
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new FluidPackageFillerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.requiredAmount;
		case 1 -> blockEntity.amount;
		case 2 -> blockEntity.remainingProgress;
		case 3 -> blockEntity.isWorking ? 1 : 0;
		case 4 -> blockEntity.getEnergyStorage().getEnergyStored();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0 -> blockEntity.requiredAmount = value;
		case 1 -> blockEntity.amount = value;
		case 2 -> blockEntity.remainingProgress = value;
		case 3 -> blockEntity.isWorking = value != 0;
		case 4 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 5;
	}
}
