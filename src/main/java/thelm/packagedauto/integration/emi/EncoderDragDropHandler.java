package thelm.packagedauto.integration.emi;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.packet.SetItemStackPacket;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;

public class EncoderDragDropHandler implements EmiDragDropHandler<EncoderScreen> {

	@Override
	public boolean dropStack(EncoderScreen gui, EmiIngredient ingredient, int x, int y) {
		ItemStack stack = wrapStack(ingredient);
		if(stack.isEmpty()) {
			return false;
		}
		for(SlotTarget target : getTargets(gui, ingredient)) {
			if(target.getBounds().contains(x, y)) {
				target.accept(ingredient);
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(EncoderScreen gui, EmiIngredient ingredient, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		ItemStack stack = wrapStack(ingredient);
		if(stack.isEmpty()) {
			return;
		}
		for(SlotTarget target : getTargets(gui, ingredient)) {
			Bounds bounds = target.getBounds();
			graphics.fill(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 0x8822BB33);
		}
	}

	public List<SlotTarget> getTargets(EncoderScreen gui, EmiIngredient ingredient) {
		ItemStack stack = wrapStack(ingredient);
		if(!stack.isEmpty()) {
			return gui.menu.slots.stream().filter(s->s instanceof FalseCopySlot).
					map(s->new SlotTarget(s, getSlotBounds(gui, s))).toList();
		}
		return List.of();
	}

	private static Bounds getSlotBounds(AbstractContainerScreen<?> gui, Slot slot) {
		return new Bounds(gui.getGuiLeft()+slot.x, gui.getGuiTop()+slot.y, 16, 16);
	}

	private static ItemStack wrapStack(EmiIngredient emiIngredient) {
		Optional<?> ingredient = PackagedAutoEMIPlugin.toStack(PackagedAutoEMIPlugin.getTreeEmiStack(emiIngredient));
		if(ingredient.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if(ingredient.get() instanceof ItemStack stack) {
			return stack;
		}
		IVolumeType type = ApiImpl.INSTANCE.getVolumeType(ingredient.get().getClass());
		if(type != null) {
			return MiscHelper.INSTANCE.tryMakeVolumePackage(ingredient.get());
		}
		return ItemStack.EMPTY;
	}

	private static record SlotTarget(Slot slot, Bounds bounds) {

		public Bounds getBounds() {
			return bounds;
		}

		public void accept(EmiIngredient emiIngredient) {
			ItemStack stack = wrapStack(emiIngredient);
			if(!stack.isEmpty()) {
				PacketDistributor.sendToServer(new SetItemStackPacket(slot.index, stack));
			}
		}
	}
}
