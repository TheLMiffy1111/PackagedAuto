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
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;

public class CraftingProxyItemResourceHandler extends BaseItemResourceHandler<CraftingProxyBlockEntity> {

	public CraftingProxyItemResourceHandler(CraftingProxyBlockEntity blockEntity) {
		super(blockEntity, 1);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		loadMarker();
		super.onContentsChanged(index, previousContents);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return 1;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return resource.is((Holder<Item>)PackagedAutoItems.PROXY_MARKER);
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return EmptyResourceHandler.instance();
	}

	@Override
	public void deserialize(ValueInput input) {
		super.deserialize(input);
		loadMarker();
	}

	public void loadMarker() {
		ItemStack stack = getStack(0);
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
