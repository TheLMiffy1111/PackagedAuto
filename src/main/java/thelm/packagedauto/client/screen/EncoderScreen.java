package thelm.packagedauto.client.screen;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.packet.CycleRecipeTypePacket;
import thelm.packagedauto.packet.LoadRecipeListPacket;
import thelm.packagedauto.packet.SaveRecipeListPacket;
import thelm.packagedauto.packet.SetPatternIndexPacket;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
public class EncoderScreen extends BaseScreen<EncoderMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/encoder.png");

	public EncoderScreen(EncoderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 258, 314);
	}

	@Override
	protected Identifier getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void init() {
		clearWidgets();
		super.init();
		int patternSlots = menu.blockEntity.patternItemHandlers.length;
		for(int i = 0; i < patternSlots; ++i) {
			addRenderableWidget(new ButtonPatternSlot(i, leftPos+30+(i%10)*18, topPos+(patternSlots > 10 ? 16 : 25)+(i/10)*18));
		}
		addRenderableWidget(new ButtonRecipeType(true, leftPos+189, topPos+74));
		addRenderableWidget(new ButtonRecipeType(false, leftPos+225, topPos+74));
		addRenderableWidget(new ButtonSavePatterns(leftPos+215, topPos+16));
		addRenderableWidget(new ButtonLoadPatterns(leftPos+215, topPos+34));
		addRenderableWidget(new ButtonClearPatterns(leftPos+171, topPos+56));
	}

	@Override
	protected void extractBackgroundAdditional(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Vec3i color = recipeType.getSlotColor(i*9+j);
				graphics.fill(leftPos+8+j*18, topPos+57+i*18, leftPos+24+j*18, topPos+73+i*18, ARGB.color(color.getX(), color.getY(), color.getZ()));
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				int slotIndex = 81+(i*3+j == 4 ? 0 : i*3+j < 4 ? i*3+j+1 : i*3+j);
				Vec3i color = recipeType.getSlotColor(slotIndex);
				graphics.fill(leftPos+198+j*18, topPos+111+i*18, leftPos+214+j*18, topPos+127+i*18, ARGB.color(color.getX(), color.getY(), color.getZ()));
			}
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		Component name = menu.patternItemHandler.recipeType.getShortDisplayName();
		graphics.text(font, name, 212 - font.width(name)/2, 64, 0xFF404040, false);
		super.extractLabels(graphics, mouseX, mouseY);
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		if(recipeType != null) {
			Object rep = recipeType.getRepresentation();
			if(rep instanceof TextureAtlasSprite sprite) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 204, 75, 16, 16);
			}
			if(rep instanceof ItemStack stack) {
				graphics.item(stack, 204, 75);
			}
			if(rep instanceof ItemStackTemplate stack) {
				graphics.item(stack.create(), 204, 75);
			}
			if(rep instanceof ItemResource resource) {
				graphics.item(resource.toStack(), 204, 75);
			}
		}
	}

	@Override
	public int getItemAmountSpecificationLimit(Slot slot) {
		return slot.index > 81 ? 1000000000 : Math.min(slot.getItem().getMaxStackSize(), 1000000000);
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
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			extractDefaultSprite(graphics);
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = menu.blockEntity.patternItemHandlers[id].getStack(i);
				if(!stack.isEmpty()) {
					graphics.item(stack, getX()+1, getY()+1);
					break;
				}
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(new SetPatternIndexPacket(id));
			menu.blockEntity.setPatternIndex(id);
			menu.setupSlots();
		}
	}

	class ButtonRecipeType extends AbstractButton {

		final boolean prev;

		ButtonRecipeType(boolean prev, int x, int y) {
			super(x, y, 10, 18, Component.empty());
			this.prev = prev;
			setTooltip(Tooltip.create(Component.translatable("block.packagedauto.encoder.recipe_type."+(prev ? "prev" : "next"))));
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+1, prev ? 258 : 266, 48, 8, 16, 512, 512);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(new CycleRecipeTypePacket(prev));
			menu.patternItemHandler.cycleRecipeType(prev);
			menu.setupSlots();
		}
	}

	class ButtonSavePatterns extends AbstractButton {

		ButtonSavePatterns(int x, int y) {
			super(x, y, 36, 18, Component.empty());
			Component line0 = Component.translatable("block.packagedauto.encoder.save");
			Component line1 = Component.translatable("block.packagedauto.encoder.save.single").withStyle(ChatFormatting.GRAY);
			setTooltip(Tooltip.create(ComponentUtils.formatList(List.of(line0, line1), Component.literal("\n"))));
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+1, 258, 16, 34, 16, 512, 512);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			boolean single = input.hasShiftDown();
			ClientPacketDistributor.sendToServer(new SaveRecipeListPacket(single));
		}
	}

	class ButtonLoadPatterns extends AbstractButton {

		ButtonLoadPatterns(int x, int y) {
			super(x, y, 36, 18, Component.empty());
			Component line0 = Component.translatable("block.packagedauto.encoder.load");
			Component line1 = Component.translatable("block.packagedauto.encoder.load.single").withStyle(ChatFormatting.GRAY);
			setTooltip(Tooltip.create(ComponentUtils.formatList(List.of(line0, line1), Component.literal("\n"))));
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+1, 258, 32, 34, 16, 512, 512);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			boolean single = input.hasShiftDown();
			ClientPacketDistributor.sendToServer(new LoadRecipeListPacket(single, false));
			menu.blockEntity.loadRecipeList(single, false);
			menu.setupSlots();
		}
	}

	class ButtonClearPatterns extends AbstractButton {

		ButtonClearPatterns(int x, int y) {
			super(x, y, 7, 7, Component.empty());
			Component line0 = Component.translatable("block.packagedauto.encoder.clear");
			Component line1 = Component.translatable("block.packagedauto.encoder.clear.all").withStyle(ChatFormatting.GRAY);
			setTooltip(Tooltip.create(ComponentUtils.formatList(List.of(line0, line1), Component.literal("\n"))));
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			boolean single = input.hasShiftDown();
			ClientPacketDistributor.sendToServer(new LoadRecipeListPacket(single, true));
			menu.blockEntity.loadRecipeList(single, true);
			menu.setupSlots();
		}
	}
}
