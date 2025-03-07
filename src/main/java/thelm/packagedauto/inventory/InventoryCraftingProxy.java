package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IProxyMarkerItem;
import thelm.packagedauto.tile.TileCraftingProxy;

public class InventoryCraftingProxy extends InventoryTileBase {

	public final TileCraftingProxy tile;

	public InventoryCraftingProxy(TileCraftingProxy tile) {
		super(tile, 1);
		this.tile = tile;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		loadMarker();
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		loadMarker();
		return stack;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return stack.getItem() instanceof IProxyMarkerItem && ((IProxyMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack) != null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		loadMarker();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	public void loadMarker() {
		ItemStack stack = getStackInSlot(0);
		if(stack.getItem() instanceof IProxyMarkerItem) {
			DirectionalGlobalPos pos = ((IProxyMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack);
			if(pos == null) {
				tile.target = null;
			}
			else if(tile.getWorld() != null && tile.getWorld().provider.getDimension() != pos.dimension()) {
				tile.target = null;
			}
			else {
				Vec3i dirVec = pos.blockPos().subtract(tile.getPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= TileCraftingProxy.range) {
					tile.target = pos;
				}
			}
		}
		else {
			tile.target = null;
		}
	}
}
