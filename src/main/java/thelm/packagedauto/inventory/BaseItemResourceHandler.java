package thelm.packagedauto.inventory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.util.MiscHelper;

public class BaseItemResourceHandler<T extends BaseBlockEntity> extends StacksResourceHandler<ItemStack, ItemResource> implements IndexModifier<ItemResource>, ContainerData {

	public final T blockEntity;
	protected Map<Direction, ResourceHandler<ItemResource>> wrapperMap = new IdentityHashMap<>(7);

	public BaseItemResourceHandler(T blockEntity, int size) {
		super(size, ItemStack.EMPTY, MiscHelper.LARGE_ITEM_OPTIONAL_CODEC);
		this.blockEntity = blockEntity;
	}

	public List<ItemStack> getStacks() {
		return Collections.unmodifiableList(stacks);
	}

	public ItemStack getStack(int index) {
		Objects.checkIndex(index, size());
		return stacks.get(index);
	}

	public void setStack(int index, ItemStack stack) {
		Objects.checkIndex(index, size());
		stacks.set(index, stack);
	}

	@Override
	public ItemResource getResourceFrom(ItemStack stack) {
		return ItemResource.of(stack);
	}

	@Override
	public int getAmountFrom(ItemStack stack) {
		return stack.getCount();
	}

	@Override
	protected ItemStack getStackFrom(ItemResource resource, int amount) {
		return resource.toStack(amount);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
	}

	@Override
	protected ItemStack copyOf(ItemStack stack) {
		return stack.copy();
	}

	@Override
	public boolean matches(ItemStack stack, ItemResource resource) {
		return resource.matches(stack);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		if(blockEntity != null) {
			blockEntity.setChanged();
		}
	}

	public void setChanged() {
		if(blockEntity != null) {
			blockEntity.setChanged();
		}
	}

	public void sync(boolean rerender) {
		if(blockEntity != null) {
			blockEntity.sync(rerender);
		}
	}

	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return this;
	}

	@Override
	public int get(int index) {
		return 0;
	}

	@Override
	public void set(int index, int value) {}

	@Override
	public int getCount() {
		return 0;
	}
}
