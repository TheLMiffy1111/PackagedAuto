package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.block.entity.DistributorBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;

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
		return stack.is(PackagedAutoItems.DISTRIBUTOR_MARKER);
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyItemHandler.INSTANCE;
	}

	@Override
	public void load(CompoundTag nbt, HolderLookup.Provider registries) {
		super.load(nbt, registries);
		for(int i = 0; i < 81; ++i) {
			loadMarker(i);
		}
	}

	public void loadMarker(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
			DirectionalGlobalPos pos = stack.get(PackagedAutoDataComponents.MARKER_POS);
			if(blockEntity.getLevel() != null && !blockEntity.getLevel().dimension().equals(pos.dimension())) {
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
