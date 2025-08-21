package thelm.packagedauto.client.screen;

import org.lwjgl.glfw.GLFW;

import com.google.common.primitives.Longs;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.container.BaseContainer;

// Code from Refined Storage
public abstract class AmountSpecifyingScreen<C extends BaseContainer<?>> extends BaseScreen<C> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/amount_specifying.png");

	private BaseScreen<?> parent;

	protected TextFieldWidget amountField;

	public AmountSpecifyingScreen(BaseScreen<?> parent, C container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
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
		buttons.clear();
		super.init();

		addButton(new ButtonSet(leftPos+114, topPos+22, new TranslationTextComponent("misc.packagedauto.set")));
		addButton(new ButtonCancel(leftPos+114, topPos+22+24, new TranslationTextComponent("gui.cancel")));

		amountField = new TextFieldWidget(font, leftPos+9, topPos+51, 63, font.lineHeight, StringTextComponent.EMPTY);
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
		addButton(amountField);

		int[] increments = getIncrements();
		int[] multipliers = getMultipliers();
		int xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			int multiplier = multipliers[i];
			addButton(new ButtonAmount(increment, multiplier, false, leftPos+xx, topPos+20));
			xx += 34;
		}
		xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			int multiplier = multipliers[i];
			addButton(new ButtonAmount(increment, multiplier, true, leftPos+xx, topPos+imageHeight-20-7));
			xx += 34;
		}
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
		amountField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int x, int y) {
		font.draw(matrixStack, getTitle().getString(), 7, 7, 0x404040);
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
		InputMappings.Input mouseKey = InputMappings.getKey(key, scanCode);
		if(minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && amountField.isFocused()) {
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	protected void onIncrementButtonClicked(int increment, int multiplier, boolean inverse, boolean shiftDown) {
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

	class ButtonSet extends Widget {

		public ButtonSet(int x, int y, ITextComponent text) {
			super(x, y, 50, 20, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onOkButtonPressed(hasShiftDown());
		}
	}

	class ButtonCancel extends Widget {

		public ButtonCancel(int x, int y, ITextComponent text) {
			super(x, y, 50, 20, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			close();
		}
	}

	class ButtonAmount extends Widget {

		int increment;
		int multiplier;
		boolean inverse;

		ITextComponent addComponent;
		ITextComponent subComponent;
		ITextComponent mulComponent;
		ITextComponent divComponent;

		public ButtonAmount(int increment, int multiplier, boolean inverse, int x, int y) {
			super(x, y, 34, 20, StringTextComponent.EMPTY);
			this.increment = increment;
			this.multiplier = multiplier;
			this.inverse = inverse;

			addComponent = new StringTextComponent("+" + increment);
			subComponent = new StringTextComponent("-" + increment);
			mulComponent = new StringTextComponent("×" + multiplier);
			divComponent = new StringTextComponent("÷" + multiplier);
		}

		@Override
		public ITextComponent getMessage() {
			if(!hasShiftDown()) {
				return !inverse ? addComponent : subComponent;
			}
			else {
				return !inverse ? mulComponent : divComponent;
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onIncrementButtonClicked(increment, multiplier, inverse, hasShiftDown());
		}
	}
}
