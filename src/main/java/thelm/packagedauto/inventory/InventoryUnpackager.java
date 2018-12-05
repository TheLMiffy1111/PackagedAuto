package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.tile.TileUnpackager;

public class InventoryUnpackager extends InventoryTileBase {

	public final TileUnpackager tile;

	public InventoryUnpackager(TileUnpackager tile) {
		super(tile, 11);
		this.tile = tile;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		if(index == 9) {
			updateRecipeList();
		}
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		if(index == 9) {
			updateRecipeList();
		}
		return stack;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		switch(index) {
		case 9: return stack.getItem() instanceof IRecipeListItem;
		case 10: return stack.hasCapability(CapabilityEnergy.ENERGY, null);
		default: return stack.getItem() instanceof IPackageItem;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		updateRecipeList();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return index != 9 && index != 10;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return index != 9 && index != 10;
	}

	public void updateRecipeList() {
		tile.recipeList.clear();
		ItemStack listStack = getStackInSlot(9);
		if(listStack.getItem() instanceof IRecipeListItem) {
			tile.recipeList.addAll(((IRecipeListItem)listStack.getItem()).getRecipeList(listStack).getRecipeList());
		}
		if(tile.getWorld() != null && !tile.getWorld().isRemote && tile.hostHelper != null) {
			tile.hostHelper.postPatternChange();
		}
	}
}
