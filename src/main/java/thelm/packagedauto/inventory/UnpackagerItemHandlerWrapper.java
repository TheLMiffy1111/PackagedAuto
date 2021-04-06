package thelm.packagedauto.inventory;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class UnpackagerItemHandlerWrapper extends SidedItemHandlerWrapper<UnpackagerItemHandler> {

	public static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};

	public UnpackagerItemHandlerWrapper(UnpackagerItemHandler itemHandler, Direction direction) {
		super(itemHandler, direction);
	}

	@Override
	public int[] getSlotsForDirection(Direction direction) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, Direction direction) {
		return true;
	}

	@Override
	public boolean canExtractItem(int slot, Direction direction) {
		return direction == Direction.UP && !Arrays.stream(itemHandler.tile.trackers).anyMatch(t->t.isEmpty());
	}
}
