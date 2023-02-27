package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class SaveRecipeListPacket {

	private boolean single;

	public SaveRecipeListPacket(boolean single) {
		this.single = single;
	}

	public static void encode(SaveRecipeListPacket pkt, PacketBuffer buf) {
		buf.writeBoolean(pkt.single);
	}

	public static SaveRecipeListPacket decode(PacketBuffer buf) {
		return new SaveRecipeListPacket(buf.readBoolean());
	}

	public static void handle(SaveRecipeListPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.tile.saveRecipeList(pkt.single);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
