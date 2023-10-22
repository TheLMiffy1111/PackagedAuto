package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.client.screen.BaseScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.item.VolumePackageItem;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetItemStackPacket;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.util.ApiImpl;

public class EncoderGhostIngredientHandler implements IGhostIngredientHandler<EncoderScreen> {

	@Override
	public <I> List<Target<I>> getTargetsTyped(EncoderScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
		ItemStack stack = wrapStack(ingredient.getIngredient());
		if(!stack.isEmpty()) {
			return gui.menu.slots.stream().filter(s->s instanceof FalseCopySlot).
					<Target<I>>map(s->new SlotTarget<>(gui, s)).toList();
		}
		return List.of();
	}

	@Override
	public void onComplete() {

	}

	private static ItemStack wrapStack(Object ingredient) {
		if(ingredient instanceof ItemStack stack) {
			return stack;
		}
		IVolumeType type = ApiImpl.INSTANCE.getVolumeType(ingredient.getClass());
		if(type != null) {
			return VolumePackageItem.tryMakeVolumePackage(ingredient);
		}
		return ItemStack.EMPTY;
	}

	static class SlotTarget<I> implements Target<I> {

		private final Slot slot;
		private final Rect2i area;

		public SlotTarget(BaseScreen<?> screen, Slot slot) {
			this.slot = slot;
			this.area = new Rect2i(screen.getGuiLeft()+slot.x-1, screen.getGuiTop()+slot.y-1, 18, 18);
		}

		@Override
		public Rect2i getArea() {
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
