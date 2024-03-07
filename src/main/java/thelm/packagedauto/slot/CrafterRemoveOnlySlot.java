package thelm.packagedauto.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.CrafterBlockEntity;

public class CrafterRemoveOnlySlot extends SlotItemHandler {

	public final CrafterBlockEntity blockEntity;

	public CrafterRemoveOnlySlot(CrafterBlockEntity blockEntity, int index, int x, int y) {
		super(blockEntity.getItemHandler(), index, x, y);
		this.blockEntity = blockEntity;
	}

	@Override
	public boolean mayPickup(Player player) {
		return !blockEntity.isWorking;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}
}
