package thelm.packagedauto.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.menu.BaseMenu;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.slot.FalseCopyVolumeSlot;
import thelm.packagedauto.slot.PreviewSlot;

public abstract class BaseScreen<C extends BaseMenu<?>> extends AbstractContainerScreen<C> {

	public final C menu;

	public BaseScreen(C menu, Inventory inventory, Component title) {
		this(menu, inventory, title, 176, 166);
	}

	public BaseScreen(C menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
		super(menu, inventory, title, imageWidth, imageHeight);
		this.menu = menu;
		titleLabelX = imageWidth/2 - font.width(title)/2;
		titleLabelY = 6;
		inventoryLabelX = menu.getPlayerInvX();
		inventoryLabelY = menu.getPlayerInvY()-11;
	}

	protected abstract Identifier getBackgroundTexture();

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		if(imageWidth > 256 || imageHeight > 256) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 512);
		}
		else {
			graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
		}
		extractBackgroundAdditional(graphics, mouseX, mouseY, partialTicks);
		for(int i = 0; i < menu.slots.size(); ++i) {
			Slot slot = menu.slots.get(i);
			if(slot.isActive()) {
				if(slot instanceof FalseCopyVolumeSlot vSlot) {
					IVolumeStackWrapper stack = vSlot.volumeInventory.getStack(slot.getSlotIndex());
					if(!stack.isEmpty()) {
						stack.getVolumeType().render(graphics, leftPos+slot.x, topPos+slot.y, stack);
						extractQuantity(graphics, leftPos+slot.x, topPos+slot.y, String.valueOf(stack.getAmount()), 0xFFFFFF);
					}
				}
				else if((slot instanceof FalseCopySlot || slot instanceof PreviewSlot)
						&& slot.getItem().has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
					IVolumeStackWrapper stack = slot.getItem().get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
					if(!stack.isEmpty()) {
						stack.getVolumeType().render(graphics, leftPos+slot.x, topPos+slot.y, stack);
					}
				}
			}
		}
	}

	protected void extractBackgroundAdditional(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		for(int i = 0; i < menu.slots.size(); ++i) {
			Slot slot = menu.slots.get(i);
			if(slot.isActive() && slot instanceof FalseCopyVolumeSlot vSlot) {
				IVolumeStackWrapper stack = vSlot.volumeInventory.getStack(slot.getSlotIndex());
				if(!stack.isEmpty() && inBounds(slot.x, slot.y, 17, 17, mouseX-leftPos, mouseY-topPos)) {
					graphics.setComponentTooltipForNextFrame(font, stack.getTooltip(), mouseX, mouseY);
				}
			}
		}
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput containerInput) {
		if(mouseButton == 0 &&
				containerInput != ContainerInput.QUICK_MOVE &&
				menu.getCarried().isEmpty() &&
				slot instanceof FalseCopySlot &&
				slot.isActive() &&
				!slot.getItem().isEmpty()) {
			if(!minecraft.hasControlDown() && slot.getItem().has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
				minecraft.setScreen(new VolumeAmountSpecifyingScreen(
						this, minecraft.player.getInventory(), slot.index, slot.getItem().get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK), getVolumeAmountSpecificationLimit(slot)));
			}
			else {
				minecraft.setScreen(new ItemAmountSpecifyingScreen(
						this, minecraft.player.getInventory(), slot.index, slot.getItem(), getItemAmountSpecificationLimit(slot)));
			}
		}
		else {
			super.slotClicked(slot, slotId, mouseButton, containerInput);
		}
	}

	public int getItemAmountSpecificationLimit(Slot slot) {
		return Math.min(slot.getMaxStackSize(), slot.getItem().getMaxStackSize());
	}

	public int getVolumeAmountSpecificationLimit(Slot slot) {
		return 1000000000;
	}

	public boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
		return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
	}

	public void extractQuantity(GuiGraphicsExtractor graphics, int x, int y, String qty, int color) {
		boolean large = minecraft.isEnforceUnicode();
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		if(!large) {
			graphics.pose().scale(0.5F, 0.5F);
		}
		graphics.text(font, qty, (large ? 16 : 30) - font.width(qty), large ? 8 : 22, color, true);
		graphics.pose().popMatrix();
	}
}
