package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class SetPatternIndexPacket {

	private int index;

	public SetPatternIndexPacket(int index) {
		this.index = index;
	}

	public static void encode(SetPatternIndexPacket pkt, PacketBuffer buf) {
		buf.writeByte(pkt.index);
	}

	public static SetPatternIndexPacket decode(PacketBuffer buf) {
		return new SetPatternIndexPacket(buf.readByte());
	}

	public static void handle(SetPatternIndexPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.tile.setPatternIndex(pkt.index);
				container.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
