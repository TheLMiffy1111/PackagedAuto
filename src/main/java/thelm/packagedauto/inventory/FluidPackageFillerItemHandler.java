package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;

public class FluidPackageFillerItemHandler extends BaseItemResourceHandler<FluidPackageFillerBlockEntity> {

	public FluidPackageFillerItemHandler(FluidPackageFillerBlockEntity blockEntity) {
		super(blockEntity, 3);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return switch(index) {
		case 1 -> false;
		case 2 -> ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Energy.ITEM) != null;
		default -> ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Fluid.ITEM) != null;
		};
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new FluidPackageFillerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.requiredAmount;
		case 1 -> blockEntity.amount;
		case 2 -> blockEntity.remainingProgress;
		case 3 -> blockEntity.isWorking ? 1 : 0;
		case 4 -> blockEntity.getEnergyStorage().getAmountAsInt();
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
