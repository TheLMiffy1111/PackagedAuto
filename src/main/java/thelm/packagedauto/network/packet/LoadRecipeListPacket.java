package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public class LoadRecipeListPacket {

	public LoadRecipeListPacket() {}

	public static void encode(LoadRecipeListPacket pkt, FriendlyByteBuf buf) {

	}

	public static LoadRecipeListPacket decode(FriendlyByteBuf buf) {
		return new LoadRecipeListPacket();
	}

	public static void handle(LoadRecipeListPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.blockEntity.loadRecipeList();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
