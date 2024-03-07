package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;

public record ChangePackagingPacket() implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:change_packaging");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {}

	public static ChangePackagingPacket read(FriendlyByteBuf buf) {
		return new ChangePackagingPacket();
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof PackagerMenu menu) {
					menu.blockEntity.changePackagingMode();
				}
				if(player.containerMenu instanceof PackagerExtensionMenu menu) {
					menu.blockEntity.changePackagingMode();
				}
			});
		}
	}
}
