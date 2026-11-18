package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.CrafterBlockEntity;

public class CrafterItemResourceHandler extends BaseItemResourceHandler<CrafterBlockEntity> {

	public CrafterItemResourceHandler(CrafterBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		if(index == 10) {
			return ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Energy.ITEM) != null;
		}
		return false;
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new CrafterItemResourceHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.remainingProgress;
		case 1 -> blockEntity.isWorking ? 1 : 0;
		case 2 -> blockEntity.getEnergyStorage().getAmountAsInt();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0 -> blockEntity.remainingProgress = value;
		case 1 -> blockEntity.isWorking = value != 0;
		case 2 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 3;
	}
}
