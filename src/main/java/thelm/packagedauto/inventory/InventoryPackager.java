package thelm.packagedauto.inventory;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;
import thelm.packagedauto.util.MiscHelper;

public class InventoryPackager extends InventoryBase {

	public static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	public final TilePackager tile;

	public InventoryPackager(TilePackager tile) {
		super(tile, 12);
		this.tile = tile;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index < 9 && !tile.getWorldObj().isRemote) {
			if(tile.isWorking && getStackInSlot(index) != null) {
				if(stack == null || !stack.isItemEqual(getStackInSlot(index)) || !tile.isInputValid()) {
					tile.endProcess();
				}
			}
		}
		super.setInventorySlotContents(index, stack);
		if(index == 10) {
			updatePatternList();
		}
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		if(index < 9 && !tile.getWorldObj().isRemote) {
			if(tile.isWorking && getStackInSlot(index) != null && !tile.isInputValid()) {
				tile.endProcess();
			}
		}
		if(index == 10) {
			updatePatternList();
		}
		return stack;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		switch(index) {
		case 9: return false;
		case 10: return stack != null && stack.getItem() instanceof IPackageRecipeListItem || stack.getItem() instanceof IPackageItem;
		case 11: return stack != null && stack.getItem() instanceof IEnergyContainerItem;
		default: return tile.isWorking ? getStackInSlot(index) != null : true;
		}
	}

	@Override
	public int getField(int id) {
		switch(id) {
		case 0: return tile.remainingProgress;
		case 1: return tile.isWorking ? 1 : 0;
		default: return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch(id) {
		case 0:
			tile.remainingProgress = value;
			break;
		case 1:
			tile.isWorking = value != 0;
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 2;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int side) {
		return index < 9;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, int side) {
		return index == 9 || side == 1 && index != 10 && index != 11;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		updatePatternList();
	}

	public void updatePatternList() {
		tile.patternList.clear();
		ItemStack listStack = getStackInSlot(10);
		if(listStack != null) {
			if(listStack.getItem() instanceof IPackageRecipeListItem) {
				((IPackageRecipeListItem)listStack.getItem()).getRecipeList(listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(tile.patternList::add));
			}
			else if(listStack.getItem() instanceof IPackageItem) {
				IPackageItem packageItem = (IPackageItem)listStack.getItem();
				tile.patternList.add(packageItem.getRecipeInfo(listStack).getPatterns().get(packageItem.getIndex(listStack)));
			}
		}
		if(tile.mode == TilePackager.Mode.FIRST) {
			tile.disjoint = true;
		}
		else if(tile.mode == TilePackager.Mode.DISJOINT) {
			tile.disjoint = MiscHelper.INSTANCE.arePatternsDisjoint(tile.patternList);
		}
		if(tile.getWorldObj() != null && !tile.getWorldObj().isRemote && tile.hostHelper != null) {
			tile.hostHelper.postPatternChange();
		}
		if(tile.getWorldObj() != null) {
			for(int x = tile.xCoord-1; x <= tile.xCoord+1; ++x) {
				for(int y = tile.yCoord-1; y <= tile.yCoord+1; ++y) {
					for(int z = tile.zCoord-1; z <= tile.zCoord+1; ++z) {
						TileEntity t = tile.getWorldObj().getTileEntity(x, y, z);
						if(t instanceof TilePackagerExtension) {
							((TilePackagerExtension)t).updatePatternList();
						}
					}
				}
			}
		}
	}
}
