package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record SaveRecipeListPacket(boolean single) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:save_recipe_list");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(single);
	}

	public static SaveRecipeListPacket read(FriendlyByteBuf buf) {
		return new SaveRecipeListPacket(buf.readBoolean());
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.blockEntity.saveRecipeList(single);
				}
			});
		}
	}
}
