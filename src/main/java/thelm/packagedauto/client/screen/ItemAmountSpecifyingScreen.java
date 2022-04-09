package thelm.packagedauto.client.screen;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.menu.ItemAmountSpecifyingMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetItemStackPacket;

public class ItemAmountSpecifyingScreen extends AmountSpecifyingScreen<ItemAmountSpecifyingMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/amount_specifying.png");

	private int containerSlot;
	private ItemStack stack;
	private int maxAmount;

	public ItemAmountSpecifyingScreen(BaseScreen<?> parent, Inventory inventory, int containerSlot, ItemStack stack, int maxAmount) {
		super(parent, new ItemAmountSpecifyingMenu(inventory, stack), inventory, new TranslatableComponent("gui.packagedauto.item_amount_specifying"));
		imageWidth = 172;
		imageHeight = 99;
		this.containerSlot = containerSlot;
		this.stack = stack;
		this.maxAmount = maxAmount;
	}

	@Override
	protected int getOkCancelButtonWidth() {
		return 50;
	}

	@Override
	protected Pair<Integer, Integer> getOkCancelPos() {
		return Pair.of(114, 22);
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
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected int[] getIncrements() {
		return new int[] {
				1, 10, 64,
		};
	}

	@Override
	protected void onOkButtonPressed(boolean shiftDown) {
		try {
			int amount = Integer.parseInt(amountField.getValue());
			ItemStack newStack = stack.copy();
			newStack.setCount(amount);
			PacketHandler.INSTANCE.sendToServer(new SetItemStackPacket((short)containerSlot, newStack));
			close();
		}
		catch(NumberFormatException e) {
			// NO OP
		}
	}
}
