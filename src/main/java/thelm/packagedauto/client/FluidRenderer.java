package thelm.packagedauto.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class FluidRenderer {

	public static final FluidRenderer INSTANCE = new FluidRenderer(16, 16, 16);

	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;

	private final int width;
	private final int height;
	private final int minHeight;

	public FluidRenderer(int width, int height, int minHeight) {
		this.width = width;
		this.height = height;
		this.minHeight = minHeight;
	}

	public void render(GuiGraphicsExtractor graphics, int xPosition, int yPosition, FluidStack fluidStack) {
		render(graphics, xPosition, yPosition, fluidStack, FluidType.BUCKET_VOLUME);
	}

	public void render(GuiGraphicsExtractor graphics, int xPosition, int yPosition, FluidStack fluidStack, int capacity) {
		drawFluid(graphics, xPosition, yPosition, fluidStack, capacity);
	}

	private void drawFluid(GuiGraphicsExtractor graphics, int xPosition, int yPosition, FluidStack fluidStack, int capacity) {
		if(capacity <= 0 || fluidStack == null || fluidStack.isEmpty()) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
		TextureAtlasSprite fluidStillSprite = fluidModel.stillMaterial().sprite();
		int fluidColor = fluidModel.fluidTintSource().colorAsStack(fluidStack);
		int amount = fluidStack.getAmount();
		int scaledAmount = (amount * height) / capacity;
		if(amount > 0 && scaledAmount < minHeight) {
			scaledAmount = minHeight;
		}
		if(scaledAmount > height) {
			scaledAmount = height;
		}
		drawTiledSprite(graphics, xPosition, yPosition, width, height, fluidColor, scaledAmount, fluidStillSprite);
	}

	private void drawTiledSprite(GuiGraphicsExtractor graphics, int xPosition, int yPosition, int tiledWidth, int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
		int xTileCount = tiledWidth / TEX_WIDTH;
		int xRemainder = tiledWidth - (xTileCount * TEX_WIDTH);
		int yTileCount = scaledAmount / TEX_HEIGHT;
		int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);
		int yStart = yPosition + tiledHeight;
		for(int xTile = 0; xTile <= xTileCount; xTile++) {
			for(int yTile = 0; yTile <= yTileCount; yTile++) {
				int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
				int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
				int x = xPosition + (xTile * TEX_WIDTH);
				int y = yStart - ((yTile + 1) * TEX_HEIGHT);
				if(width > 0 && height > 0) {
					graphics.enableScissor(x, y + TEX_HEIGHT - height, x + width, y + TEX_HEIGHT);
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, TEX_WIDTH, TEX_HEIGHT, color);
					graphics.disableScissor();
				}
			}
		}
	}
}
