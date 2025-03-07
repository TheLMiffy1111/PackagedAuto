package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.tile.TileDistributor;

public class InventoryDistributor extends InventoryTileBase {

	public final TileDistributor tile;

	public InventoryDistributor(TileDistributor tile) {
		super(tile, 81);
		this.tile = tile;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		loadMarker(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		loadMarker(index);
		return stack;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return stack.getItem() instanceof IDistributorMarkerItem && ((IDistributorMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack) != null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for(int i = 0; i < 81; ++i) {
			loadMarker(i);
		}
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	public void loadMarker(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if(stack.getItem() instanceof IDistributorMarkerItem) {
			DirectionalGlobalPos pos = ((IDistributorMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack);
			if(pos == null) {
				tile.positions.remove(slot);
			}
			else if(tile.getWorld() != null && tile.getWorld().provider.getDimension() != pos.dimension()) {
				tile.positions.remove(slot);
			}
			else {
				Vec3i dirVec = pos.blockPos().subtract(tile.getPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= TileDistributor.range) {
					tile.positions.put(slot, pos);
				}
			}
		}
		else {
			tile.positions.remove(slot);
		}
	}
}
