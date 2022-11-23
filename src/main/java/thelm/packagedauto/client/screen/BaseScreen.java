package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.menu.BaseMenu;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.slot.FalseCopyVolumeSlot;
import thelm.packagedauto.slot.PreviewSlot;

public abstract class BaseScreen<C extends BaseMenu<?>> extends AbstractContainerScreen<C> {

	public final C menu;

	public BaseScreen(C menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.menu = menu;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		renderTooltip(poseStack, mouseX, mouseY);
	}

	protected abstract ResourceLocation getBackgroundTexture();

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, getBackgroundTexture());
		if(imageWidth > 256 || imageHeight > 256) {
			blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 512);
		}
		else {
			blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		}
		renderBgAdditional(poseStack, partialTicks, mouseX, mouseY);
		for(int i = 0; i < menu.slots.size(); ++i) {
			Slot slot = menu.slots.get(i);
			if(slot.isActive()) {
				if(slot instanceof FalseCopyVolumeSlot vSlot) {
					IVolumeStackWrapper stack = vSlot.volumeInventory.getStackInSlot(slot.getSlotIndex());
					if(!stack.isEmpty()) {
						stack.getVolumeType().render(poseStack, leftPos+slot.x, topPos+slot.y, stack);
						renderQuantity(poseStack, leftPos+slot.x, topPos+slot.y, String.valueOf(stack.getAmount()), 0xFFFFFF);
					}
				}
				else if((slot instanceof FalseCopySlot || slot instanceof PreviewSlot)
						&& slot.getItem().getItem() instanceof IVolumePackageItem vPackage) {
					IVolumeStackWrapper stack = vPackage.getVolumeStack(slot.getItem());
					if(!stack.isEmpty()) {
						stack.getVolumeType().render(poseStack, leftPos+slot.x, topPos+slot.y, stack);
					}
				}
			}
		}
	}

	protected void renderBgAdditional(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {

	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		for(int i = 0; i < menu.slots.size(); ++i) {
			Slot slot = menu.slots.get(i);
			if(slot.isActive() && slot instanceof FalseCopyVolumeSlot vSlot) {
				IVolumeStackWrapper stack = vSlot.volumeInventory.getStackInSlot(slot.getSlotIndex());
				if(!stack.isEmpty() && inBounds(slot.x, slot.y, 17, 17, mouseX-leftPos, mouseY-topPos)) {
					renderComponentTooltip(poseStack, stack.getTooltip(), mouseX-leftPos, mouseY-topPos);
				}
			}
		}
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
		boolean valid = type != ClickType.QUICK_MOVE && minecraft.player.containerMenu.getCarried().isEmpty();
		if(valid && slot instanceof FalseCopySlot fSlot && slot.isActive()) {
			if(!slot.getItem().isEmpty()) {
				if(!hasControlDown() && slot.getItem().getItem() instanceof IVolumePackageItem vPackage) {
					minecraft.setScreen(new VolumeAmountSpecifyingScreen(
							this, minecraft.player.getInventory(),
							slot.index, vPackage.getVolumeStack(slot.getItem()),
							1000000));
				}
				else {
					minecraft.setScreen(new ItemAmountSpecifyingScreen(
							this, minecraft.player.getInventory(),
							slot.index, slot.getItem(),
							Math.min(slot.getMaxStackSize(), slot.getItem().getMaxStackSize())));
				}
			}
		}
		else {
			super.slotClicked(slot, slotId, mouseButton, type);
		}
	}

	public boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
		return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
	}

	public void renderQuantity(PoseStack poseStack, int x, int y, String qty, int color) {
		boolean large = minecraft.isEnforceUnicode();
		poseStack.pushPose();
		poseStack.translate(x, y, 300);
		if(!large) {
			poseStack.scale(0.5F, 0.5F, 1);
		}
		font.drawShadow(poseStack, qty, (large ? 16 : 30) - font.width(qty), large ? 8 : 22, color);
		poseStack.popPose();
	}
}
