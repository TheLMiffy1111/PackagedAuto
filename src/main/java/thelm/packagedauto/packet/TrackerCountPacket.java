package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.UnpackagerMenu;

public record TrackerCountPacket(boolean decrease) implements CustomPacketPayload {

	public static final Type<TrackerCountPacket> TYPE = new Type<>(Identifier.parse("packagedauto:tracker_count"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TrackerCountPacket> STREAM_CODEC = ByteBufCodecs.BOOL.
			map(TrackerCountPacket::new, TrackerCountPacket::decrease).cast();

	@Override
	public Type<TrackerCountPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof UnpackagerMenu menu) {
					menu.blockEntity.changeTrackerCount(decrease);
				}
			});
		}
	}
}
