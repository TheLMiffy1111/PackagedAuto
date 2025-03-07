package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.block.entity.DistributorBlockEntity;

public class DistributorItemHandler extends BaseItemHandler<DistributorBlockEntity> {

	public DistributorItemHandler(DistributorBlockEntity blockEntity) {
		super(blockEntity, 81);
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
		return stack.getItem() instanceof IDistributorMarkerItem marker && marker.getDirectionalGlobalPos(stack) != null;
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		for(int i = 0; i < 81; ++i) {
			loadMarker(i);
		}
	}

	public void loadMarker(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if(stack.getItem() instanceof IDistributorMarkerItem marker) {
			DirectionalGlobalPos pos = marker.getDirectionalGlobalPos(stack);
			if(pos == null) {
				blockEntity.positions.remove(slot);
			}
			else if(blockEntity.getLevel() != null && !blockEntity.getLevel().dimension().equals(pos.dimension())) {
				blockEntity.positions.remove(slot);
			}
			else {
				Vec3i dirVec = pos.blockPos().subtract(blockEntity.getBlockPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= DistributorBlockEntity.range) {
					blockEntity.positions.put(slot, pos);
				}
			}
		}
		else {
			blockEntity.positions.remove(slot);
		}
	}
}
