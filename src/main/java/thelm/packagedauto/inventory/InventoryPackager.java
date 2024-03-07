package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import com.google.common.collect.Streams;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;

public class InventoryPackager extends InventoryTileBase {

	public final TilePackager tile;

	public InventoryPackager(TilePackager tile) {
		super(tile, 12);
		this.tile = tile;
		slots = IntStream.rangeClosed(0, 9).toArray();
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
			if(tile.isWorking && !getStackInSlot(index).isEmpty() && !tile.isInputValid()) {
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
		case 2: return tile.mode.ordinal();
		case 3: return tile.getEnergyStorage().getEnergyStored();
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
		case 2:
			tile.mode = TilePackager.Mode.values()[value];
			break;
		case 3:
			tile.getEnergyStorage().setEnergyStored(value);
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
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
			IRecipeInfo recipe = packageItem.getRecipeInfo(listStack);
			if(recipe != null) {
				tile.patternList.add(recipe.getPatterns().get(packageItem.getIndex(listStack)));
			}
		}
		switch(tile.mode) {
		case EXACT:
			tile.disjoint = false;
			break;
		case DISJOINT:
			tile.disjoint = MiscUtil.arePatternsDisjoint(tile.patternList);
			break;
		case FIRST:
			tile.disjoint = true;
			break;
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
