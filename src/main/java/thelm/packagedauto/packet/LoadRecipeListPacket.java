package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record LoadRecipeListPacket(boolean single, boolean clear) implements CustomPacketPayload {

	public static final Type<LoadRecipeListPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:load_recipe_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LoadRecipeListPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, LoadRecipeListPacket::single,
			ByteBufCodecs.BOOL, LoadRecipeListPacket::clear,
			LoadRecipeListPacket::new);

	@Override
	public Type<LoadRecipeListPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.blockEntity.loadRecipeList(single, clear);
				}
			});
		}
	}
}
