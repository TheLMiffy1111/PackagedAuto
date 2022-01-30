package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public class SetPatternIndexPacket {

	private int index;

	public SetPatternIndexPacket(int index) {
		this.index = index;
	}

	public static void encode(SetPatternIndexPacket pkt, FriendlyByteBuf buf) {
		buf.writeByte(pkt.index);
	}

	public static SetPatternIndexPacket decode(FriendlyByteBuf buf) {
		return new SetPatternIndexPacket(buf.readByte());
	}

	public static void handle(SetPatternIndexPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.blockEntity.setPatternIndex(pkt.index);
				menu.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
