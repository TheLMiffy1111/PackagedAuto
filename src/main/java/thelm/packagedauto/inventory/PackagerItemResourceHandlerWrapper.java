package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class PackagerItemResourceHandlerWrapper extends SidedItemResourceHandlerWrapper<PackagerItemResourceHandler> {

	public static final int[] INDICES = IntStream.rangeClosed(0, 9).toArray();

	public PackagerItemResourceHandlerWrapper(PackagerItemResourceHandler wrapped, Direction direction) {
		super(wrapped, direction);
	}

	@Override
	public int[] getIndicesForDirection(Direction direction) {
		return INDICES;
	}

	@Override
	public boolean canInsertItem(int index, ItemResource resource, Direction direction) {
		return index < 9;
	}

	@Override
	public boolean canExtractItem(int index, ItemResource resource, Direction direction) {
		return index == 9 || direction == Direction.UP;
	}
}
