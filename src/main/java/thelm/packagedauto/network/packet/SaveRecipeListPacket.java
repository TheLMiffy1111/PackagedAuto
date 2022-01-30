package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public class SaveRecipeListPacket {

	private boolean single;

	public SaveRecipeListPacket(boolean single) {
		this.single = single;
	}

	public static void encode(SaveRecipeListPacket pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.single);
	}

	public static SaveRecipeListPacket decode(FriendlyByteBuf buf) {
		return new SaveRecipeListPacket(buf.readBoolean());
	}

	public static void handle(SaveRecipeListPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.blockEntity.saveRecipeList(pkt.single);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
