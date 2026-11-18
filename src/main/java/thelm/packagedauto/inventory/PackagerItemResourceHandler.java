package thelm.packagedauto.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.util.MiscHelper;

public class PackagerItemResourceHandler extends BaseItemResourceHandler<PackagerBlockEntity> {

	public PackagerItemResourceHandler(PackagerBlockEntity blockEntity) {
		super(blockEntity, 12);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		if(index < 9 && !blockEntity.getLevel().isClientSide()) {
			if(blockEntity.isWorking && !getResource(index).isEmpty() && !blockEntity.isInputValid()) {
				blockEntity.endProcess();
			}
		}
		if(index == 10) {
			updatePatternList();
		}
		super.onContentsChanged(index, previousContents);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		if(index == 10) {
			return 1;
		}
		return super.getCapacity(index, resource);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return switch(index) {
		case 9 -> false;
		case 10 -> resource.has(PackagedAutoDataComponents.RECIPE_LIST) || MiscHelper.INSTANCE.isPackage(resource);
		case 11 -> ItemAccess.forStack(resource.toStack()).getCapability(Capabilities.Energy.ITEM) != null;
		default -> blockEntity.isWorking ? !getResource(index).isEmpty() : true;
		};
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new PackagerItemResourceHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		return switch(id) {
		case 0 -> blockEntity.remainingProgress;
		case 1 -> blockEntity.isWorking ? 1 : 0;
		case 2 -> blockEntity.mode.ordinal();
		case 3 -> blockEntity.getEnergyStorage().getAmountAsInt();
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
	public void deserialize(ValueInput input) {
		super.deserialize(input);
		updatePatternList();
	}

	public void updatePatternList() {
		blockEntity.patternList.clear();
		ItemStack listStack = getStack(10);
		if(listStack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			listStack.get(PackagedAutoDataComponents.RECIPE_LIST).stream().
			filter(IPackageRecipeInfo::isPackageable).forEach(recipe->{
				recipe.getPatterns().forEach(blockEntity.patternList::add);
				recipe.getExtraPatterns().forEach(blockEntity.patternList::add);
			});
		}
		else if(MiscHelper.INSTANCE.isPackage(listStack)) {
			IPackageRecipeInfo recipe = listStack.get(PackagedAutoDataComponents.RECIPE);
			int index = listStack.get(PackagedAutoDataComponents.PACKAGE_INDEX);
			if(recipe.isPackageable() && recipe.validPatternIndex(index)) {
				blockEntity.patternList.add(recipe.getPatterns().get(index));
			}
		}
		blockEntity.disjoint = switch(blockEntity.mode) {
		case EXACT -> false;
		case DISJOINT -> MiscHelper.INSTANCE.arePatternsDisjoint(blockEntity.patternList);
		case FIRST -> true;
		};
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
			blockEntity.postPatternChange();
		}
		if(blockEntity.getLevel() != null) {
			BlockPos.betweenClosedStream(blockEntity.getBlockPos().offset(-1, -1, -1), blockEntity.getBlockPos().offset(1, 1, 1)).
			map(blockEntity.getLevel()::getBlockEntity).filter(t->t instanceof PackagerExtensionBlockEntity).
			map(t->(PackagerExtensionBlockEntity)t).forEach(t->t.updatePatternList());
		}
	}
}
