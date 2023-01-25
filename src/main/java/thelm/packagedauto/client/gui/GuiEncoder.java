package thelm.packagedauto.client.gui;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.integration.nei.NEIHandler;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketCycleRecipeType;
import thelm.packagedauto.network.packet.PacketLoadRecipeList;
import thelm.packagedauto.network.packet.PacketSaveRecipeList;
import thelm.packagedauto.network.packet.PacketSetItemStack;
import thelm.packagedauto.network.packet.PacketSetPatternIndex;
import thelm.packagedauto.slot.SlotFalseCopy;
import thelm.packagedauto.util.MiscHelper;

public class GuiEncoder extends GuiBase<ContainerEncoder> {

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
			buttonList.add(new GuiButtonPatternSlot(i, guiLeft+29+(i%10)*18, guiTop+(patternSlots > 10 ? 16 : 25)+(i/10)*18));
		}
		buttonList.add(new GuiButtonRecipeType(0, guiLeft+204, guiTop+74));
		buttonList.add(new GuiButtonSavePatterns(0, guiLeft+213, guiTop+16, StatCollector.translateToLocal("tile.packagedauto.encoder.save")));
		buttonList.add(new GuiButtonLoadPatterns(0, guiLeft+213, guiTop+34, StatCollector.translateToLocal("tile.packagedauto.encoder.load")));
		if(Loader.isModLoaded("NotEnoughItems")) {
			buttonList.add(new GuiButtonShowRecipesNEI(0, guiLeft+172, guiTop+129));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		IPackageRecipeType recipeType = container.patternInventory.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Color color = recipeType.getSlotColor(i*9+j);
				GL11.glColor4f(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, color.getAlpha()/255F);
				func_146110_a(guiLeft+8+j*18, guiTop+56+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				Color color = recipeType.getSlotColor(81+i*3+j);
				GL11.glColor4f(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, color.getAlpha()/255F);
				func_146110_a(guiLeft+198+j*18, guiTop+110+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		GL11.glColor4f(1, 1, 1, 1);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getInventoryName();
		fontRendererObj.drawString(s, xSize/2 - fontRendererObj.getStringWidth(s)/2, 6, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal(container.playerInventory.getInventoryName()), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		String str = fontRendererObj.trimStringToWidth(container.patternInventory.recipeType.getLocalizedNameShort(), 86);
		fontRendererObj.drawString(str, 212 - fontRendererObj.getStringWidth(str)/2, 64, 0x404040);
		for(GuiButton guibutton : (List<GuiButton>)buttonList) {
			if(guibutton.func_146115_a()) {
				guibutton.func_146111_b(mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button instanceof GuiButtonPatternSlot) {
			PacketHandler.INSTANCE.sendToServer(new PacketSetPatternIndex(button.id));
			container.tile.setPatternIndex(button.id);
			container.setupSlots();
		}
		if(button instanceof GuiButtonRecipeType) {
			boolean reverse = isShiftKeyDown();
			PacketHandler.INSTANCE.sendToServer(new PacketCycleRecipeType(reverse));
			container.patternInventory.cycleRecipeType(reverse);
			container.setupSlots();
		}
		if(button instanceof GuiButtonSavePatterns) {
			boolean single = isShiftKeyDown();
			PacketHandler.INSTANCE.sendToServer(new PacketSaveRecipeList(single));
		}
		if(button instanceof GuiButtonLoadPatterns) {
			PacketHandler.INSTANCE.sendToServer(new PacketLoadRecipeList());
			container.tile.loadRecipeList();
			container.setupSlots();
		}
		if(button instanceof GuiButtonShowRecipesNEI) {
			MiscHelper.INSTANCE.conditionalSupplier(()->true, ()->()->{
				IPackageRecipeType recipeType = container.patternInventory.recipeType;
				if(recipeType != null) {
					NEIHandler.INSTANCE.showCategories(recipeType.getNEICategories());
				}
				return null;
			}, null).get();
		}
	}

	class GuiButtonPatternSlot extends GuiButton {

		GuiButtonPatternSlot(int buttonId, int x, int y) {
			super(buttonId, x, y, 18, 18, "");
		}

		@Override
		public int getHoverState(boolean mouseOver) {
			if(container.tile.patternIndex == id) {
				return 2;
			}
			return super.getHoverState(mouseOver);
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			super.drawButton(mc, mouseX, mouseY);
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = container.tile.patternInventories[id].stacks.get(i);
				if(stack != null) {
					RenderHelper.enableGUIStandardItemLighting();
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					GL11.glColor4f(1, 1, 1, 1);
					itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, xPosition+1, yPosition+1);
					RenderHelper.disableStandardItemLighting();
					GL11.glDisable(GL12.GL_RESCALE_NORMAL);
					break;
				}
			}
		}

		@Override
		public void func_146111_b(int mouseX, int mouseY) {
			drawCreativeTabHoveringText(StatCollector.translateToLocalFormatted("tile.packagedauto.encoder.pattern_slot", String.format("%02d", id)), mouseX, mouseY);
		}
	}

	class GuiButtonRecipeType extends GuiButton {

		GuiButtonRecipeType(int buttonId, int x, int y) {
			super(buttonId, x, y, 18, 18, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			super.drawButton(mc, mouseX, mouseY);
			IPackageRecipeType recipeType = container.patternInventory.recipeType;
			if(recipeType != null) {
				Object rep = recipeType.getRepresentation();
				if(rep instanceof IIcon) {
					GL11.glColor4f(1, 1, 1, 1);
					mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);
					drawTexturedModelRectFromIcon(xPosition+1, yPosition+1, (IIcon)rep, 16, 16);
				}
				if(rep instanceof ItemStack) {
					RenderHelper.enableGUIStandardItemLighting();
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					GL11.glColor4f(1, 1, 1, 1);
					itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), (ItemStack)rep, xPosition+1, yPosition+1);
					RenderHelper.disableStandardItemLighting();
					GL11.glDisable(GL12.GL_RESCALE_NORMAL);
				}
			}
		}

		@Override
		public void func_146111_b(int mouseX, int mouseY) {
			drawCreativeTabHoveringText(StatCollector.translateToLocal("tile.packagedauto.encoder.change_recipe_type"), mouseX, mouseY);
		}
	}

	class GuiButtonSavePatterns extends GuiButton {

		GuiButtonSavePatterns(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 38, 18, text);
		}

		@Override
		public void func_146111_b(int mouseX, int mouseY) {
			if(isShiftKeyDown()) {
				drawCreativeTabHoveringText(StatCollector.translateToLocal("tile.packagedauto.encoder.save_single"), mouseX, mouseY);
			}
		}
	}

	class GuiButtonLoadPatterns extends GuiButton {

		GuiButtonLoadPatterns(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 38, 18, text);
		}
	}

	class GuiButtonShowRecipesNEI extends GuiButton {

		GuiButtonShowRecipesNEI(int buttonId, int x, int y) {
			super(buttonId, x, y, 22, 16, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			if(visible) {
				field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition+width && mouseY < yPosition+height;
				mouseDragged(mc, mouseX, mouseY);
			}
		}

		@Override
		public void func_146111_b(int mouseX, int mouseY) {
			IPackageRecipeType recipeType = container.patternInventory.recipeType;
			if(recipeType != null && !recipeType.getNEICategories().isEmpty()) {
				drawCreativeTabHoveringText(StatCollector.translateToLocal("nei.recipe.tooltip"), mouseX, mouseY);
			}
		}
	}
}
