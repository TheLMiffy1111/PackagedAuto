package thelm.packagedauto.inventory;

import java.util.Arrays;
import java.util.stream.IntStream;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class UnpackagerItemHandlerWrapper extends SidedItemHandlerWrapper<UnpackagerItemHandler> {

	public static final int[] SLOTS = IntStream.rangeClosed(0, 8).toArray();

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
		return direction == Direction.UP && !Arrays.stream(itemHandler.blockEntity.trackers).anyMatch(t->t.isEmpty());
	}
}
