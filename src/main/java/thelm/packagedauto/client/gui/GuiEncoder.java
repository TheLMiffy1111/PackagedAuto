package thelm.packagedauto.client.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketCycleRecipeType;
import thelm.packagedauto.network.packet.PacketLoadRecipeList;
import thelm.packagedauto.network.packet.PacketSaveRecipeList;
import thelm.packagedauto.network.packet.PacketSetPatternIndex;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
public class GuiEncoder extends GuiContainerTileBase<ContainerEncoder> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/encoder.png");

	public GuiEncoder(ContainerEncoder container) {
		super(container);
		xSize = 258;
		ySize = 314;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		super.initGui();
		int patternSlots = container.tile.patternInventories.length;
		for(int i = 0; i < patternSlots; ++i) {
			addButton(new GuiButtonPatternSlot(i, guiLeft+30+(i%10)*18, guiTop+(patternSlots > 10 ? 16 : 25)+(i/10)*18));
		}
		addButton(new GuiButtonRecipeType(0, guiLeft+189, guiTop+74));
		addButton(new GuiButtonRecipeType(1, guiLeft+225, guiTop+74));
		addButton(new GuiButtonSavePatterns(0, guiLeft+215, guiTop+16));
		addButton(new GuiButtonLoadPatterns(0, guiLeft+215, guiTop+34));
		addButton(new GuiButtonClearPatterns(0, guiLeft+171, guiTop+56));
		if(Loader.isModLoaded("jei")) {
			addButton(new GuiButtonShowRecipesJEI(0, guiLeft+172, guiTop+129));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		IRecipeType recipeType = container.patternInventory.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Color color = recipeType.getSlotColor(i*9+j);
				GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, color.getAlpha()/255F);
				drawModalRectWithCustomSizedTexture(guiLeft+8+j*18, guiTop+57+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				Color color = recipeType.getSlotColor(81+i*3+j);
				GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, 1);
				drawModalRectWithCustomSizedTexture(guiLeft+198+j*18, guiTop+111+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		GlStateManager.color(1, 1, 1, 1);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize/2 - fontRenderer.getStringWidth(s)/2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		String str = fontRenderer.trimStringToWidth(container.patternInventory.recipeType.getLocalizedNameShort(), 86);
		fontRenderer.drawString(str, 212 - fontRenderer.getStringWidth(str)/2, 64, 0x404040);
		IRecipeType recipeType = container.patternInventory.recipeType;
		if(recipeType != null) {
			Object rep = recipeType.getRepresentation();
			if(rep instanceof TextureAtlasSprite) {
				GlStateManager.color(1, 1, 1, 1);
				mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(204, 75, (TextureAtlasSprite)rep, 16, 16);
			}
			if(rep instanceof ItemStack) {
				RenderHelper.enableGUIStandardItemLighting();
				GlStateManager.color(1, 1, 1, 1);
				mc.getRenderItem().renderItemIntoGUI((ItemStack)rep, 204, 75);
				RenderHelper.disableStandardItemLighting();
			}
		}
		for(GuiButton guibutton : buttonList) {
			if(guibutton.isMouseOver()) {
				guibutton.drawButtonForegroundLayer(mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	public int getItemAmountSpecificationLimit(Slot slot) {
		int stackLimit = slot.getStack().getMaxStackSize();
		return slot.slotNumber > 81 ? Math.max(stackLimit, 999) : stackLimit;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button instanceof GuiButtonPatternSlot) {
			PacketHandler.INSTANCE.sendToServer(new PacketSetPatternIndex(button.id));
			container.tile.setPatternIndex(button.id);
			container.setupSlots();
		}
		if(button instanceof GuiButtonRecipeType) {
			PacketHandler.INSTANCE.sendToServer(new PacketCycleRecipeType(button.id == 0));
			container.patternInventory.cycleRecipeType(button.id == 0);
			container.setupSlots();
		}
		if(button instanceof GuiButtonSavePatterns) {
			boolean single = isShiftKeyDown();
			PacketHandler.INSTANCE.sendToServer(new PacketSaveRecipeList(single));
		}
		if(button instanceof GuiButtonLoadPatterns) {
			boolean single = isShiftKeyDown();
			PacketHandler.INSTANCE.sendToServer(new PacketLoadRecipeList(single, false));
			container.tile.loadRecipeList(single, false);
			container.setupSlots();
		}
		if(button instanceof GuiButtonClearPatterns) {
			boolean single = !isShiftKeyDown();
			PacketHandler.INSTANCE.sendToServer(new PacketLoadRecipeList(single, true));
			container.tile.loadRecipeList(single, true);
			container.setupSlots();
		}
		if(button instanceof GuiButtonShowRecipesJEI) {
			MiscUtil.conditionalRunnable(()->true, ()->()->{
				IRecipeType recipeType = container.patternInventory.recipeType;
				if(recipeType != null) {
					PackagedAutoJEIPlugin.showCategories(recipeType.getJEICategories());
				}
			}, ()->()->{}).run();
		}
	}

	class GuiButtonPatternSlot extends GuiButton {

		GuiButtonPatternSlot(int buttonId, int x, int y) {
			super(buttonId, x, y, 18, 18, "");
		}

		@Override
		protected int getHoverState(boolean mouseOver) {
			if(container.tile.patternIndex == id) {
				return 2;
			}
			return super.getHoverState(mouseOver);
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = container.tile.patternInventories[id].stacks.get(i);
				if(!stack.isEmpty()) {
					RenderHelper.enableGUIStandardItemLighting();
					GlStateManager.color(1, 1, 1, 1);
					mc.getRenderItem().renderItemIntoGUI(stack, x+1, y+1);
					RenderHelper.disableStandardItemLighting();
					break;
				}
			}
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocalFormatted("tile.packagedauto.encoder.pattern_slot", String.format("%02d", id)), mouseX, mouseY);
		}
	}

	class GuiButtonRecipeType extends GuiButton {

		GuiButtonRecipeType(int buttonId, int x, int y) {
			super(buttonId, x, y, 10, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawModalRectWithCustomSizedTexture(x+1, y+1, id == 0 ? 258 : 266, 48, 8, 16, 512, 512);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagedauto.encoder.recipe_type."+(id == 0 ? "prev" : "next")), mouseX, mouseY);
		}
	}

	class GuiButtonSavePatterns extends GuiButton {

		GuiButtonSavePatterns(int buttonId, int x, int y) {
			super(buttonId, x, y, 36, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawModalRectWithCustomSizedTexture(x+1, y+1, 258, 16, 34, 16, 512, 512);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			String line0 = I18n.translateToLocal("tile.packagedauto.encoder.save");
			String line1 = TextFormatting.GRAY+I18n.translateToLocal("tile.packagedauto.encoder.save.single");
			drawHoveringText(Arrays.asList(line0, line1), mouseX, mouseY);
		}
	}

	class GuiButtonLoadPatterns extends GuiButton {

		GuiButtonLoadPatterns(int buttonId, int x, int y) {
			super(buttonId, x, y, 36, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawModalRectWithCustomSizedTexture(x+1, y+1, 258, 32, 34, 16, 512, 512);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			String line0 = I18n.translateToLocal("tile.packagedauto.encoder.load");
			String line1 = TextFormatting.GRAY+I18n.translateToLocal("tile.packagedauto.encoder.load.single");
			drawHoveringText(Arrays.asList(line0, line1), mouseX, mouseY);
		}
	}

	class GuiButtonClearPatterns extends GuiButton {

		GuiButtonClearPatterns(int buttonId, int x, int y) {
			super(buttonId, x, y, 7, 7, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if(visible) {
				hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			}
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			String line0 = I18n.translateToLocal("tile.packagedauto.encoder.clear");
			String line1 = TextFormatting.GRAY+I18n.translateToLocal("tile.packagedauto.encoder.clear.all");
			drawHoveringText(Arrays.asList(line0, line1), mouseX, mouseY);
		}
	}

	class GuiButtonShowRecipesJEI extends GuiButton {

		GuiButtonShowRecipesJEI(int buttonId, int x, int y) {
			super(buttonId, x, y, 22, 16, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if(visible) {
				hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			}
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			IRecipeType recipeType = container.patternInventory.recipeType;
			if(recipeType != null && !recipeType.getJEICategories().isEmpty()) {
				drawHoveringText(I18n.translateToLocal("jei.tooltip.show.recipes"), mouseX, mouseY);
			}
		}
	}
}
