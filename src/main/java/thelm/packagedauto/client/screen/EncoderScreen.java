package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.packet.CycleRecipeTypePacket;
import thelm.packagedauto.packet.LoadRecipeListPacket;
import thelm.packagedauto.packet.SaveRecipeListPacket;
import thelm.packagedauto.packet.SetPatternIndexPacket;

public class EncoderScreen extends BaseScreen<EncoderMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/encoder.png");

	public EncoderScreen(EncoderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 258;
		imageHeight = 314;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void init() {
		clearWidgets();
		super.init();
		int patternSlots = menu.blockEntity.patternItemHandlers.length;
		for(int i = 0; i < patternSlots; ++i) {
			addRenderableWidget(new ButtonPatternSlot(i, leftPos+29+(i%10)*18, topPos+(patternSlots > 10 ? 16 : 25)+(i/10)*18));
		}
		addRenderableWidget(new ButtonRecipeType(leftPos+204, topPos+74));
		addRenderableWidget(new ButtonSavePatterns(leftPos+213, topPos+16, Component.translatable("block.packagedauto.encoder.save")));
		addRenderableWidget(new ButtonLoadPatterns(leftPos+213, topPos+34, Component.translatable("block.packagedauto.encoder.load")));
	}

	@Override
	protected void renderBgAdditional(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Vec3i color = recipeType.getSlotColor(i*9+j);
				RenderSystem.setShaderColor(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				graphics.blit(BACKGROUND, leftPos+8+j*18, topPos+56+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int slotIndex = 81+(i*3+j == 4 ? 0 : i*3+j < 4 ? i*3+j+1 : i*3+j);
				Vec3i color = recipeType.getSlotColor(slotIndex);
				RenderSystem.setShaderColor(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				graphics.blit(BACKGROUND, leftPos+198+j*18, topPos+110+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		graphics.drawString(font, s, imageWidth/2 - font.width(s)/2, 6, 0x404040, false);
		graphics.drawString(font, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040, false);
		String str = menu.patternItemHandler.recipeType.getShortDisplayName().getString();
		graphics.drawString(font, str, 212 - font.width(str)/2, 64, 0x404040, false);
		super.renderLabels(graphics, mouseX, mouseY);
	}

	class ButtonPatternSlot extends AbstractButton {

		int id;

		ButtonPatternSlot(int id, int x, int y) {
			super(x, y, 18, 18, Component.empty());
			this.id = id;
			setTooltip(Tooltip.create(Component.translatable("block.packagedauto.encoder.pattern_slot", String.format("%02d", id))));
		}

		@Override
		public boolean isHoveredOrFocused() {
			return super.isHoveredOrFocused() || menu.blockEntity.patternIndex == id;
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = menu.blockEntity.patternItemHandlers[id].getStackInSlot(i);
				if(!stack.isEmpty()) {
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					graphics.renderItem(stack, getX()+1, getY()+1);
					break;
				}
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public void onPress() {
			PacketDistributor.SERVER.with(null).send(new SetPatternIndexPacket(id));
			menu.blockEntity.setPatternIndex(id);
			menu.setupSlots();
		}
	}

	class ButtonRecipeType extends AbstractButton {

		ButtonRecipeType(int x, int y) {
			super(x, y, 18, 18, Component.empty());
			setTooltip(Tooltip.create(Component.translatable("block.packagedauto.encoder.change_recipe_type")));
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
			if(recipeType != null) {
				Object rep = recipeType.getRepresentation();
				if(rep instanceof TextureAtlasSprite sprite) {
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					graphics.blit(getX()+1, getY()+1, 0, 16, 16, sprite);
				}
				if(rep instanceof ItemStack stack) {
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					graphics.renderItem(stack, getX()+1, getY()+1);
				}
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public void onPress() {
			boolean reverse = hasShiftDown();
			PacketDistributor.SERVER.with(null).send(new CycleRecipeTypePacket(reverse));
			menu.patternItemHandler.cycleRecipeType(reverse);
			menu.setupSlots();
		}
	}

	class ButtonSavePatterns extends AbstractButton {

		private static final Tooltip TOOLTIP = Tooltip.create(Component.translatable("block.packagedauto.encoder.save_single"));

		ButtonSavePatterns(int x, int y, Component text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(hasShiftDown() ? TOOLTIP : null);
			super.renderWidget(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public void onPress() {
			boolean single = hasShiftDown();
			PacketDistributor.SERVER.with(null).send(new SaveRecipeListPacket(single));
		}
	}

	class ButtonLoadPatterns extends AbstractButton {

		ButtonLoadPatterns(int x, int y, Component text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public void onPress() {
			PacketDistributor.SERVER.with(null).send(new LoadRecipeListPacket());
			menu.blockEntity.loadRecipeList();
			menu.setupSlots();
		}
	}
}
