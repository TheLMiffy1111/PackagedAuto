package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;

public class PackagerExtensionItemResourceHandler extends BaseItemResourceHandler<PackagerExtensionBlockEntity> {

	public PackagerExtensionItemResourceHandler(PackagerExtensionBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		if(index < 9 && !blockEntity.getLevel().isClientSide()) {
			if(blockEntity.isWorking && !getStack(index).isEmpty() && !blockEntity.isInputValid()) {
				blockEntity.endProcess();
			}
		}
		super.onContentsChanged(index, previousContents);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return switch(index) {
		case 9 -> false;
		case 10 -> ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Energy.ITEM) != null;
		default -> blockEntity.isWorking ? !getStack(index).isEmpty() : true;
		};
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerExtensionItemResourceHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.remainingProgress;
		case 1 -> blockEntity.isWorking ? 1 : 0;
		case 2 -> blockEntity.mode.ordinal();
		case 3 -> blockEntity.getEnergyStorage().getAmountAsInt();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0 -> blockEntity.remainingProgress = value;
		case 1 -> blockEntity.isWorking = value != 0;
		case 2 -> blockEntity.mode = PackagerBlockEntity.Mode.values()[value];
		case 3 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 4;
	}
}
