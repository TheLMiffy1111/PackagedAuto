package thelm.packagedauto.packet;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.client.WorldOverlayRenderer;

public record DirectionalMarkerPacket(List<DirectionalGlobalPos> positions, int color, int lifetime) implements CustomPacketPayload {

	public static final Type<DirectionalMarkerPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:directional_marker"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DirectionalMarkerPacket> STREAM_CODEC = StreamCodec.composite(
			DirectionalGlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list()), DirectionalMarkerPacket::positions,
			PacketStreamCodecs.MEDIUM, DirectionalMarkerPacket::color,
			ByteBufCodecs.UNSIGNED_SHORT, DirectionalMarkerPacket::lifetime,
			DirectionalMarkerPacket::new);

	@Override
	public Type<DirectionalMarkerPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addDirectionalMarkers(positions, color, lifetime);
		});
	}

	public static void sendDirectionalMarkers(ServerPlayer player, List<DirectionalGlobalPos> positions, int color, int lifetime) {
		PacketDistributor.sendToPlayer(player, new DirectionalMarkerPacket(positions, color, lifetime));
	}
}
