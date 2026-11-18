package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class CrafterItemResourceHandlerWrapper extends SidedItemResourceHandlerWrapper<CrafterItemResourceHandler> {

	public static final int[] INDICES = IntStream.rangeClosed(0, 9).toArray();

	public CrafterItemResourceHandlerWrapper(CrafterItemResourceHandler wrapped, Direction direction) {
		super(wrapped, direction);
	}

	@Override
	public int[] getIndicesForDirection(Direction direction) {
		return INDICES;
	}

	@Override
	public boolean canInsertItem(int index, ItemResource resource, Direction direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemResource resource, Direction direction) {
		return wrapped.blockEntity.isWorking ? index == 9 : true;
	}
}
