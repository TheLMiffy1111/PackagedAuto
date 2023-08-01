package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.block.entity.CrafterBlockEntity;

public class CrafterItemHandler extends BaseItemHandler<CrafterBlockEntity> {

	public CrafterItemHandler(CrafterBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) {
		if(index == 10) {
			return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
		}
		return false;
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new CrafterItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.remainingProgress;
		case 1 -> blockEntity.isWorking ? 1 : 0;
		case 2 -> blockEntity.getEnergyStorage().getEnergyStored();
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
