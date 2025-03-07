package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;

public class PackagingProviderItemHandler extends BaseItemHandler<PackagingProviderBlockEntity> {

	public PackagingProviderItemHandler(PackagingProviderBlockEntity blockEntity) {
		super(blockEntity, 1);
	}

	@Override
	protected void onContentsChanged(int slot) {
		updateRecipeList();
		super.onContentsChanged(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() instanceof IPackageRecipeListItem;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		updateRecipeList();
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.blocking ? 1 : 0;
		case 1 -> blockEntity.provideDirect ? 1 : 0;
		case 2 -> blockEntity.providePackaging ? 1 : 0;
		case 3 -> blockEntity.provideUnpackaging ? 1 : 0;
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0 -> blockEntity.blocking = value != 0;
		case 1 -> blockEntity.provideDirect = value != 0;
		case 2 -> blockEntity.providePackaging = value != 0;
		case 3 -> blockEntity.provideUnpackaging = value != 0;
		}
	}

	@Override
	public int getCount() {
		return 4;
	}

	public void updateRecipeList() {
		blockEntity.recipeList.clear();
		ItemStack listStack = getStackInSlot(0);
		if(listStack.getItem() instanceof IPackageRecipeListItem listItem) {
			blockEntity.recipeList.addAll(listItem.getRecipeList(listStack).getRecipeList());
		}
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
			blockEntity.postPatternChange();
		}
	}
}
