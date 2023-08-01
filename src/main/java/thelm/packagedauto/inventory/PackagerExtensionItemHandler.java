package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;

public class PackagerExtensionItemHandler extends BaseItemHandler<PackagerExtensionBlockEntity> {

	public PackagerExtensionItemHandler(PackagerExtensionBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot < 9 && !blockEntity.getLevel().isClientSide) {
			if(blockEntity.isWorking && !getStackInSlot(slot).isEmpty() && !blockEntity.isInputValid()) {
				blockEntity.endProcess();
			}
		}
		super.onContentsChanged(slot);
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) {
		return switch(index) {
		case 9 -> false;
		case 10 -> stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
		default -> blockEntity.isWorking ? !getStackInSlot(index).isEmpty() : true;
		};
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerExtensionItemHandlerWrapper(this, s));
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
