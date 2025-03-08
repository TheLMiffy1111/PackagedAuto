package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record SetPatternIndexPacket(int index) implements CustomPacketPayload {

	public static final Type<SetPatternIndexPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:set_pattern_index"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetPatternIndexPacket> STREAM_CODEC = PacketStreamCodecs.UNSIGNED_BYTE.
			map(SetPatternIndexPacket::new, SetPatternIndexPacket::index).cast();

	@Override
	public Type<SetPatternIndexPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.blockEntity.setPatternIndex(index);
					menu.setupSlots();
				}
			});
		}
	}
}
