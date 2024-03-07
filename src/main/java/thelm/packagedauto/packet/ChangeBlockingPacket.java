package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.UnpackagerMenu;

public record ChangeBlockingPacket() implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:change_blocking");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {}

	public static ChangeBlockingPacket read(FriendlyByteBuf buf) {
		return new ChangeBlockingPacket();
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof UnpackagerMenu menu) {
					menu.blockEntity.changeBlockingMode();
				}
			});
		}
	}
}
