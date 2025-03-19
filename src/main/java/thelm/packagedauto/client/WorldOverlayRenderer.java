package thelm.packagedauto.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.DistributorMarkerItem;
import thelm.packagedauto.item.ProxyMarkerItem;

// Based on Botania, Scannables, and AE2
public class WorldOverlayRenderer {

	public static final WorldOverlayRenderer INSTANCE = new WorldOverlayRenderer();
	public static final Vector3d BLOCK_SIZE = new Vector3d(1, 1, 1);

	private WorldOverlayRenderer() {}

	private Minecraft mc;
	private List<DirectionalMarkerInfo> directionalMarkers = new LinkedList<>();
	private List<SizedMarkerInfo> sizedMarkers = new LinkedList<>();
	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		mc = Minecraft.getInstance();
		MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(this::onRenderWorldLast);
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END || mc.level == null || mc.player == null || mc.isPaused()) {
			return;
		}
		for(Hand hand : Hand.values()) {
			ItemStack stack = mc.player.getItemInHand(hand);
			if(stack.getItem() == DistributorMarkerItem.INSTANCE) {
				DirectionalGlobalPos globalPos = DistributorMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
				if(globalPos != null) {
					addDirectionalMarkers(Collections.singletonList(globalPos), 0x00FFFF, 1);
				}
			}
			if(stack.getItem() == ProxyMarkerItem.INSTANCE) {
				DirectionalGlobalPos globalPos = ProxyMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
				if(globalPos != null) {
					addDirectionalMarkers(Collections.singletonList(globalPos), 0xFF7F00, 1);
				}
			}
		}
	}

	public void onRenderWorldLast(RenderWorldLastEvent event) {
		render(event.getMatrixStack(), event.getPartialTicks());
	}

	public void addDirectionalMarkers(List<DirectionalGlobalPos> positions, int color, int lifetime) {
		directionalMarkers.add(new DirectionalMarkerInfo(positions, color, lifetime));
	}

	public void addSizedMarker(Vector3d lowerCorner, Vector3d size, int color, int lifetime) {
		sizedMarkers.add(new SizedMarkerInfo(lowerCorner, size, color, lifetime));
	}

	public void addBeams(Vector3d source, List<Vector3d> deltas, int color, int lifetime, boolean fadeout) {
		beams.add(new BeamInfo(source, deltas, color, lifetime, fadeout));
	}

	public void render(MatrixStack matrixStack, float partialTick) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		directionalMarkers.removeIf(marker->marker.shouldRemove(currentTick));
		sizedMarkers.removeIf(marker->marker.shouldRemove(currentTick));
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+partialTick;
		Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		IRenderTypeBuffer.Impl buffers = RenderTypeHelper.BUFFERS;
		IVertexBuilder quadBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_QUAD);
		IVertexBuilder lineBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_LINE_4);

		for(DirectionalMarkerInfo marker : directionalMarkers) {
			int r = marker.color>>16&0xFF;
			int g = marker.color>> 8&0xFF;
			int b = marker.color    &0xFF;

			for(DirectionalGlobalPos globalPos : marker.positions) {
				if(!globalPos.dimension().equals(mc.level.dimension())) {
					continue;
				}

				int range = 64;
				BlockPos blockPos = globalPos.blockPos();
				Vector3d distVec = cameraPos.subtract(Vector3d.atCenterOf(blockPos));
				if(Doubles.max(Math.abs(distVec.x), Math.abs(distVec.y), Math.abs(distVec.z)) > range) {
					continue;
				}

				matrixStack.pushPose();
				matrixStack.translate(blockPos.getX()-cameraPos.x, blockPos.getY()-cameraPos.y, blockPos.getZ()-cameraPos.z);

				Direction direction = globalPos.direction();
				addMarkerVertices(matrixStack, quadBuffer, BLOCK_SIZE, direction, r, g, b, 127);
				addMarkerVertices(matrixStack, lineBuffer, BLOCK_SIZE, null, r, g, b, 255);

				matrixStack.popPose();
			}
		}

		RenderSystem.disableDepthTest();
		buffers.endBatch();
		RenderSystem.enableDepthTest();

		lineBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_LINE_4);

		for(SizedMarkerInfo marker : sizedMarkers) {
			Vector3d lowerCorner = marker.lowerCorner;

			matrixStack.pushPose();
			matrixStack.translate(lowerCorner.x-cameraPos.x, lowerCorner.y-cameraPos.y, lowerCorner.z-cameraPos.z);

			int r = marker.color>>16&0xFF;
			int g = marker.color>> 8&0xFF;
			int b = marker.color    &0xFF;
			addMarkerVertices(matrixStack, lineBuffer, marker.size, null, r, g, b, 255);

			matrixStack.popPose();
		}

		buffers.endBatch();

		lineBuffer = buffers.getBuffer(RenderTypeHelper.BEAM_LINE_3);

		for(BeamInfo beam : beams) {
			Vector3d source = beam.source;

			matrixStack.pushPose();
			matrixStack.translate(source.x-cameraPos.x, source.y-cameraPos.y, source.z-cameraPos.z);

			int r = beam.color>>16&0xFF;
			int g = beam.color>> 8&0xFF;
			int b = beam.color    &0xFF;
			int a = (int)(beam.getAlpha(renderTick)*255);
			for(Vector3d delta : beam.deltas) {
				addBeamVertices(matrixStack, lineBuffer, delta, r, g, b, a);
			}

			matrixStack.popPose();
		}

		buffers.endBatch();
	}

	public void addMarkerVertices(MatrixStack matrixStack, IVertexBuilder buffer, Vector3d delta, Direction direction, int r, int g, int b, int a) {
		Matrix4f pose = matrixStack.last().pose();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		if(direction == null || direction == Direction.NORTH) {
			// Face North, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			// Face North, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == Direction.SOUTH) {
			// Face South, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
			// Face South, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == Direction.WEST) {
			// Face West, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
			// Face West, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == Direction.EAST) {
			// Face East, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			// Face East, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == Direction.DOWN) {
			// Face Down
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
		}
		if(direction == Direction.UP) {
			// Face Up
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
			// Face North, Edge East
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			// Face South, Edge East
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
			// Face South, Edge West
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
		}
	}

	public void addBeamVertices(MatrixStack matrixStack, IVertexBuilder buffer, Vector3d delta, int r, int g, int b, int a) {
		Matrix4f pose = matrixStack.last().pose();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
		buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
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

		private Vector3d lowerCorner;
		private Vector3d size;
		private int color;
		private int lifetime;
		private int startTick;

		public SizedMarkerInfo(Vector3d lowerCorner, Vector3d size, int color, int lifetime) {
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

		private Vector3d source;
		private List<Vector3d> deltas;
		private int color;
		private int lifetime;
		private int startTick;
		private boolean fadeout;

		public BeamInfo(Vector3d source, List<Vector3d> deltas, int color, int lifetime, boolean fadeout) {
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

	public static class RenderTypeHelper extends RenderType {

		private RenderTypeHelper(String name, VertexFormat format, int mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
			super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
		}

		public static final RenderType MARKER_LINE_4;
		public static final RenderType MARKER_QUAD;
		public static final RenderType BEAM_LINE_3;
		public static final IRenderTypeBuffer.Impl BUFFERS;

		static {
			MARKER_LINE_4 = create("packagedauto:marker_line_4",
					DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 8192, false, false,
					State.builder().
					setLineState(new LineState(OptionalDouble.of(4))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			MARKER_QUAD = create("packagedauto:marker_quad",
					DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 1024, false, false,
					State.builder().
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			BEAM_LINE_3 = create("packagedauto:beam_line_3",
					DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 8192, false, false,
					State.builder().
					setLineState(new LineState(OptionalDouble.of(3))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setOutputState(ITEM_ENTITY_TARGET).
					setWriteMaskState(COLOR_DEPTH_WRITE).
					setCullState(NO_CULL).
					createCompositeState(false));
			BUFFERS = IRenderTypeBuffer.immediateWithBuffers(
					ImmutableMap.of(MARKER_LINE_4, new BufferBuilder(MARKER_LINE_4.bufferSize()),
							MARKER_QUAD, new BufferBuilder(MARKER_QUAD.bufferSize()),
							BEAM_LINE_3, new BufferBuilder(BEAM_LINE_3.bufferSize())),
					Tessellator.getInstance().getBuilder());
		}
	}
}
