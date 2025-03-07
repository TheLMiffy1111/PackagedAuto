package thelm.packagedauto.packet;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.client.WorldOverlayRenderer;

public record BeamPacket(Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout) implements CustomPacketPayload {

	public static final Type<BeamPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:beam"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BeamPacket> STREAM_CODEC = StreamCodec.composite(
			PacketStreamCodecs.VEC3, BeamPacket::source,
			PacketStreamCodecs.VEC3.apply(ByteBufCodecs.list()), BeamPacket::deltas,
			PacketStreamCodecs.MEDIUM, BeamPacket::color,
			ByteBufCodecs.UNSIGNED_SHORT, BeamPacket::lifetime,
			ByteBufCodecs.BOOL, BeamPacket::fadeout,
			BeamPacket::new);

	@Override
	public Type<BeamPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addBeams(source, deltas, color, lifetime, fadeout);
		});
	}

	public static void sendBeams(ServerLevel level, Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout, double range) {
		PacketDistributor.sendToPlayersNear(level, null, source.x, source.y, source.z, range, new BeamPacket(source, deltas, color, lifetime, fadeout));
	}

	public static void sendBeams(ServerPlayer player, Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout) {
		PacketDistributor.sendToPlayer(player, new BeamPacket(source, deltas, color, lifetime, fadeout));
	}
}
