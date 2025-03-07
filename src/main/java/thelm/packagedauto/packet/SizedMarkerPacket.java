package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.client.WorldOverlayRenderer;

public record SizedMarkerPacket(Vec3 lowerCorner, Vec3 size, int color, int lifetime) implements CustomPacketPayload {

	public static final Type<SizedMarkerPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:sized_marker"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SizedMarkerPacket> STREAM_CODEC = StreamCodec.composite(
			PacketStreamCodecs.VEC3, SizedMarkerPacket::lowerCorner,
			PacketStreamCodecs.VEC3, SizedMarkerPacket::size,
			PacketStreamCodecs.MEDIUM, SizedMarkerPacket::color,
			ByteBufCodecs.UNSIGNED_SHORT, SizedMarkerPacket::lifetime,
			SizedMarkerPacket::new);

	@Override
	public Type<SizedMarkerPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addSizedMarker(lowerCorner, size, color, lifetime);
		});
	}

	public static void sendSizedMarker(ServerPlayer player, Vec3 lowerCorner, Vec3 size, int color, int lifetime) {
		PacketDistributor.sendToPlayer(player, new SizedMarkerPacket(lowerCorner, size, color, lifetime));
	}
}
