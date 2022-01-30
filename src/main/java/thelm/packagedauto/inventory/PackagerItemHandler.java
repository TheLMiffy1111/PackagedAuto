package thelm.packagedauto.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import thelm.packagedauto.api.IPackageItem;
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
			if(blockEntity.isWorking && !getStackInSlot(slot).isEmpty()) {
				if(blockEntity.isWorking && (getStackInSlot(slot).isEmpty() || !blockEntity.isInputValid())) {
					blockEntity.endProcess();
				}
			}
		}
		if(slot == 10) {
			updatePatternList();
		}
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		switch(slot) {
		case 9: return false;
		case 10: return stack.getItem() instanceof IPackageRecipeListItem || stack.getItem() instanceof IPackageItem;
		case 11: return stack.getCapability(CapabilityEnergy.ENERGY, null).isPresent();
		default: return blockEntity.isWorking ? !getStackInSlot(slot).isEmpty() : true;
		}
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		switch(id) {
		case 0: return blockEntity.remainingProgress;
		case 1: return blockEntity.isWorking ? 1 : 0;
		default: return 0;
		}
	}

	@Override
	public void set(int id, int value) {
		switch(id) {
		case 0:
			blockEntity.remainingProgress = value;
			break;
		case 1:
			blockEntity.isWorking = value != 0;
			break;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		updatePatternList();
	}

	public void updatePatternList() {
		blockEntity.patternList.clear();
		ItemStack listStack = getStackInSlot(10);
		if(listStack.getItem() instanceof IPackageRecipeListItem) {
			((IPackageRecipeListItem)listStack.getItem()).getRecipeList(blockEntity.getLevel(), listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(blockEntity.patternList::add));
		}
		else if(listStack.getItem() instanceof IPackageItem) {
			IPackageItem packageItem = (IPackageItem)listStack.getItem();
			blockEntity.patternList.add(packageItem.getRecipeInfo(listStack).getPatterns().get(packageItem.getIndex(listStack)));
		}
		if(blockEntity.forceDisjoint) {
			blockEntity.disjoint = true;
		}
		else if(blockEntity.checkDisjoint) {
			blockEntity.disjoint = MiscHelper.INSTANCE.arePatternsDisjoint(blockEntity.patternList);
		}
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
