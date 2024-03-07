package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.FluidPackageFillerMenu;

public record SetFluidAmountPacket(int amount) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:set_fluid_amount");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(amount);
	}

	public static SetFluidAmountPacket read(FriendlyByteBuf buf) {
		return new SetFluidAmountPacket(buf.readInt());
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof FluidPackageFillerMenu menu) {
					menu.blockEntity.requiredAmount = amount;
				}
			});
		}
	}
}
