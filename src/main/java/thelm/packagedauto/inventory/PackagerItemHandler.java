package thelm.packagedauto.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.util.MiscHelper;

public class PackagerItemHandler extends BaseItemHandler<PackagerBlockEntity> {

	public PackagerItemHandler(PackagerBlockEntity blockEntity) {
		super(blockEntity, 12);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot < 9 && !blockEntity.getLevel().isClientSide) {
			if(blockEntity.isWorking && !getStackInSlot(slot).isEmpty() && !blockEntity.isInputValid()) {
				blockEntity.endProcess();
			}
		}
		if(slot == 10) {
			updatePatternList();
		}
		super.onContentsChanged(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return switch(slot) {
		case 9 -> false;
		case 10 -> stack.getItem() instanceof IPackageRecipeListItem || stack.getItem() instanceof IPackageItem;
		case 11 -> stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
		default -> blockEntity.isWorking ? !getStackInSlot(slot).isEmpty() : true;
		};
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.remainingProgress;
		case 1 -> blockEntity.isWorking ? 1 : 0;
		case 2 -> blockEntity.mode.ordinal();
		case 3 -> blockEntity.getEnergyStorage().getEnergyStored();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0 -> blockEntity.remainingProgress = value;
		case 1 -> blockEntity.isWorking = value != 0;
		case 2 -> blockEntity.mode = PackagerBlockEntity.Mode.values()[value];
		case 3 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		updatePatternList();
	}

	public void updatePatternList() {
		blockEntity.patternList.clear();
		ItemStack listStack = getStackInSlot(10);
		if(listStack.getItem() instanceof IPackageRecipeListItem listItem) {
			listItem.getRecipeList(blockEntity.getLevel(), listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(blockEntity.patternList::add));
		}
		else if(listStack.getItem() instanceof IPackageItem packageItem) {
			IPackageRecipeInfo recipe = packageItem.getRecipeInfo(listStack);
			if(recipe != null) {
				blockEntity.patternList.add(recipe.getPatterns().get(packageItem.getIndex(listStack)));
			}
		}
		blockEntity.disjoint = switch(blockEntity.mode) {
		case EXACT -> false;
		case DISJOINT -> MiscHelper.INSTANCE.arePatternsDisjoint(blockEntity.patternList);
		case FIRST -> true;
		};
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
			blockEntity.postPatternChange();
		}
		if(blockEntity.getLevel() != null) {
			BlockPos.betweenClosedStream(blockEntity.getBlockPos().offset(-1, -1, -1), blockEntity.getBlockPos().offset(1, 1, 1)).
			map(blockEntity.getLevel()::getBlockEntity).filter(t->t instanceof PackagerExtensionBlockEntity).
			map(t->(PackagerExtensionBlockEntity)t).forEach(t->t.updatePatternList());
		}
	}
}
