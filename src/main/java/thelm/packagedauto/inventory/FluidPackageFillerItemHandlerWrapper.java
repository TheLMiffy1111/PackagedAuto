package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class FluidPackageFillerItemHandlerWrapper extends SidedItemResourceHandlerWrapper<FluidPackageFillerItemHandler> {

	public static final int[] INDICES = {1};

	public FluidPackageFillerItemHandlerWrapper(FluidPackageFillerItemHandler wrapped, Direction direction) {
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
		return index == 1;
	}
}
