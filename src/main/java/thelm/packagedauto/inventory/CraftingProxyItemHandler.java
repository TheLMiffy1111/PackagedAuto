package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IProxyMarkerItem;
import thelm.packagedauto.tile.CraftingProxyTile;

public class CraftingProxyItemHandler extends BaseItemHandler<CraftingProxyTile> {

	public CraftingProxyItemHandler(CraftingProxyTile tile) {
		super(tile, 1);
	}

	@Override
	protected void onContentsChanged(int slot) {
		loadMarker();
		super.onContentsChanged(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() instanceof IProxyMarkerItem && ((IProxyMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack) != null;
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
	}

	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		loadMarker();
	}

	public void loadMarker() {
		ItemStack stack = getStackInSlot(0);
		if(stack.getItem() instanceof IProxyMarkerItem) {
			DirectionalGlobalPos pos = ((IProxyMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack);
			if(pos == null) {
				tile.target = null;
			}
			else if(tile.getLevel() != null && !tile.getLevel().dimension().equals(pos.dimension())) {
				tile.target = null;
			}
			else {
				Vector3i dirVec = pos.blockPos().subtract(tile.getBlockPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= CraftingProxyTile.range) {
					tile.target = pos;
				}
			}
		}
		else {
			tile.target = null;
		}
	}
}
