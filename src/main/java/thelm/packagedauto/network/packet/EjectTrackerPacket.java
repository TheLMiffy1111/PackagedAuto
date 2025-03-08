package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.UnpackagerContainer;

public class EjectTrackerPacket {

	private final int index;

	public EjectTrackerPacket(int index) {
		this.index = index;
	}

	public void encode(PacketBuffer buf) {
		buf.writeByte(index);
	}

	public static EjectTrackerPacket decode(PacketBuffer buf) {
		return new EjectTrackerPacket(buf.readUnsignedByte());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerContainer) {
				UnpackagerContainer container = (UnpackagerContainer)player.containerMenu;
				container.tile.trackers[index].ejectItems();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
