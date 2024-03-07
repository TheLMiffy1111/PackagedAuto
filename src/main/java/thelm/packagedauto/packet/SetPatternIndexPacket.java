package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record SetPatternIndexPacket(int index) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:set_pattern_index");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(index);
	}

	public static SetPatternIndexPacket read(FriendlyByteBuf buf) {
		return new SetPatternIndexPacket(buf.readByte());
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.blockEntity.setPatternIndex(index);
					menu.setupSlots();
				}
			});
		}
	}
}
