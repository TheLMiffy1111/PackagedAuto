package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public class CycleRecipeTypePacket {

	private boolean reverse;

	public CycleRecipeTypePacket(boolean reverse) {
		this.reverse = reverse;
	}

	public static void encode(CycleRecipeTypePacket pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.reverse);
	}

	public static CycleRecipeTypePacket decode(FriendlyByteBuf buf) {
		return new CycleRecipeTypePacket(buf.readBoolean());
	}

	public static void handle(CycleRecipeTypePacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.patternItemHandler.cycleRecipeType(pkt.reverse);
				menu.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
