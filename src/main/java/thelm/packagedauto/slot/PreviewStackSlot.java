package thelm.packagedauto.slot;

import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.world.inventory.StackCopySlot;

public class PreviewStackSlot extends StackCopySlot {

	public final Supplier<ItemStack> stack;

	public PreviewStackSlot(Supplier<ItemStack> stack, int xPosition, int yPosition) {
		super(0, xPosition, yPosition);
		this.stack = stack;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	protected ItemStack getStackCopy() {
		return stack.get().copy();
	}

	@Override
	protected void setStackCopy(ItemStack stack) {}
}
