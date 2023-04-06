package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class CrafterItemHandlerWrapper extends SidedItemHandlerWrapper<CrafterItemHandler> {

	public static final int[] SLOTS = IntStream.rangeClosed(0, 9).toArray();

	public CrafterItemHandlerWrapper(CrafterItemHandler itemHandler, Direction direction) {
		super(itemHandler, direction);
	}

	@Override
	public int[] getSlotsForDirection(Direction direction) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, Direction direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, Direction direction) {
		return itemHandler.blockEntity.isWorking ? index == 9 : true;
	}
}
