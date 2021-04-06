package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class CycleRecipeTypePacket {

	private boolean reverse;

	public CycleRecipeTypePacket(boolean reverse) {
		this.reverse = reverse;
	}

	public static void encode(CycleRecipeTypePacket pkt, PacketBuffer buf) {
		buf.writeBoolean(pkt.reverse);
	}

	public static CycleRecipeTypePacket decode(PacketBuffer buf) {
		return new CycleRecipeTypePacket(buf.readBoolean());
	}

	public static void handle(CycleRecipeTypePacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.openContainer instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.openContainer;
				container.patternItemHandler.cycleRecipeType(pkt.reverse);
				container.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
