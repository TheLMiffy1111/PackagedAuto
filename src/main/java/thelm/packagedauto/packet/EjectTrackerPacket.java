package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.UnpackagerMenu;

public record EjectTrackerPacket(int index) implements CustomPacketPayload {

	public static final Type<EjectTrackerPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:eject_tracker"));
	public static final StreamCodec<RegistryFriendlyByteBuf, EjectTrackerPacket> STREAM_CODEC = PacketStreamCodecs.UNSIGNED_BYTE.
			map(EjectTrackerPacket::new, EjectTrackerPacket::index).cast();

	@Override
	public Type<EjectTrackerPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof UnpackagerMenu menu) {
					menu.blockEntity.trackers[index].ejectItems();
				}
			});
		}
	}
}
