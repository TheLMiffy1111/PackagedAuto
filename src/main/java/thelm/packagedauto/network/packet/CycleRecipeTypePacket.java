package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class CycleRecipeTypePacket {

	private final boolean reverse;

	public CycleRecipeTypePacket(boolean reverse) {
		this.reverse = reverse;
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(reverse);
	}

	public static CycleRecipeTypePacket decode(PacketBuffer buf) {
		return new CycleRecipeTypePacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.patternItemHandler.cycleRecipeType(reverse);
				container.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
