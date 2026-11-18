package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record SaveRecipeListPacket(boolean single) implements CustomPacketPayload {

	public static final Type<SaveRecipeListPacket> TYPE = new Type<>(Identifier.parse("packagedauto:save_recipe_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SaveRecipeListPacket> STREAM_CODEC = ByteBufCodecs.BOOL.
			map(SaveRecipeListPacket::new, SaveRecipeListPacket::single).cast();

	@Override
	public Type<SaveRecipeListPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.blockEntity.saveRecipeList(single);
				}
			});
		}
	}
}
