package thelm.packagedauto.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.container.ContainerAmountSpecifying;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetItemStack;

// Code from Refined Storage
public class GuiAmountSpecifying extends GuiContainerTileBase<ContainerAmountSpecifying> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/amount_specifying.png");

	private GuiContainerTileBase<?> parent;
	private int containerSlot;
	private ItemStack stack;
	private int maxAmount;

	protected GuiTextField amountField;

	public GuiAmountSpecifying(GuiContainerTileBase<?> parent, InventoryPlayer playerInventory, int containerSlot, ItemStack stack, int maxAmount) {
		super(new ContainerAmountSpecifying(playerInventory, stack));
		xSize = 172;
		ySize = 99;
		this.parent = parent;
		this.containerSlot = containerSlot;
		this.stack = stack;
		this.maxAmount = maxAmount;
	}

	protected int getDefaultAmount() {
		return stack.getCount();
	}

	protected int getMaxAmount() {
		return maxAmount;
	}

	protected int[] getIncrements() {
		return new int[] {
				1, 10, 64,
		};
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		super.initGui();

		addButton(new ButtonSet(0, guiLeft+114, guiTop+22, I18n.translateToLocal("misc.packagedauto.set")));
		addButton(new ButtonCancel(0, guiLeft+114, guiTop+22+24, I18n.translateToLocal("gui.cancel")));

		amountField = new GuiTextField(0, fontRenderer, guiLeft+9, guiTop+51, 63, fontRenderer.FONT_HEIGHT);
		amountField.setEnableBackgroundDrawing(false);
		amountField.setText(String.valueOf(getDefaultAmount()));
		amountField.setTextColor(0xFFFFFF);
		amountField.setValidator(s->{
			if(s.isEmpty()) {
				return true;
			}
			try {
				int amount = Integer.parseInt(s);
				return amount >= 0 && amount <= getMaxAmount();
			}
			catch(NumberFormatException e) {
				return false;
			}
		});
		amountField.setFocused(true);

		int[] increments = getIncrements();
		int xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			String text = "+" + increment;
			addButton(new ButtonIncrement(i, guiLeft+xx, guiTop+20, text));
			xx += 34;
		}
		xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			String text = "-" + increment;
			addButton(new ButtonIncrement(i+3, guiLeft+xx, guiTop+ySize-20-7, text));
			xx += 34;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		amountField.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		fontRenderer.drawString(I18n.translateToLocal("gui.packagedauto.amount_specifying"), 7, 7, 0x404040);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			close();
			return;
		}
		if((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) && amountField.isFocused()) {
			onOkButtonPressed(isShiftKeyDown());
			return;
		}
		if(amountField.textboxKeyTyped(typedChar, keyCode)) {
			return;
		}
		if(mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode) && amountField.isFocused()) {
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button instanceof ButtonSet) {
			onOkButtonPressed(isShiftKeyDown());
		}
		if(button instanceof ButtonCancel) {
			close();
		}
		if(button instanceof ButtonIncrement) {
			int increment = getIncrements()[button.id % 3];
			onIncrementButtonClicked(increment * (button.id / 3 == 0 ? 1 : -1));
		}
	}

	protected void onIncrementButtonClicked(int increment) {
		int oldAmount = 0;
		try {
			oldAmount = Integer.parseInt(amountField.getText());
		}
		catch(NumberFormatException e) {
			// NO OP
		}
		int newAmount = MathHelper.clamp(oldAmount+increment, 0, getMaxAmount());
		amountField.setText(String.valueOf(newAmount));
	}

	protected void onOkButtonPressed(boolean shiftDown) {
		try {
			int amount = MathHelper.clamp(Integer.parseInt(amountField.getText()), 0, maxAmount);
			ItemStack newStack = stack.copy();
			newStack.setCount(amount);
			PacketHandler.INSTANCE.sendToServer(new PacketSetItemStack((short)containerSlot, newStack));
			close();
		}
		catch(NumberFormatException e) {
			// NO OP
		}
	}

	public void close() {
		mc.displayGuiScreen(parent);
	}

	public GuiContainerTileBase<?> getParent() {
		return parent;
	}

	class ButtonSet extends GuiButton {

		public ButtonSet(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 50, 20, text);
		}
	}

	class ButtonCancel extends GuiButton {

		public ButtonCancel(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 50, 20, text);
		}
	}

	class ButtonIncrement extends GuiButton {

		public ButtonIncrement(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 34, 20, text);
		}
	}
}
