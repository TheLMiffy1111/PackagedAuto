package thelm.packagedauto.inventory;

import java.util.Arrays;
import java.util.stream.IntStream;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class UnpackagerItemResourceHandlerWrapper extends SidedItemResourceHandlerWrapper<UnpackagerItemResourceHandler> {

	public static final int[] INDICES = IntStream.rangeClosed(0, 8).toArray();

	public UnpackagerItemResourceHandlerWrapper(UnpackagerItemResourceHandler wrapped, Direction direction) {
		super(wrapped, direction);
	}

	@Override
	public int[] getIndicesForDirection(Direction direction) {
		return INDICES;
	}

	@Override
	public boolean canInsertItem(int index, ItemResource resource, Direction direction) {
		return true;
	}

	@Override
	public boolean canExtractItem(int index, ItemResource resource, Direction direction) {
		return direction == Direction.UP && !Arrays.stream(wrapped.blockEntity.trackers).anyMatch(t->t.isEmpty());
	}
}
