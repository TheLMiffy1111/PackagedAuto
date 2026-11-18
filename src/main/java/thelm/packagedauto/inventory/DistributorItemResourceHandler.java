package thelm.packagedauto.inventory;

import com.google.common.primitives.Ints;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.block.entity.DistributorBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;

public class DistributorItemResourceHandler extends BaseItemResourceHandler<DistributorBlockEntity> {

	public DistributorItemResourceHandler(DistributorBlockEntity blockEntity) {
		super(blockEntity, 81);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		loadMarker(index);
		super.onContentsChanged(index, previousContents);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return 1;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return resource.is((Holder<Item>)PackagedAutoItems.DISTRIBUTOR_MARKER);
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return EmptyResourceHandler.instance();
	}

	@Override
	public void deserialize(ValueInput input) {
		super.deserialize(input);
		for(int i = 0; i < 81; ++i) {
			loadMarker(i);
		}
	}

	public void loadMarker(int index) {
		ItemStack stack = getStack(index);
		if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
			DirectionalGlobalPos pos = stack.get(PackagedAutoDataComponents.MARKER_POS);
			if(blockEntity.getLevel() != null && !blockEntity.getLevel().dimension().equals(pos.dimension())) {
				blockEntity.positions.remove(index);
			}
			else {
				Vec3i dirVec = pos.blockPos().subtract(blockEntity.getBlockPos());
				int dist = Ints.max(Math.abs(dirVec.getX()), Math.abs(dirVec.getY()), Math.abs(dirVec.getZ()));
				if(dist <= DistributorBlockEntity.range) {
					blockEntity.positions.put(index, pos);
				}
			}
		}
		else {
			blockEntity.positions.remove(index);
		}
	}
}
