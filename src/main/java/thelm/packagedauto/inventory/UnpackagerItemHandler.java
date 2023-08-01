package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;

public class UnpackagerItemHandler extends BaseItemHandler<UnpackagerBlockEntity> {

	public UnpackagerItemHandler(UnpackagerBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot == 9) {
			updateRecipeList();
		}
		else if(slot != 10) {
			clearRejectedIndexes();
		}
		super.onContentsChanged(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return switch(slot) {
		case 9 -> stack.getItem() instanceof IPackageRecipeListItem;
		case 10 -> stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
		default -> stack.getItem() instanceof IPackageItem;
		};
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		updateRecipeList();
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new UnpackagerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.getEnergyStorage().getEnergyStored();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 1;
	}

	public void updateRecipeList() {
		blockEntity.recipeList.clear();
		ItemStack listStack = getStackInSlot(9);
		if(listStack.getItem() instanceof IPackageRecipeListItem listItem) {
			blockEntity.recipeList.addAll(listItem.getRecipeList(blockEntity.getLevel(), listStack).getRecipeList());
		}
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
			blockEntity.postPatternChange();
		}
	}

	public void clearRejectedIndexes() {
		for(PackageTracker tracker : blockEntity.trackers) {
			tracker.clearRejectedIndexes();
		}
	}
}
