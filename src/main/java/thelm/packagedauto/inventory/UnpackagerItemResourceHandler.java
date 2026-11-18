package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.util.MiscHelper;

public class UnpackagerItemResourceHandler extends BaseItemResourceHandler<UnpackagerBlockEntity> {

	public UnpackagerItemResourceHandler(UnpackagerBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		if(index == 9) {
			updateRecipeList();
		}
		else if(index != 10) {
			clearRejectedIndexes();
		}
		super.onContentsChanged(index, previousContents);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		if(index == 9) {
			return 1;
		}
		return super.getCapacity(index, resource);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return switch(index) {
		case 9 -> resource.has(PackagedAutoDataComponents.RECIPE_LIST);
		case 10 -> ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Energy.ITEM) != null;
		default -> MiscHelper.INSTANCE.isPackage(resource);
		};
	}

	@Override
	public void deserialize(ValueInput input) {
		super.deserialize(input);
		updateRecipeList();
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new UnpackagerItemResourceHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		if(id < 10) {
			return blockEntity.trackers[id].getSyncValue();
		}
		return switch(id) {
		case 10 -> blockEntity.blocking ? 1 : 0;
		case 11 -> blockEntity.trackerCount;
		case 12 -> blockEntity.getEnergyStorage().getAmountAsInt();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		if(id < 10) {
			blockEntity.trackers[id].setSyncValue(value);
		}
		switch(id) {
		case 10 -> blockEntity.blocking = value != 0;
		case 11 -> blockEntity.trackerCount = value;
		case 12 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 13;
	}

	public void updateRecipeList() {
		blockEntity.recipeList.clear();
		ItemStack listStack = getStack(9);
		if(listStack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			blockEntity.recipeList.addAll(listStack.get(PackagedAutoDataComponents.RECIPE_LIST));
		}
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
			blockEntity.postPatternChange();
		}
	}

	public void clearRejectedIndexes() {
		for(PackageTracker tracker : blockEntity.trackers) {
			tracker.clearRejectedIndexes();
		}
	}
}
