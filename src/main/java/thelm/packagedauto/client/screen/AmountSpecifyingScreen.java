package thelm.packagedauto.client.screen;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.BaseMenu;

// Code from Refined Storage
public abstract class AmountSpecifyingScreen<C extends BaseMenu<?>> extends BaseScreen<C> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/amount_specifying.png");

	private BaseScreen<?> parent;

	protected EditBox amountField;

	public AmountSpecifyingScreen(BaseScreen<?> parent, C menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 172, 99);
		this.parent = parent;
	}

	protected abstract int getDefaultAmount();

	protected abstract int getMaxAmount();

	protected abstract int[] getIncrements();

	protected abstract int[] getMultipliers();

	@Override
	protected Identifier getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();

		addRenderableWidget(new ButtonSet(leftPos+114, topPos+22, Component.translatable("misc.packagedauto.set")));
		addRenderableWidget(new ButtonCancel(leftPos+114, topPos+22+24, Component.translatable("gui.cancel")));

		String amountText = amountField == null ? String.valueOf(getDefaultAmount()) : amountField.getValue();
		amountField = new EditBox(font, leftPos+9, topPos+51, 63, font.lineHeight, Component.empty());
		amountField.setValue( amountText);
		amountField.setBordered(false);
		amountField.setTextColor(0xFFFFFFFF);
		amountField.setFilter(s->s.matches("[0-9]*"));
		amountField.setResponder(s->{
			if(!s.isEmpty()) {
				try {
					int amount = Integer.parseInt(s);
					if(amount < 0 && amount > getMaxAmount()) {
						amountField.setValue(String.valueOf(Math.clamp(amount, 0, getMaxAmount())));
					}
				}
				catch(NumberFormatException e) {}
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
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, getTitle(), 7, 7, 0xFF404040, false);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if(event.key() == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		if((event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && amountField.isFocused()) {
			onOkButtonPressed(event.hasShiftDown());
			return true;
		}
		if(amountField.keyPressed(event)) {
			return true;
		}
		InputConstants.Key mouseKey = InputConstants.getKey(event);
		if(minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && amountField.isFocused()) {
			return true;
		}
		return super.keyPressed(event);
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
		newAmount = Math.clamp(newAmount, 0, getMaxAmount());
		amountField.setValue(String.valueOf(newAmount));
	}

	protected abstract void onOkButtonPressed(boolean shiftDown);

	public void close() {
		minecraft.setScreen(parent);
	}

	public BaseScreen<?> getParent() {
		return parent;
	}

	class ButtonSet extends AbstractButton {

		public ButtonSet(int x, int y, Component text) {
			super(x, y, 50, 20, text);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			extractDefaultSprite(graphics);
			extractDefaultLabel(graphics.textRendererForWidget(this, HoveredTextEffects.NONE));
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			onOkButtonPressed(input.hasShiftDown());
		}
	}

	class ButtonCancel extends AbstractButton {

		public ButtonCancel(int x, int y, Component text) {
			super(x, y, 50, 20, text);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			extractDefaultSprite(graphics);
			extractDefaultLabel(graphics.textRendererForWidget(this, HoveredTextEffects.NONE));
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			close();
		}
	}

	class ButtonAmount extends AbstractButton {

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
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			extractDefaultSprite(graphics);
			extractDefaultLabel(graphics.textRendererForWidget(this, HoveredTextEffects.NONE));
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

		@Override
		public Component getMessage() {
			if(!minecraft.hasShiftDown()) {
				return !inverse ? addComponent : subComponent;
			}
			else {
				return !inverse ? mulComponent : divComponent;
			}
		}

		@Override
		public void onPress(InputWithModifiers input) {
			onAmountButtonClicked(increment, multiplier, inverse, input.hasShiftDown());
		}
	}
}
