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
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;

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
		return stack.is(PackagedAutoItems.proxy_marker);
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyItemHandler.INSTANCE;
	}

	@Override
	public void load(CompoundTag nbt, HolderLookup.Provider registries) {
		super.load(nbt, registries);
		loadMarker();
	}

	public void loadMarker() {
		ItemStack stack = getStackInSlot(0);
		if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
			DirectionalGlobalPos pos = stack.get(PackagedAutoDataComponents.MARKER_POS);
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
