package thelm.packagedauto.integration.jei;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.client.gui.GuiContainerTileBase;
import thelm.packagedauto.client.gui.GuiEncoder;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetItemStack;
import thelm.packagedauto.slot.SlotFalseCopy;

public class EncoderGhostIngredientHandler implements IGhostIngredientHandler<GuiEncoder> {

	@Override
	public <I> List<Target<I>> getTargets(GuiEncoder gui, I ingredient, boolean doStart) {
		ItemStack stack = wrapStack(ingredient);
		if(!stack.isEmpty()) {
			return gui.container.inventorySlots.stream().filter(s->s instanceof SlotFalseCopy).
					<Target<I>>map(s->new SlotTarget<>(gui, s)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public void onComplete() {

	}

	private static ItemStack wrapStack(Object ingredient) {
		if(ingredient instanceof ItemStack) {
			return (ItemStack)ingredient;
		}
		return ItemStack.EMPTY;
	}

	static class SlotTarget<I> implements Target<I> {

		private final Slot slot;
		private final Rectangle area;

		public SlotTarget(GuiContainerTileBase<?> gui, Slot slot) {
			this.slot = slot;
			this.area = new Rectangle(gui.getGuiLeft()+slot.xPos-1, gui.getGuiTop()+slot.yPos-1, 18, 18);
		}

		@Override
		public Rectangle getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemStack stack = wrapStack(ingredient);
			if(!stack.isEmpty()) {
				PacketHandler.INSTANCE.sendToServer(new PacketSetItemStack((short)slot.slotNumber, stack));
			}
		}
	}
}
