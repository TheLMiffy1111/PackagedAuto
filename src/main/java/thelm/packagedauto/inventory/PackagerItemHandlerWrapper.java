package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class PackagerItemHandlerWrapper extends SidedItemHandlerWrapper<PackagerItemHandler> {

	public static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	public PackagerItemHandlerWrapper(PackagerItemHandler itemHandler, Direction direction) {
		super(itemHandler, direction);
	}

	@Override
	public int[] getSlotsForDirection(Direction direction) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, Direction direction) {
		return slot < 9;
	}

	@Override
	public boolean canExtractItem(int slot, Direction direction) {
		return slot == 9 || direction == Direction.UP;
	}
}
