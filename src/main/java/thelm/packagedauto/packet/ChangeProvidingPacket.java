package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.menu.PackagingProviderMenu;

public record ChangeProvidingPacket(PackagingProviderBlockEntity.Type provideType) implements CustomPacketPayload {

	public static final Type<ChangeProvidingPacket> TYPE = new Type<>(Identifier.parse("packagedauto:change_providing"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ChangeProvidingPacket> STREAM_CODEC = PackagingProviderBlockEntity.Type.STREAM_CODEC.
			map(ChangeProvidingPacket::new, ChangeProvidingPacket::provideType).cast();

	@Override
	public Type<ChangeProvidingPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof PackagingProviderMenu menu) {
					menu.blockEntity.changeProvideType(provideType);
				}
			});
		}
	}
}
