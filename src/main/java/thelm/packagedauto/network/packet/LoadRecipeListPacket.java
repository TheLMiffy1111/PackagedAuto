package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class LoadRecipeListPacket {

	public LoadRecipeListPacket() {}

	public static void encode(LoadRecipeListPacket pkt, PacketBuffer buf) {}

	public static LoadRecipeListPacket decode(PacketBuffer buf) {
		return new LoadRecipeListPacket();
	}

	public static void handle(LoadRecipeListPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.tile.loadRecipeList();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
