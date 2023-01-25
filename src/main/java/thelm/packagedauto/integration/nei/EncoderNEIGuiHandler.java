package thelm.packagedauto.integration.nei;

import java.util.List;

import codechicken.nei.api.INEIGuiAdapter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.client.gui.GuiEncoder;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetItemStack;
import thelm.packagedauto.slot.SlotFalseCopy;

public class EncoderNEIGuiHandler extends INEIGuiAdapter {

	public static final EncoderNEIGuiHandler INSTANCE = new EncoderNEIGuiHandler();

	@Override
	public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
		if(gui instanceof GuiEncoder) {
			GuiEncoder encoder = (GuiEncoder)gui;
			mouseX -= encoder.getLeft();
			mouseY -= encoder.getTop();
			for(Slot slot : (List<Slot>)encoder.container.inventorySlots) {
				if(mouseX >= slot.xDisplayPosition-1 && mouseY >= slot.yDisplayPosition-1 && mouseX < slot.xDisplayPosition+17 && mouseY < slot.yDisplayPosition+17) {
					if(slot instanceof SlotFalseCopy && draggedStack != null) {
						PacketHandler.INSTANCE.sendToServer(new PacketSetItemStack((short)slot.slotNumber, draggedStack));
						return true;
					}
				}
			}
		}
		return false;
	}
}
