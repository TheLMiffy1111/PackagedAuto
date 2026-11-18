package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record CycleRecipeTypePacket(boolean reverse) implements CustomPacketPayload {

	public static final Type<CycleRecipeTypePacket> TYPE = new Type<>(Identifier.parse("packagedauto:cycle_recipe_type"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CycleRecipeTypePacket> STREAM_CODEC = ByteBufCodecs.BOOL.
			map(CycleRecipeTypePacket::new, CycleRecipeTypePacket::reverse).cast();

	@Override
	public Type<CycleRecipeTypePacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.patternItemHandler.cycleRecipeType(reverse);
					menu.setupSlots();
				}
			});
		}
	}
}
