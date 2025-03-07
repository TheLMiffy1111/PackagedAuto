package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.item.PackagedAutoItems;

public class EncoderItemHandler extends BaseItemHandler<EncoderBlockEntity> {

	public EncoderItemHandler(EncoderBlockEntity blockEntity) {
		super(blockEntity, 1);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.is(PackagedAutoItems.RECIPE_HOLDER);
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyItemHandler.INSTANCE;
	}
}
