package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IProxyMarkerItem;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;

public class CraftingProxyItemHandler extends BaseItemHandler<CraftingProxyBlockEntity> {

	public CraftingProxyItemHandler(CraftingProxyBlockEntity blockEntity) {
		super(blockEntity, 1);
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
		return stack.getItem() instanceof IProxyMarkerItem marker && marker.getDirectionalGlobalPos(stack) != null;
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		loadMarker();
	}

	public void loadMarker() {
		ItemStack stack = getStackInSlot(0);
		if(stack.getItem() instanceof IProxyMarkerItem marker) {
			DirectionalGlobalPos pos = marker.getDirectionalGlobalPos(stack);
			if(pos == null) {
				blockEntity.target = null;
			}
			else if(blockEntity.getLevel() != null && !blockEntity.getLevel().dimension().equals(pos.dimension())) {
				blockEntity.target = null;
			}
			else {
				Vec3i dirVec = pos.blockPos().subtract(blockEntity.getBlockPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= CraftingProxyBlockEntity.range) {
					blockEntity.target = pos;
				}
			}
		}
		else {
			blockEntity.target = null;
		}
	}
}
