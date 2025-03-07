package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public record LoadRecipeListPacket(boolean single, boolean clear) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(single);
		buf.writeBoolean(clear);
	}

	public static LoadRecipeListPacket decode(FriendlyByteBuf buf) {
		return new LoadRecipeListPacket(buf.readBoolean(), buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.blockEntity.loadRecipeList(single, clear);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
