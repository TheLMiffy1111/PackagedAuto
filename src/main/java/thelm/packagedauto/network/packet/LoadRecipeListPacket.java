package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class LoadRecipeListPacket {

	private final boolean single;
	private final boolean clear;

	public LoadRecipeListPacket(boolean single, boolean clear) {
		this.single = single;
		this.clear = clear;
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(single);
		buf.writeBoolean(clear);
	}

	public static LoadRecipeListPacket decode(PacketBuffer buf) {
		return new LoadRecipeListPacket(buf.readBoolean(), buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.tile.loadRecipeList(single, clear);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
