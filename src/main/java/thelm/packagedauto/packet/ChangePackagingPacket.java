package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;

public record ChangePackagingPacket() implements CustomPacketPayload {

	public static final Type<ChangePackagingPacket> TYPE = new Type<>(Identifier.parse("packagedauto:change_packaging"));
	public static final ChangePackagingPacket INSTANCE = new ChangePackagingPacket();
	public static final StreamCodec<RegistryFriendlyByteBuf, ChangePackagingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ChangePackagingPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
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
