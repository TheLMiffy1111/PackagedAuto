package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.packet.SetItemStackPacket;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;

public class EncoderGhostIngredientHandler implements IGhostIngredientHandler<EncoderScreen> {

	@Override
	public <I> List<Target<I>> getTargetsTyped(EncoderScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
		ItemStack stack = wrapStack(ingredient.getIngredient());
		if(!stack.isEmpty()) {
			return gui.menu.slots.stream().filter(s->s instanceof FalseCopySlot).
					<Target<I>>map(s->new SlotTarget<>(s, getSlotArea(gui, s))).toList();
		}
		return List.of();
	}

	@Override
	public void onComplete() {}

	private static Rect2i getSlotArea(AbstractContainerScreen<?> gui, Slot slot) {
		return new Rect2i(gui.getGuiLeft()+slot.x, gui.getGuiTop()+slot.y, 16, 16);
	}

	private static ItemStack wrapStack(Object ingredient) {
		if(ingredient instanceof ItemStack stack) {
			return stack;
		}
		IVolumeType type = ApiImpl.INSTANCE.getVolumeType(ingredient.getClass());
		if(type != null) {
			return MiscHelper.INSTANCE.tryMakeVolumePackage(ingredient);
		}
		return ItemStack.EMPTY;
	}

	private static record SlotTarget<I>(Slot slot, Rect2i area) implements Target<I> {

		@Override
		public Rect2i getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemStack stack = wrapStack(ingredient);
			if(!stack.isEmpty()) {
				PacketDistributor.sendToServer(new SetItemStackPacket(slot.index, stack));
			}
		}
	}
}
