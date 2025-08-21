package thelm.packagedauto.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.container.ContainerItemAmountSpecifying;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetItemStack;

public class GuiItemAmountSpecifying extends GuiAmountSpecifying<ContainerItemAmountSpecifying> {

	private int containerSlot;
	private ItemStack stack;
	private int maxAmount;

	public GuiItemAmountSpecifying(GuiContainerTileBase<?> parent, InventoryPlayer playerInventory, int containerSlot, ItemStack stack, int maxAmount) {
		super(parent, new ContainerItemAmountSpecifying(playerInventory, stack));
		this.containerSlot = containerSlot;
		this.stack = stack;
		this.maxAmount = maxAmount;
	}

	@Override
	protected int getDefaultAmount() {
		return stack.getCount();
	}

	@Override
	protected int getMaxAmount() {
		return maxAmount;
	}

	@Override
	protected int[] getIncrements() {
		return new int[] {1, 10, 64};
	}

	@Override
	protected int[] getMultipliers() {
		return new int[] {2, 3, 5};
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		fontRenderer.drawString(I18n.translateToLocal("gui.packagedauto.item_amount_specifying"), 7, 7, 0x404040);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void onOkButtonPressed(boolean shiftDown) {
		try {
			int amount = MathHelper.clamp(Integer.parseInt(amountField.getText()), 0, maxAmount);
			ItemStack newStack = stack.copy();
			newStack.setCount(amount);
			PacketHandler.INSTANCE.sendToServer(new PacketSetItemStack(containerSlot, newStack));
			close();
		}
		catch(NumberFormatException e) {
			// NO OP
		}
	}
}
