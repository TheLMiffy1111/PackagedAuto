package thelm.packagedauto.inventory;

import com.google.common.collect.Streams;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;

public class InventoryPackager extends InventoryTileBase {

	public static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	public final TilePackager tile;

	public InventoryPackager(TilePackager tile) {
		super(tile, 12);
		this.tile = tile;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index < 9 && !tile.getWorld().isRemote) {
			if(tile.isWorking && !getStackInSlot(index).isEmpty()) {
				if(stack.isEmpty() || !stack.isItemEqual(getStackInSlot(index)) || !tile.isInputValid()) {
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
		if(index < 9 && !tile.getWorld().isRemote) {
			if(tile.isWorking && !getStackInSlot(index).isEmpty()) {
				if(tile.isWorking && (getStackInSlot(index).isEmpty() || !tile.isInputValid())) {
					tile.endProcess();
				}
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
		case 10: return stack.getItem() instanceof IRecipeListItem || stack.getItem() instanceof IPackageItem;
		case 11: return stack.hasCapability(CapabilityEnergy.ENERGY, null);
		default: return tile.isWorking ? !getStackInSlot(index).isEmpty() : true;
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
	public int[] getSlotsForFace(EnumFacing side) {
		return SLOTS;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		updatePatternList();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return index < 9;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return index == 9 || direction == EnumFacing.UP && index != 10 && index != 11;
	}

	public void updatePatternList() {
		tile.patternList.clear();
		ItemStack listStack = getStackInSlot(10);
		if(listStack.getItem() instanceof IRecipeListItem) {
			((IRecipeListItem)listStack.getItem()).getRecipeList(listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(tile.patternList::add));
		}
		else if(listStack.getItem() instanceof IPackageItem) {
			IPackageItem packageItem = (IPackageItem)listStack.getItem();
			tile.patternList.add(packageItem.getRecipeInfo(listStack).getPatterns().get(packageItem.getIndex(listStack)));
		}
		if(tile.mode == TilePackager.Mode.FIRST) {
			tile.disjoint = true;
		}
		else if(tile.mode == TilePackager.Mode.DISJOINT) {
			tile.disjoint = MiscUtil.arePatternsDisjoint(tile.patternList);
		}
		if(tile.getWorld() != null && !tile.getWorld().isRemote && tile.hostHelper != null) {
			tile.hostHelper.postPatternChange();
		}
		if(tile.getWorld() != null) {
			Streams.stream(BlockPos.getAllInBox(tile.getPos().add(-1, -1, -1), tile.getPos().add(1, 1, 1))).
			map(tile.getWorld()::getTileEntity).filter(t->t instanceof TilePackagerExtension).
			map(t->(TilePackagerExtension)t).forEach(t->t.updatePatternList());
		}
	}
}
