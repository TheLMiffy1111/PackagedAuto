package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class FluidPackageFillerItemHandlerWrapper extends SidedItemHandlerWrapper<FluidPackageFillerItemHandler> {

	public static final int[] SLOTS = {1};

	public FluidPackageFillerItemHandlerWrapper(FluidPackageFillerItemHandler itemHandler, Direction direction) {
		super(itemHandler, direction);
	}

	@Override
	public int[] getSlotsForDirection(Direction direction) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, Direction direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, Direction direction) {
		return slot == 1;
	}
}
