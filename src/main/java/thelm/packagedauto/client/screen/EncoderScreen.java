package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.CycleRecipeTypePacket;
import thelm.packagedauto.network.packet.LoadRecipeListPacket;
import thelm.packagedauto.network.packet.SaveRecipeListPacket;
import thelm.packagedauto.network.packet.SetPatternIndexPacket;

public class EncoderScreen extends BaseScreen<EncoderContainer> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/encoder.png");

	public EncoderScreen(EncoderContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		imageWidth = 258;
		imageHeight = 314;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void init() {
		buttons.clear();
		super.init();
		int patternSlots = menu.tile.patternItemHandlers.length;
		for(int i = 0; i < patternSlots; ++i) {
			addButton(new ButtonPatternSlot(i, leftPos+29+(i%10)*18, topPos+(patternSlots > 10 ? 16 : 25)+(i/10)*18));
		}
		addButton(new ButtonRecipeType(leftPos+204, topPos+74));
		addButton(new ButtonSavePatterns(leftPos+213, topPos+16, new TranslationTextComponent("block.packagedauto.encoder.save")));
		addButton(new ButtonLoadPatterns(leftPos+213, topPos+34, new TranslationTextComponent("block.packagedauto.encoder.load")));
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Vector3i color = recipeType.getSlotColor(i*9+j);
				RenderSystem.color4f(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				blit(matrixStack, leftPos+8+j*18, topPos+57+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				Vector3i color = recipeType.getSlotColor(81+i*3+j);
				RenderSystem.color4f(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				blit(matrixStack, leftPos+198+j*18, topPos+111+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		RenderSystem.color4f(1F, 1F, 1F, 1F);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = menu.tile.getDisplayName().getString();
		font.draw(matrixStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(matrixStack, menu.playerInventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		String str = menu.patternItemHandler.recipeType.getShortDisplayName().getString();
		font.draw(matrixStack, str, 212 - font.width(str)/2, 64, 0x404040);
		for(Widget button : buttons) {
			if(button.isMouseOver(mouseX, mouseY)) {
				button.renderToolTip(matrixStack, mouseX-leftPos, mouseY-topPos);
				break;
			}
		}
	}

	class ButtonPatternSlot extends Widget {

		int id;

		ButtonPatternSlot(int id, int x, int y) {
			super(x, y, 18, 18, StringTextComponent.EMPTY);
			this.id = id;
		}

		@Override
		protected int getYImage(boolean mouseOver) {
			if(menu.tile.patternIndex == id) {
				return 2;
			}
			return super.getYImage(mouseOver);
		}

		@Override
		public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = menu.tile.patternItemHandlers[id].getStackInSlot(i);
				if(!stack.isEmpty()) {
					RenderHelper.turnBackOn();
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					minecraft.getItemRenderer().renderGuiItem(stack, x+1, y+1);
					RenderHelper.turnOff();
					break;
				}
			}
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.encoder.pattern_slot", String.format("%02d", id)), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new SetPatternIndexPacket(id));
			menu.tile.setPatternIndex(id);
			menu.setupSlots();
		}
	}

	class ButtonRecipeType extends Widget {

		ButtonRecipeType(int x, int y) {
			super(x, y, 18, 18, StringTextComponent.EMPTY);
		}

		@Override
		public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
			IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
			if(recipeType != null) {
				Object rep = recipeType.getRepresentation();
				if(rep instanceof TextureAtlasSprite) {
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					minecraft.getTextureManager().bind(PlayerContainer.BLOCK_ATLAS);
					blit(matrixStack, x+1, y+1, 0, 16, 16, (TextureAtlasSprite)rep);
				}
				if(rep instanceof ItemStack) {
					RenderHelper.turnBackOn();
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					minecraft.getItemRenderer().renderGuiItem((ItemStack)rep, x+1, y+1);
					RenderHelper.turnOff();
				}
			}
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.encoder.change_recipe_type"), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			boolean reverse = hasShiftDown();
			PacketHandler.INSTANCE.sendToServer(new CycleRecipeTypePacket(reverse));
			menu.patternItemHandler.cycleRecipeType(reverse);
			menu.setupSlots();
		}
	}

	class ButtonSavePatterns extends Widget {

		ButtonSavePatterns(int x, int y, ITextComponent text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			if(hasShiftDown()) {
				renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.encoder.save_single"), mouseX, mouseY);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			boolean single = hasShiftDown();
			PacketHandler.INSTANCE.sendToServer(new SaveRecipeListPacket(single));
		}
	}

	class ButtonLoadPatterns extends Widget {

		ButtonLoadPatterns(int x, int y, ITextComponent text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new LoadRecipeListPacket());
			menu.tile.loadRecipeList();
			menu.setupSlots();
		}
	}
}
