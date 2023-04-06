package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class PackagerExtensionItemHandlerWrapper extends SidedItemHandlerWrapper<PackagerExtensionItemHandler> {

	public static final int[] SLOTS = IntStream.rangeClosed(0, 9).toArray();

	public PackagerExtensionItemHandlerWrapper(PackagerExtensionItemHandler itemHandler, Direction direction) {
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
