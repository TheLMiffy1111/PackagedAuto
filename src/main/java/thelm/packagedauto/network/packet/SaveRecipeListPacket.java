package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class SaveRecipeListPacket {

	private final boolean single;

	public SaveRecipeListPacket(boolean single) {
		this.single = single;
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(single);
	}

	public static SaveRecipeListPacket decode(PacketBuffer buf) {
		return new SaveRecipeListPacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.tile.saveRecipeList(single);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
