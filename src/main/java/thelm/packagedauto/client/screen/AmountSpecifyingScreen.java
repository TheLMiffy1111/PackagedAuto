package thelm.packagedauto.client.screen;

import org.lwjgl.glfw.GLFW;

import com.google.common.primitives.Longs;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.BaseMenu;

// Code from Refined Storage
public abstract class AmountSpecifyingScreen<C extends BaseMenu<?>> extends BaseScreen<C> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/amount_specifying.png");

	private BaseScreen<?> parent;

	protected EditBox amountField;

	public AmountSpecifyingScreen(BaseScreen<?> parent, C menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 172;
		imageHeight = 99;
		this.parent = parent;
	}

	protected abstract int getDefaultAmount();

	protected abstract int getMaxAmount();

	protected abstract int[] getIncrements();

	protected abstract int[] getMultipliers();

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();

		addRenderableWidget(new ButtonSet(leftPos+114, topPos+22, Component.translatable("misc.packagedauto.set")));
		addRenderableWidget(new ButtonCancel(leftPos+114, topPos+22+24, Component.translatable("gui.cancel")));

		amountField = new EditBox(font, leftPos+9, topPos+51, 63, font.lineHeight, Component.empty());
		amountField.setBordered(false);
		amountField.setValue(String.valueOf(getDefaultAmount()));
		amountField.setTextColor(0xFFFFFF);
		amountField.setFilter(s->{
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
		addRenderableWidget(amountField);

		int[] increments = getIncrements();
		int[] multipliers = getMultipliers();
		int xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			int multiplier = multipliers[i];
			addRenderableWidget(new ButtonAmount(increment, multiplier, false, leftPos+xx, topPos+20));
			xx += 34;
		}
		xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			int multiplier = multipliers[i];
			addRenderableWidget(new ButtonAmount(increment, multiplier, true, leftPos+xx, topPos+imageHeight-20-7));
			xx += 34;
		}
	}

	@Override
	protected void renderBgAdditional(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		amountField.renderButton(poseStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		font.draw(poseStack, getTitle().getString(), 7, 7, 0x404040);
		super.renderLabels(poseStack, mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if(key == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		if((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && amountField.isFocused()) {
			onOkButtonPressed(hasShiftDown());
			return true;
		}
		if(amountField.keyPressed(key, scanCode, modifiers)) {
			return true;
		}
		InputConstants.Key mouseKey = InputConstants.getKey(key, scanCode);
		if(minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && amountField.isFocused()) {
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	protected void onAmountButtonClicked(int increment, int multiplier, boolean inverse, boolean shiftDown) {
		long oldAmount = 0;
		try {
			oldAmount = Integer.parseInt(amountField.getValue());
		}
		catch(NumberFormatException e) {
			// NO OP
		}
		long newAmount;
		if(!shiftDown) {
			newAmount = !inverse ? oldAmount+increment : oldAmount-increment;
		}
		else {
			newAmount = !inverse ? oldAmount*multiplier : oldAmount/multiplier;
		}
		newAmount = Longs.constrainToRange(newAmount, 0, getMaxAmount());
		amountField.setValue(String.valueOf(newAmount));
	}

	protected abstract void onOkButtonPressed(boolean shiftDown);

	public void close() {
		minecraft.setScreen(parent);
	}

	public BaseScreen<?> getParent() {
		return parent;
	}

	class ButtonSet extends AbstractWidget {

		public ButtonSet(int x, int y, Component text) {
			super(x, y, 50, 20, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onOkButtonPressed(hasShiftDown());
		}

		@Override
		public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
	}

	class ButtonCancel extends AbstractWidget {

		public ButtonCancel(int x, int y, Component text) {
			super(x, y, 50, 20, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			close();
		}

		@Override
		public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
	}

	class ButtonAmount extends AbstractWidget {

		int increment;
		int multiplier;
		boolean inverse;

		Component addComponent;
		Component subComponent;
		Component mulComponent;
		Component divComponent;

		public ButtonAmount(int increment, int multiplier, boolean inverse, int x, int y) {
			super(x, y, 34, 20, Component.empty());
			this.increment = increment;
			this.multiplier = multiplier;
			this.inverse = inverse;

			addComponent = Component.literal("+" + increment);
			subComponent = Component.literal("-" + increment);
			mulComponent = Component.literal("×" + multiplier);
			divComponent = Component.literal("÷" + multiplier);
		}

		@Override
		public Component getMessage() {
			if(!hasShiftDown()) {
				return !inverse ? addComponent : subComponent;
			}
			else {
				return !inverse ? mulComponent : divComponent;
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onAmountButtonClicked(increment, multiplier, inverse, hasShiftDown());
		}

		@Override
		public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
	}
}
