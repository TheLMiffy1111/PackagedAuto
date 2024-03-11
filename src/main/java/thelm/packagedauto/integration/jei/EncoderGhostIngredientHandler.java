package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.client.screen.BaseScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetItemStackPacket;
import thelm.packagedauto.slot.FalseCopySlot;

public class EncoderGhostIngredientHandler implements IGhostIngredientHandler<EncoderScreen> {

	@Override
	public <I> List<Target<I>> getTargets(EncoderScreen gui, I ingredient, boolean doStart) {
		ItemStack stack = wrapStack(ingredient);
		if(!stack.isEmpty()) {
			return gui.menu.slots.stream().filter(s->s instanceof FalseCopySlot).
					<Target<I>>map(s->new SlotTarget<>(gui, s)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public void onComplete() {}

	private static ItemStack wrapStack(Object ingredient) {
		if(ingredient instanceof ItemStack) {
			return (ItemStack)ingredient;
		}
		return ItemStack.EMPTY;
	}

	static class SlotTarget<I> implements Target<I> {

		private final Slot slot;
		private final Rectangle2d area;

		public SlotTarget(BaseScreen<?> screen, Slot slot) {
			this.slot = slot;
			this.area = new Rectangle2d(screen.getGuiLeft()+slot.x-1, screen.getGuiTop()+slot.y-1, 18, 18);
		}

		@Override
		public Rectangle2d getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemStack stack = wrapStack(ingredient);
			if(!stack.isEmpty()) {
				PacketHandler.INSTANCE.sendToServer(new SetItemStackPacket((short)slot.index, stack));
			}
		}
	}
}
