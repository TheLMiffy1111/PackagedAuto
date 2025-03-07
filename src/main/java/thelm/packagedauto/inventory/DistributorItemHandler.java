package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.tile.DistributorTile;

public class DistributorItemHandler extends BaseItemHandler<DistributorTile> {

	public DistributorItemHandler(DistributorTile tile) {
		super(tile, 81);
	}

	@Override
	protected void onContentsChanged(int slot) {
		loadMarker(slot);
		super.onContentsChanged(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() instanceof IDistributorMarkerItem && ((IDistributorMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack) != null;
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
	}

	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		for(int i = 0; i < 81; ++i) {
			loadMarker(i);
		}
	}

	public void loadMarker(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if(stack.getItem() instanceof IDistributorMarkerItem) {
			DirectionalGlobalPos pos = ((IDistributorMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack);
			if(pos == null) {
				tile.positions.remove(slot);
			}
			else if(tile.getLevel() != null && !tile.getLevel().dimension().equals(pos.dimension())) {
				tile.positions.remove(slot);
			}
			else {
				Vector3i dirVec = pos.blockPos().subtract(tile.getBlockPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= DistributorTile.range) {
					tile.positions.put(slot, pos);
				}
			}
		}
		else {
			tile.positions.remove(slot);
		}
	}
}
