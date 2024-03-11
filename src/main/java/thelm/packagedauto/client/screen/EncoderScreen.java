package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.CycleRecipeTypePacket;
import thelm.packagedauto.network.packet.LoadRecipeListPacket;
import thelm.packagedauto.network.packet.SaveRecipeListPacket;
import thelm.packagedauto.network.packet.SetPatternIndexPacket;

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
	protected void renderBgAdditional(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Vec3i color = recipeType.getSlotColor(i*9+j);
				RenderSystem.setShaderColor(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				blit(poseStack, leftPos+8+j*18, topPos+57+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int slotIndex = 81+(i*3+j == 4 ? 0 : i*3+j < 4 ? i*3+j+1 : i*3+j);
				Vec3i color = recipeType.getSlotColor(slotIndex);
				RenderSystem.setShaderColor(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				blit(poseStack, leftPos+198+j*18, topPos+111+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		font.draw(poseStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(poseStack, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		String str = menu.patternItemHandler.recipeType.getShortDisplayName().getString();
		font.draw(poseStack, str, 212 - font.width(str)/2, 64, 0x404040);
		super.renderLabels(poseStack, mouseX, mouseY);
		for(GuiEventListener child : children()) {
			if(child.isMouseOver(mouseX, mouseY) && child instanceof AbstractWidget button) {
				button.renderToolTip(poseStack, mouseX-leftPos, mouseY-topPos);
				break;
			}
		}
	}

	class ButtonPatternSlot extends AbstractWidget {

		int id;

		ButtonPatternSlot(int id, int x, int y) {
			super(x, y, 18, 18, Component.empty());
			this.id = id;
		}

		@Override
		protected int getYImage(boolean mouseOver) {
			if(menu.blockEntity.patternIndex == id) {
				return 2;
			}
			return super.getYImage(mouseOver);
		}

		@Override
		public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(poseStack, mouseX, mouseY, partialTicks);
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = menu.blockEntity.patternItemHandlers[id].getStackInSlot(i);
				if(!stack.isEmpty()) {
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					minecraft.getItemRenderer().renderGuiItem(stack, x+1, y+1);
					break;
				}
			}
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, Component.translatable("block.packagedauto.encoder.pattern_slot", String.format("%02d", id)), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new SetPatternIndexPacket(id));
			menu.blockEntity.setPatternIndex(id);
			menu.setupSlots();
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	class ButtonRecipeType extends AbstractWidget {

		ButtonRecipeType(int x, int y) {
			super(x, y, 18, 18, Component.empty());
		}

		@Override
		public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(poseStack, mouseX, mouseY, partialTicks);
			IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
			if(recipeType != null) {
				Object rep = recipeType.getRepresentation();
				if(rep instanceof TextureAtlasSprite sprite) {
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
					blit(poseStack, x+1, y+1, 0, 16, 16, sprite);
				}
				if(rep instanceof ItemStack stack) {
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					minecraft.getItemRenderer().renderGuiItem(stack, x+1, y+1);
				}
			}
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, Component.translatable("block.packagedauto.encoder.change_recipe_type"), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			boolean reverse = hasShiftDown();
			PacketHandler.INSTANCE.sendToServer(new CycleRecipeTypePacket(reverse));
			menu.patternItemHandler.cycleRecipeType(reverse);
			menu.setupSlots();
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	class ButtonSavePatterns extends AbstractWidget {

		ButtonSavePatterns(int x, int y, Component text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			if(hasShiftDown()) {
				renderTooltip(poseStack, Component.translatable("block.packagedauto.encoder.save_single"), mouseX, mouseY);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			boolean single = hasShiftDown();
			PacketHandler.INSTANCE.sendToServer(new SaveRecipeListPacket(single));
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	class ButtonLoadPatterns extends AbstractWidget {

		ButtonLoadPatterns(int x, int y, Component text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new LoadRecipeListPacket());
			menu.blockEntity.loadRecipeList();
			menu.setupSlots();
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}
}
