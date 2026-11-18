package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class PackagingProviderItemResourceHandler extends BaseItemResourceHandler<PackagingProviderBlockEntity> {

	public PackagingProviderItemResourceHandler(PackagingProviderBlockEntity blockEntity) {
		super(blockEntity, 1);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		updateRecipeList();
		super.onContentsChanged(index, previousContents);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return 1;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return resource.has(PackagedAutoDataComponents.RECIPE_LIST);
	}

	@Override
	public void deserialize(ValueInput input) {
		super.deserialize(input);
		updateRecipeList();
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return EmptyResourceHandler.instance();
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
		ItemStack listStack = getStack(0);
		if(listStack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			blockEntity.recipeList.addAll(listStack.get(PackagedAutoDataComponents.RECIPE_LIST));
		}
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
			blockEntity.postPatternChange();
		}
	}
}
