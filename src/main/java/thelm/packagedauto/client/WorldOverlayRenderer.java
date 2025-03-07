package thelm.packagedauto.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.primitives.Doubles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.ItemDistributorMarker;
import thelm.packagedauto.item.ItemProxyMarker;

// Based on Botania, Scannables, and AE2
public class WorldOverlayRenderer {

	public static final WorldOverlayRenderer INSTANCE = new WorldOverlayRenderer();
	public static final Vec3d BLOCK_SIZE = new Vec3d(1, 1, 1);

	private WorldOverlayRenderer() {}

	private Minecraft mc;
	private List<DirectionalMarkerInfo> directionalMarkers = new LinkedList<>();
	private List<SizedMarkerInfo> sizedMarkers = new LinkedList<>();
	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		mc = Minecraft.getMinecraft();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END || mc.world == null || mc.player == null || mc.isGamePaused()) {
			return;
		}
		for(EnumHand hand : EnumHand.values()) {
			ItemStack stack = mc.player.getHeldItem(hand);
			if(stack.getItem() == ItemDistributorMarker.INSTANCE) {
				DirectionalGlobalPos globalPos = ItemDistributorMarker.INSTANCE.getDirectionalGlobalPos(stack);
				if(globalPos != null) {
					addDirectionalMarkers(Collections.singletonList(globalPos), 0x00FFFF, 1);
				}
			}
			if(stack.getItem() == ItemProxyMarker.INSTANCE) {
				DirectionalGlobalPos globalPos = ItemProxyMarker.INSTANCE.getDirectionalGlobalPos(stack);
				if(globalPos != null) {
					addDirectionalMarkers(Collections.singletonList(globalPos), 0xFF7F00, 1);
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		render(event.getPartialTicks());
	}

	public void addDirectionalMarkers(List<DirectionalGlobalPos> positions, int color, int lifetime) {
		directionalMarkers.add(new DirectionalMarkerInfo(positions, color, lifetime));
	}

	public void addSizedMarker(Vec3d lowerCorner, Vec3d size, int color, int lifetime) {
		sizedMarkers.add(new SizedMarkerInfo(lowerCorner, size, color, lifetime));
	}

	public void addBeams(Vec3d source, List<Vec3d> deltas, int color, int lifetime, boolean fadeout) {
		beams.add(new BeamInfo(source, deltas, color, lifetime, fadeout));
	}

	public void render(float partialTick) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		directionalMarkers.removeIf(marker->marker.shouldRemove(currentTick));
		sizedMarkers.removeIf(marker->marker.shouldRemove(currentTick));
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+partialTick;
		double viewerPosX = mc.getRenderManager().viewerPosX;
		double viewerPosY = mc.getRenderManager().viewerPosY;
		double viewerPosZ = mc.getRenderManager().viewerPosZ;

		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GL11.glLineWidth(4);

		GlStateManager.disableDepth();

		for(DirectionalMarkerInfo marker : directionalMarkers) {
			int r = marker.color>>16&0xFF;
			int g = marker.color>> 8&0xFF;
			int b = marker.color    &0xFF;

			for(DirectionalGlobalPos globalPos : marker.positions) {
				if(globalPos.dimension() != mc.world.provider.getDimension()) {
					continue;
				}

				int range = 64;
				BlockPos blockPos = globalPos.blockPos();
				double distX = viewerPosX-blockPos.getX()-0.5;
				double distY = viewerPosY-blockPos.getY()-0.5;
				double distZ = viewerPosZ-blockPos.getZ()-0.5;
				if(Doubles.max(Math.abs(distX), Math.abs(distY), Math.abs(distZ)) > range) {
					continue;
				}

				GlStateManager.pushMatrix();
				GlStateManager.translate(blockPos.getX()-viewerPosX, blockPos.getY()-viewerPosY, blockPos.getZ()-viewerPosZ);

				EnumFacing direction = globalPos.direction();
				drawMarker(BLOCK_SIZE, direction, r, g, b, 127);
				drawMarker(BLOCK_SIZE, null, r, g, b, 255);

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.enableDepth();

		for(SizedMarkerInfo marker : sizedMarkers) {
			Vec3d lowerCorner = marker.lowerCorner;

			GlStateManager.pushMatrix();
			GlStateManager.translate(lowerCorner.x-viewerPosX, lowerCorner.y-viewerPosY, lowerCorner.z-viewerPosZ);

			int r = marker.color>>16&0xFF;
			int g = marker.color>> 8&0xFF;
			int b = marker.color    &0xFF;
			drawMarker(marker.size, null, r, g, b, 255);

			GlStateManager.popMatrix();
		}

		GL11.glLineWidth(3);

		for(BeamInfo beam : beams) {
			Vec3d source = beam.source;

			GlStateManager.pushMatrix();
			GlStateManager.translate(source.x-viewerPosX, source.y-viewerPosY, source.z-viewerPosZ);

			int r = beam.color>>16&0xFF;
			int g = beam.color>> 8&0xFF;
			int b = beam.color    &0xFF;
			int a = (int)(beam.getAlpha(renderTick)*255);
			for(Vec3d delta : beam.deltas) {
				drawBeam(delta, r, g, b, a);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	public void drawMarker(Vec3d delta, EnumFacing direction, int r, int g, int b, int a) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		buffer.begin(direction == null ? GL11.GL_LINES : GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		if(direction == null || direction == EnumFacing.NORTH) {
			// Face North, Edge Bottom
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			// Face North, Edge Top
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == EnumFacing.SOUTH) {
			// Face South, Edge Bottom
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
			// Face South, Edge Top
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == EnumFacing.WEST) {
			// Face West, Edge Bottom
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
			// Face West, Edge Top
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == EnumFacing.EAST) {
			// Face East, Edge Bottom
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			// Face East, Edge Top
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == EnumFacing.DOWN) {
			// Face Down
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
		}
		if(direction == EnumFacing.UP) {
			// Face Up
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
			// Face North, Edge East
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			// Face South, Edge East
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
			// Face South, Edge West
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
		}
		tessellator.draw();
	}

	public void drawBeam(Vec3d delta, int r, int g, int b, int a) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
		buffer.pos(x, y, z).color(r, g, b, a).endVertex();
		tessellator.draw();
	}

	public static class DirectionalMarkerInfo {

		private List<DirectionalGlobalPos> positions;
		private int color;
		private int lifetime;
		private int startTick;

		public DirectionalMarkerInfo(List<DirectionalGlobalPos> positions, int color, int lifetime) {
			this.positions = positions;
			this.color = color;
			this.lifetime = lifetime;
			startTick = RenderTimer.INSTANCE.getTicks();
		}

		public boolean shouldRemove(int currentTick) {
			if(currentTick < startTick) {
				currentTick += 0x1FFFFF;
			}
			return currentTick-startTick >= lifetime;
		}
	}

	public static class SizedMarkerInfo {

		private Vec3d lowerCorner;
		private Vec3d size;
		private int color;
		private int lifetime;
		private int startTick;

		public SizedMarkerInfo(Vec3d lowerCorner, Vec3d size, int color, int lifetime) {
			this.lowerCorner = lowerCorner;
			this.size = size;
			this.color = color;
			this.lifetime = lifetime;
			startTick = RenderTimer.INSTANCE.getTicks();
		}

		public boolean shouldRemove(int currentTick) {
			if(currentTick < startTick) {
				currentTick += 0x1FFFFF;
			}
			return currentTick-startTick >= lifetime;
		}
	}

	public static class BeamInfo {

		private Vec3d source;
		private List<Vec3d> deltas;
		private int color;
		private int lifetime;
		private int startTick;
		private boolean fadeout;

		public BeamInfo(Vec3d source, List<Vec3d> deltas, int color, int lifetime, boolean fadeout) {
			this.source = source;
			this.deltas = deltas;
			this.color = color;
			this.lifetime = lifetime;
			this.fadeout = fadeout;
			startTick = RenderTimer.INSTANCE.getTicks();
		}

		public boolean shouldRemove(int currentTick) {
			if(currentTick < startTick) {
				currentTick += 0x1FFFFF;
			}
			return currentTick-startTick >= lifetime;
		}

		public float getAlpha(float renderTick) {
			if(!fadeout) {
				return 1;
			}
			float diff = renderTick-startTick;
			if(diff < 0) {
				diff += 0x1FFFFF;
			}
			float factor = Math.min(diff/lifetime, 1);
			return 1-factor*factor;
		}
	}
}
