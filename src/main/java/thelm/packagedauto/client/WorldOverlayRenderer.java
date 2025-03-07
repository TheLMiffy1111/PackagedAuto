package thelm.packagedauto.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.DistributorMarkerItem;
import thelm.packagedauto.item.ProxyMarkerItem;

// Based on Botania, Scannables, and AE2
@SuppressWarnings("removal")
public class WorldOverlayRenderer {

	public static final WorldOverlayRenderer INSTANCE = new WorldOverlayRenderer();
	public static final Vec3 BLOCK_SIZE = new Vec3(1, 1, 1);

	private WorldOverlayRenderer() {}

	private Minecraft mc;
	private List<DirectionalMarkerInfo> directionalMarkers = new LinkedList<>();
	private List<SizedMarkerInfo> sizedMarkers = new LinkedList<>();
	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		mc = Minecraft.getInstance();
		MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(this::onRenderLevelLast);
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END || mc.level == null || mc.player == null || mc.isPaused()) {
			return;
		}
		for(InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = mc.player.getItemInHand(hand);
			if(stack.is(DistributorMarkerItem.INSTANCE)) {
				DirectionalGlobalPos globalPos = DistributorMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
				if(globalPos != null) {
					addDirectionalMarkers(List.of(globalPos), 0x00FFFF, 1);
				}
			}
			if(stack.is(ProxyMarkerItem.INSTANCE)) {
				DirectionalGlobalPos globalPos = ProxyMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
				if(globalPos != null) {
					addDirectionalMarkers(List.of(globalPos), 0xFF7F00, 1);
				}
			}
		}
	}

	public void onRenderLevelLast(RenderLevelLastEvent event) {
		render(event.getPoseStack(), event.getPartialTick());
	}

	public void addDirectionalMarkers(List<DirectionalGlobalPos> positions, int color, int lifetime) {
		directionalMarkers.add(new DirectionalMarkerInfo(positions, color, lifetime));
	}

	public void addSizedMarker(Vec3 lowerCorner, Vec3 size, int color, int lifetime) {
		sizedMarkers.add(new SizedMarkerInfo(lowerCorner, size, color, lifetime));
	}

	public void addBeams(Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout) {
		beams.add(new BeamInfo(source, deltas, color, lifetime, fadeout));
	}

	public void render(PoseStack poseStack, float partialTick) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		directionalMarkers.removeIf(marker->marker.shouldRemove(currentTick));
		sizedMarkers.removeIf(marker->marker.shouldRemove(currentTick));
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+partialTick;
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		MultiBufferSource.BufferSource buffers = RenderTypeHelper.BUFFERS;
		VertexConsumer quadBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_QUAD);
		VertexConsumer lineBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_LINE_4);

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
				Vec3 distVec = cameraPos.subtract(Vec3.atCenterOf(blockPos));
				if(Doubles.max(Math.abs(distVec.x), Math.abs(distVec.y), Math.abs(distVec.z)) > range) {
					continue;
				}

				poseStack.pushPose();
				poseStack.translate(blockPos.getX()-cameraPos.x, blockPos.getY()-cameraPos.y, blockPos.getZ()-cameraPos.z);

				Direction direction = globalPos.direction();
				addMarkerVertices(poseStack, quadBuffer, BLOCK_SIZE, direction, r, g, b, 127);
				addMarkerVertices(poseStack, lineBuffer, BLOCK_SIZE, null, r, g, b, 255);

				poseStack.popPose();
			}
		}

		RenderSystem.disableDepthTest();
		buffers.endBatch();
		RenderSystem.enableDepthTest();

		lineBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_LINE_4);

		for(SizedMarkerInfo marker : sizedMarkers) {
			Vec3 lowerCorner = marker.lowerCorner;

			poseStack.pushPose();
			poseStack.translate(lowerCorner.x-cameraPos.x, lowerCorner.y-cameraPos.y, lowerCorner.z-cameraPos.z);

			int r = marker.color>>16&0xFF;
			int g = marker.color>> 8&0xFF;
			int b = marker.color    &0xFF;
			addMarkerVertices(poseStack, lineBuffer, marker.size, null, r, g, b, 255);

			poseStack.popPose();
		}

		buffers.endBatch();

		lineBuffer = buffers.getBuffer(RenderTypeHelper.BEAM_LINE_3);

		for(BeamInfo beam : beams) {
			Vec3 source = beam.source();

			poseStack.pushPose();
			poseStack.translate(source.x-cameraPos.x, source.y-cameraPos.y, source.z-cameraPos.z);

			int r = beam.color>>16&0xFF;
			int g = beam.color>> 8&0xFF;
			int b = beam.color    &0xFF;
			int a = (int)(beam.getAlpha(renderTick)*255);
			for(Vec3 delta : beam.deltas) {
				addBeamVertices(poseStack, lineBuffer, delta, r, g, b, a);
			}

			poseStack.popPose();
		}

		buffers.endBatch();
	}

	public void addMarkerVertices(PoseStack poseStack, VertexConsumer buffer, Vec3 delta, Direction direction, int r, int g, int b, int a) {
		Matrix4f pose = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		if(direction == null || direction == Direction.NORTH) {
			// Face North, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			// Face North, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
		}
		if(direction == null || direction == Direction.SOUTH) {
			// Face South, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			// Face South, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
		}
		if(direction == null || direction == Direction.WEST) {
			// Face West, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
			// Face West, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
		}
		if(direction == null || direction == Direction.EAST) {
			// Face East, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
			// Face East, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
		}
		if(direction == Direction.DOWN) {
			// Face Down
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
		}
		if(direction == Direction.UP) {
			// Face Up
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			// Face North, Edge East
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
			// Face South, Edge East
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			// Face South, Edge West
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
		}
	}

	public void addBeamVertices(PoseStack poseStack, VertexConsumer buffer, Vec3 delta, int r, int g, int b, int a) {
		Vec3 normalVec = delta.normalize();
		Matrix4f pose = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		float xn = (float)normalVec.x;
		float yn = (float)normalVec.y;
		float zn = (float)normalVec.z;
		buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, xn, yn, zn).endVertex();
		buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, xn, yn, zn).endVertex();
	}

	public static record DirectionalMarkerInfo(List<DirectionalGlobalPos> positions, int color, int lifetime, int startTick) {

		public DirectionalMarkerInfo(List<DirectionalGlobalPos> positions, int color, int lifetime) {
			this(positions, color, lifetime, RenderTimer.INSTANCE.getTicks());
		}

		public boolean shouldRemove(int currentTick) {
			if(currentTick < startTick) {
				currentTick += 0x1FFFFF;
			}
			return currentTick-startTick >= lifetime;
		}
	}

	public static record SizedMarkerInfo(Vec3 lowerCorner, Vec3 size, int color, int lifetime, int startTick) {

		public SizedMarkerInfo(Vec3 lowerCorner, Vec3 size, int color, int lifetime) {
			this(lowerCorner, size, color, lifetime, RenderTimer.INSTANCE.getTicks());
		}

		public boolean shouldRemove(int currentTick) {
			if(currentTick < startTick) {
				currentTick += 0x1FFFFF;
			}
			return currentTick-startTick >= lifetime;
		}
	}

	public static record BeamInfo(Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout, int startTick) {

		public BeamInfo(Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout) {
			this(source, deltas, color, lifetime, fadeout, RenderTimer.INSTANCE.getTicks());
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

		private RenderTypeHelper(String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
			super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
		}

		public static final RenderType MARKER_LINE_4;
		public static final RenderType MARKER_QUAD;
		public static final RenderType BEAM_LINE_3;
		public static final MultiBufferSource.BufferSource BUFFERS;

		static {
			MARKER_LINE_4 = create("packagedauto:marker_line_4",
					DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 8192, false, false,
					CompositeState.builder().
					setShaderState(RENDERTYPE_LINES_SHADER).
					setLineState(new LineStateShard(OptionalDouble.of(4))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			MARKER_QUAD = create("packagedauto:marker_quad",
					DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 1024, false, false,
					CompositeState.builder().
					setShaderState(RenderStateShard.POSITION_COLOR_SHADER).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			BEAM_LINE_3 = create("packagedauto:beam_line_3",
					DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 8192, false, false,
					CompositeState.builder().
					setShaderState(RENDERTYPE_LINES_SHADER).
					setLineState(new LineStateShard(OptionalDouble.of(3))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setOutputState(ITEM_ENTITY_TARGET).
					setWriteMaskState(COLOR_DEPTH_WRITE).
					setCullState(NO_CULL).
					createCompositeState(false));
			BUFFERS = MultiBufferSource.immediateWithBuffers(
					Map.of(MARKER_LINE_4, new BufferBuilder(MARKER_LINE_4.bufferSize()),
							MARKER_QUAD, new BufferBuilder(MARKER_QUAD.bufferSize()),
							BEAM_LINE_3, new BufferBuilder(BEAM_LINE_3.bufferSize())),
					Tesselator.getInstance().getBuilder());
		}
	}
}
