package thelm.packagedauto.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

// Code from Refined Storage
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

	private static TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
		ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);
		return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;
		RenderSystem.setShaderColor(red, green, blue, alpha);
	}

	private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel) {
		float uMin = textureSprite.getU0();
		float uMax = textureSprite.getU1();
		float vMin = textureSprite.getV0();
		float vMax = textureSprite.getV1();
		uMax = uMax - (maskRight / 16F * (uMax - uMin));
		vMax = vMax - (maskTop / 16F * (vMax - vMin));
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
		bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
		tessellator.end();
	}

	public void render(GuiGraphics graphics, int xPosition, int yPosition, FluidStack fluidStack) {
		render(graphics, xPosition, yPosition, fluidStack, FluidType.BUCKET_VOLUME);
	}

	public void render(GuiGraphics graphics, int xPosition, int yPosition, FluidStack fluidStack, int capacity) {
		RenderSystem.enableBlend();
		drawFluid(graphics, xPosition, yPosition, fluidStack, capacity);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.disableBlend();
	}

	private void drawFluid(GuiGraphics graphics, int xPosition, int yPosition, FluidStack fluidStack, int capacity) {
		if(capacity <= 0 || fluidStack == null || fluidStack.isEmpty()) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluidStack);
		int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);
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

	private void drawTiledSprite(GuiGraphics graphics, int xPosition, int yPosition, int tiledWidth, int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		Matrix4f matrix = graphics.pose().last().pose();
		setGLColorFromInt(color);
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
					int maskTop = TEX_HEIGHT - height;
					int maskRight = TEX_WIDTH - width;
					drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 100);
				}
			}
		}
	}
}
