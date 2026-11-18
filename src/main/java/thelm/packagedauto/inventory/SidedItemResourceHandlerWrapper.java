package thelm.packagedauto.inventory;

import java.util.Objects;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class SidedItemResourceHandlerWrapper<H extends ResourceHandler<ItemResource>> implements ResourceHandler<ItemResource> {

	protected H wrapped;
	protected Direction direction;

	public SidedItemResourceHandlerWrapper(H wrapped, Direction direction) {
		this.wrapped = wrapped;
		this.direction = direction;
	}

	@Override
	public int size() {
		return getIndicesForDirection(direction).length;
	}

	@Override
	public ItemResource getResource(int index) {
		int[] map = getIndicesForDirection(direction);
		Objects.checkIndex(index, map.length);
		return wrapped.getResource(map[index]);
	}

	@Override
	public long getAmountAsLong(int index) {
		int[] map = getIndicesForDirection(direction);
		Objects.checkIndex(index, map.length);
		return wrapped.getAmountAsLong(map[index]);
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		int[] map = getIndicesForDirection(direction);
		Objects.checkIndex(index, map.length);
		return wrapped.getCapacityAsLong(map[index], resource);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		int[] map = getIndicesForDirection(direction);
		Objects.checkIndex(index, map.length);
		return wrapped.isValid(map[index], resource);
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		int[] map = getIndicesForDirection(direction);
		Objects.checkIndex(index, map.length);
		int i = map[index];
		return !canInsertItem(i, resource, direction) ? 0 : wrapped.insert(i, resource, amount, transaction);
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		int[] map = getIndicesForDirection(direction);
		Objects.checkIndex(index, map.length);
		int i = map[index];
		return !canExtractItem(i, resource, direction) ? 0 : wrapped.extract(i, resource, amount, transaction);
	}

	public abstract int[] getIndicesForDirection(Direction direction);

	public abstract boolean canInsertItem(int index, ItemResource resource, Direction direction);

	public abstract boolean canExtractItem(int index, ItemResource resource, Direction direction);
}
