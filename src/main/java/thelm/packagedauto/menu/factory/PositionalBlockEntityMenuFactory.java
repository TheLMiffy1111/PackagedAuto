package thelm.packagedauto.menu.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;

public class PositionalBlockEntityMenuFactory<C extends AbstractContainerMenu, T extends BlockEntity> implements IContainerFactory<C> {

	public interface Factory<C, T> {
		C create(int windowId, Inventory inv, T blockEntity);
	}

	private final Factory<C, T> factory;

	public PositionalBlockEntityMenuFactory(Factory<C, T> factory) {
		this.factory = factory;
	}

	@Override
	public C create(int windowId, Inventory inv, FriendlyByteBuf data) {
		BlockPos pos = data.readBlockPos();
		T blockEntity = (T)inv.player.level.getBlockEntity(pos);
		return factory.create(windowId, inv, blockEntity);
	}
}
