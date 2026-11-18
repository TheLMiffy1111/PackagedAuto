package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.item.PackagedAutoItems;

public class EncoderItemResourceHandler extends BaseItemResourceHandler<EncoderBlockEntity> {

	public EncoderItemResourceHandler(EncoderBlockEntity blockEntity) {
		super(blockEntity, 1);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return resource.is((Holder<Item>)PackagedAutoItems.RECIPE_HOLDER);
	}

	@Override
	public ResourceHandler<ItemResource> getWrapperForDirection(Direction side) {
		return EmptyResourceHandler.instance();
	}
}
