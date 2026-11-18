package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.FluidPackageFillerMenu;

public record SetFluidAmountPacket(int amount) implements CustomPacketPayload {

	public static final Type<SetFluidAmountPacket> TYPE = new Type<>(Identifier.parse("packagedauto:set_fluid_amount"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetFluidAmountPacket> STREAM_CODEC = ByteBufCodecs.INT.
			map(SetFluidAmountPacket::new, SetFluidAmountPacket::amount).cast();

	@Override
	public Type<SetFluidAmountPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof FluidPackageFillerMenu menu) {
					menu.blockEntity.requiredAmount = amount;
				}
			});
		}
	}
}
